package ua.lviv.bas.cinema.service.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingOperationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.booking.BookingMapper;
import ua.lviv.bas.cinema.repository.booking.BookingRepository;
import ua.lviv.bas.cinema.repository.booking.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.cinema.SeatRepository;
import ua.lviv.bas.cinema.repository.cinema.SessionRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketTypeRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.common.PriceCalculatorService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;
    private final SeatRepository seatRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final BookingMapper bookingMapper;
    private final BonusService bonusService;
    private final PriceCalculatorService priceCalculator;
    private final SeatReservationService seatReservationService;
    private final AuditService auditService;

    @Value("${booking.expiration-minutes:20}")
    private int expirationMinutes;

    @Value("${booking.temp-hold-minutes:5}")
    private int tempHoldMinutes;

    @Value("${booking.session-too-close-minutes:30}")
    private int sessionTooCloseMinutes;

    @CacheEvict(value = {"seatAvailability", "availableSeatsCount", "sessions"}, allEntries = true)
    public BookingResponse createBooking(BookingCreateRequest request, User user) {
        var session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new SessionNotFoundException(request.sessionId()));

        validateSession(session);

        List<SeatReservation> seatReservations = new ArrayList<>();

        for (var seatSelection : request.seats()) {
            var reservation = findOrCreateReservation(session, user, seatSelection);

            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                throw new SeatNotAvailableException("Seat already booked");
            }

            updateReservationWithTicketType(reservation, seatSelection);
            seatReservations.add(reservation);
        }

        var totalPrice = seatReservations.stream().map(SeatReservation::getSeatPrice).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        var priceResult = calculateFinalPrice(totalPrice, request.bonusPointsToUse(), user.getId());
        var expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        var booking = createBookingEntity(user, session, seatReservations, priceResult, expiresAt);

        seatReservations.forEach(sr -> {
            sr.setBooking(booking);
            sr.setStatus(ReservationStatus.CONFIRMED);
            sr.setReservedUntil(expiresAt);
        });

        var saved = bookingRepository.save(booking);
        seatReservationRepository.saveAll(seatReservations);

        if (priceResult.bonusPointsUsed() > 0) {
            bonusService.spendPoints(user.getId(), priceResult.bonusPointsUsed(), saved);
        }

        log.info("Created booking {} for user {} with {} bonus points used", saved.getId(), user.getId(),
                priceResult.bonusPointsUsed());
        auditCreate(saved, user, session, totalPrice, priceResult);

        return bookingMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId, User user) {
        var booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        return bookingMapper.toResponse(booking);
    }

    public void cancelBooking(Long bookingId, User user) {
        var booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (!canCancel(booking)) {
            throw BookingValidationException.cannotCancel();
        }

        var oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);

        booking.getSeatReservations().forEach(sr -> {
            sr.setStatus(ReservationStatus.EXPIRED);
            sr.setBooking(null);
        });

        seatReservationRepository.saveAll(booking.getSeatReservations());

        if (booking.getBonusPointsUsed() != null && booking.getBonusPointsUsed() > 0) {
            bonusService.refundPoints(booking);
        }

        bookingRepository.save(booking);
        log.info("Cancelled booking {} for user {}", bookingId, user.getId());
        auditCancel(bookingId, oldStatus);
    }

    public void confirmBooking(Long bookingId) {
        var booking = bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw BookingOperationException.onlyPendingCanBeConfirmed();
        }

        var oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.getSeatReservations().forEach(sr -> sr.setStatus(ReservationStatus.CONFIRMED));
        bookingRepository.save(booking);
        auditConfirm(bookingId, oldStatus);
    }

    private BookingPriceResult calculateFinalPrice(BigDecimal totalPrice, Integer bonusPointsToUse, Long userId) {
        BigDecimal bonusDiscount = BigDecimal.ZERO;
        Integer bonusPointsUsed = 0;

        if (bonusPointsToUse != null && bonusPointsToUse > 0) {
            bonusService.validatePointsForBooking(userId, bonusPointsToUse, totalPrice);
            bonusDiscount = priceCalculator.calculateBonusDiscount(bonusPointsToUse);
            bonusPointsUsed = bonusPointsToUse;
        }

        var finalPrice = totalPrice.subtract(bonusDiscount).max(BigDecimal.ZERO);
        return new BookingPriceResult(totalPrice, bonusPointsUsed, bonusDiscount, finalPrice);
    }

    private record BookingPriceResult(BigDecimal totalPrice, Integer bonusPointsUsed, BigDecimal bonusDiscount,
                                      BigDecimal finalPrice) {
    }

    private void validateSession(Session session) {
        if (session.getStatus() != CinemaSessionStatus.SCHEDULED) {
            throw BookingValidationException.sessionNotAvailable();
        }
        if (session.getStartTime().isBefore(LocalDateTime.now())) {
            throw BookingValidationException.sessionAlreadyStarted();
        }
        if (session.getStartTime().isBefore(LocalDateTime.now().plusMinutes(sessionTooCloseMinutes))) {
            throw BookingValidationException.sessionTooClose();
        }
    }

    private boolean canCancel(Booking booking) {
        return booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED;
    }

    private SeatReservation findOrCreateReservation(Session session, User user,
                                                    BookingCreateRequest.SeatSelectionRequest seatSelection) {
        var seatId = seatSelection.seatId();

        Optional<SeatReservation> existingReservation = seatReservationRepository
                .findBySessionIdAndSeatIdAndStatusAndReservedByUserId(session.getId(), seatId,
                        ReservationStatus.PENDING, user.getId());

        if (existingReservation.isPresent()) {
            var reservation = existingReservation.get();
            if (reservation.getReservedUntil().isBefore(LocalDateTime.now())) {
                reservation.setReservedUntil(LocalDateTime.now().plusMinutes(tempHoldMinutes));
                return seatReservationRepository.save(reservation);
            }
            return reservation;
        }

        var seat = seatRepository.findById(seatId)
                .orElseThrow(() -> new SeatNotAvailableException("Seat not found: " + seatId));

        seatReservationService.validateAvailability(session.getId(), seatId);

        var newReservation = SeatReservation.builder().seat(seat).session(session).ticketType(null).seatPrice(null)
                .status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(tempHoldMinutes))
                .reservedByUser(user).build();

        return seatReservationRepository.save(newReservation);
    }

    private void updateReservationWithTicketType(SeatReservation reservation,
                                                 BookingCreateRequest.SeatSelectionRequest seatSelection) {
        var ticketType = ticketTypeRepository.findById(seatSelection.ticketTypeId())
                .orElseThrow(() -> new TicketTypeNotFoundException(seatSelection.ticketTypeId()));

        var seatPrice = priceCalculator.calculateSeatPrice(reservation.getSession(), reservation.getSeat(), ticketType);

        reservation.setTicketType(ticketType);
        reservation.setSeatPrice(seatPrice);
    }

    private Booking createBookingEntity(User user, Session session, List<SeatReservation> seatReservations,
                                        BookingPriceResult priceResult, LocalDateTime expiresAt) {
        return Booking.builder().user(user).session(session).status(BookingStatus.PENDING)
                .totalPrice(priceResult.totalPrice()).bonusPointsUsed(priceResult.bonusPointsUsed())
                .bonusDiscountAmount(priceResult.bonusDiscount()).finalPrice(priceResult.finalPrice())
                .expiresAt(expiresAt).seatReservations(seatReservations).build();
    }

    private void auditCreate(Booking booking, User user, Session session, BigDecimal totalPrice,
                             BookingPriceResult priceResult) {
        Map<String, Object> details = new HashMap<>();
        details.put("userId", user.getId());
        details.put("sessionId", session.getId());
        details.put("totalPrice", totalPrice);
        details.put("finalPrice", priceResult.finalPrice());
        details.put("bonusPointsUsed", priceResult.bonusPointsUsed());
        auditService.logChange("Booking", booking.getId(), "Booking #" + booking.getId(), AuditAction.CREATED, null,
                details);
    }

    private void auditCancel(Long bookingId, BookingStatus oldStatus) {
        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("status", oldStatus);
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("status", BookingStatus.CANCELLED);
        auditService.logChange("Booking", bookingId, "Booking #" + bookingId, AuditAction.CANCELLED, oldDetails,
                newDetails);
    }

    private void auditConfirm(Long bookingId, BookingStatus oldStatus) {
        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("status", oldStatus);
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("status", BookingStatus.CONFIRMED);
        auditService.logChange("Booking", bookingId, "Booking #" + bookingId, AuditAction.CONFIRMED, oldDetails,
                newDetails);
    }
}