package ua.lviv.bas.cinema.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.SeatReservation;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.booking.dto.request.BookingCreateRequest;
import ua.lviv.bas.cinema.booking.dto.response.BookingResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingConcurrentModificationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingOperationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.booking.mapper.BookingMapper;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.ticket.domain.TicketType;
import ua.lviv.bas.cinema.ticket.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;
import ua.lviv.bas.cinema.bonus.service.BonusQueryService;
import ua.lviv.bas.cinema.common.PriceCalculatorService;
import ua.lviv.bas.cinema.audit.service.AuditService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SessionRepository sessionRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final BookingMapper bookingMapper;
    private final BonusLedgerService bonusLedgerService;
    private final BonusQueryService bonusQueryService;
    private final PriceCalculatorService priceCalculator;
    private final SeatReservationService seatReservationService;
    private final AuditService auditService;
    private final CacheManager cacheManager;

    @Value("${booking.expiration-minutes:20}")
    private int expirationMinutes;

    @Value("${booking.temp-hold-minutes:5}")
    private int tempHoldMinutes;

    @Value("${booking.session-too-close-minutes:30}")
    private int sessionTooCloseMinutes;

    @Caching(evict = {
            @CacheEvict(value = "seatAvailability", key = "#request.sessionId()"),
            @CacheEvict(value = "sessions", allEntries = true)
    })
    public BookingResponse createBooking(BookingCreateRequest request, User user) {
        var session = sessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new EntityNotFoundException("Session", request.sessionId()));

        validateSession(session);

        var seatReservations = buildSeatReservations(session, user, request.seats());
        var totalPrice = seatReservations.stream().map(SeatReservation::getSeatPrice).reduce(BigDecimal.ZERO,
                BigDecimal::add);

        var priceResult = calculateFinalPrice(totalPrice, request.bonusPointsToUse(), user.getId());
        var expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
        var booking = createBookingEntity(user, session, seatReservations, priceResult, expiresAt);
        var saved = confirmSeatsAndSaveBooking(booking, seatReservations, expiresAt);

        if (priceResult.bonusPointsUsed() > 0) {
            bonusLedgerService.spendPoints(user.getId(), priceResult.bonusPointsUsed(), saved);
        }

        log.info("Created booking {} for user {} with {} bonus points used", saved.getId(), user.getId(),
                priceResult.bonusPointsUsed());
        auditCreate(saved, user, session, totalPrice, priceResult);

        return bookingMapper.toResponse(saved);
    }

    private List<SeatReservation> buildSeatReservations(Session session, User user,
                                                        List<BookingCreateRequest.SeatSelectionRequest> seatSelections) {
        List<SeatReservation> seatReservations = new ArrayList<>();

        var orderedSeatSelections = seatSelections.stream()
                .sorted(Comparator.comparing(BookingCreateRequest.SeatSelectionRequest::seatId)).toList();

        var ticketTypesById = findTicketTypesByIds(orderedSeatSelections);

        for (var seatSelection : orderedSeatSelections) {
            var reservation = findOrCreateReservation(session, user, seatSelection);

            if (reservation.getStatus() == ReservationStatus.CONFIRMED) {
                throw new SeatNotAvailableException("Seat already booked");
            }

            updateReservationWithTicketType(reservation, seatSelection, ticketTypesById);
            seatReservations.add(reservation);
        }

        return seatReservations;
    }

    private Map<Long, TicketType> findTicketTypesByIds(
            List<BookingCreateRequest.SeatSelectionRequest> seatSelections) {
        var distinctIds = seatSelections.stream().map(BookingCreateRequest.SeatSelectionRequest::ticketTypeId)
                .distinct().toList();
        return ticketTypeRepository.findAllById(distinctIds).stream()
                .collect(Collectors.toMap(TicketType::getId, Function.identity()));
    }

    private Booking confirmSeatsAndSaveBooking(Booking booking, List<SeatReservation> seatReservations,
                                               LocalDateTime expiresAt) {
        seatReservations.forEach(sr -> {
            sr.setBooking(booking);
            sr.setStatus(ReservationStatus.CONFIRMED);
            sr.setReservedUntil(expiresAt);
        });

        var saved = bookingRepository.save(booking);
        seatReservationRepository.saveAll(seatReservations);
        return saved;
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId, User user) {
        var booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
        return bookingMapper.toResponse(booking);
    }

    public void cancelBooking(Long bookingId, User user) {
        var booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));

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
            bonusLedgerService.refundPoints(booking);
        }

        try {
            bookingRepository.saveAndFlush(booking);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BookingConcurrentModificationException(bookingId);
        }
        evictSeatAvailabilityCache(booking.getSession().getId());
        log.info("Cancelled booking {} for user {}", bookingId, user.getId());
        auditCancel(bookingId, oldStatus);
    }

    private void evictSeatAvailabilityCache(Long sessionId) {
        Cache cache = cacheManager.getCache("seatAvailability");
        if (cache != null) {
            cache.evict(sessionId);
        }
    }

    public void confirmBooking(Long bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw BookingOperationException.onlyPendingCanBeConfirmed();
        }

        var oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.getSeatReservations().forEach(sr -> sr.setStatus(ReservationStatus.CONFIRMED));

        try {
            bookingRepository.saveAndFlush(booking);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BookingConcurrentModificationException(bookingId);
        }
        auditConfirm(bookingId, oldStatus);
    }

    private BookingPriceResult calculateFinalPrice(BigDecimal totalPrice, Integer bonusPointsToUse, Long userId) {
        BigDecimal bonusDiscount = BigDecimal.ZERO;
        Integer bonusPointsUsed = 0;

        if (bonusPointsToUse != null && bonusPointsToUse > 0) {
            bonusQueryService.validatePointsForBooking(userId, bonusPointsToUse, totalPrice);
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

        var seat = seatReservationService.lockSeat(seatId);

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

        return seatReservationService.holdLockedSeat(session, seat, user);
    }

    private void updateReservationWithTicketType(SeatReservation reservation,
                                                 BookingCreateRequest.SeatSelectionRequest seatSelection,
                                                 Map<Long, TicketType> ticketTypesById) {
        var ticketType = Optional.ofNullable(ticketTypesById.get(seatSelection.ticketTypeId()))
                .orElseThrow(() -> new EntityNotFoundException("Ticket type", seatSelection.ticketTypeId()));

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