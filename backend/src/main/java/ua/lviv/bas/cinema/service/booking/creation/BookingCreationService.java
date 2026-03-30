package ua.lviv.bas.cinema.service.booking.creation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.BookingStatus;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
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
import ua.lviv.bas.cinema.service.booking.reservation.ReservationValidator;
import ua.lviv.bas.cinema.service.shared.AuditService;
import ua.lviv.bas.cinema.service.shared.PriceCalculatorService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingCreationService {
	private final BookingRepository bookingRepository;
	private final SessionRepository sessionRepository;
	private final SeatRepository seatRepository;
	private final TicketTypeRepository ticketTypeRepository;
	private final SeatReservationRepository seatReservationRepository;
	private final BookingMapper bookingMapper;
	private final BonusService bonusService;
	private final PriceCalculatorService priceCalculator;
	private final BookingPriceCalculator bookingPriceCalculator;
	private final BookingValidator bookingValidator;
	private final ReservationValidator availabilityValidator;
	private final AuditService auditService;

	@Value("${booking.expiration-minutes:20}")
	private int expirationMinutes;

	@Value("${booking.temp-hold-minutes:5}")
	private int tempHoldMinutes;

	@Caching(evict = { @CacheEvict(value = "seatAvailability", key = "#request.sessionId()"),
			@CacheEvict(value = "availableSeatsCount", key = "#request.sessionId()"),
			@CacheEvict(value = "sessions", allEntries = true) })
	public BookingResponse createBooking(BookingCreateRequest request, User user) {
		Session session = sessionRepository.findById(request.sessionId())
				.orElseThrow(() -> new SessionNotFoundException(request.sessionId()));

		bookingValidator.validateSessionForBooking(session);

		List<SeatReservation> seatReservations = new ArrayList<>();

		for (BookingCreateRequest.SeatSelectionRequest seatSelection : request.seats()) {
			SeatReservation reservation = findOrCreateValidReservation(session, user, seatSelection);
			updateReservationWithTicketType(reservation, seatSelection);
			seatReservations.add(reservation);
		}

		BigDecimal totalPrice = seatReservations.stream().map(SeatReservation::getSeatPrice).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		BookingPriceCalculator.BookingPriceResult priceResult = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				request.bonusPointsToUse(), user.getId());

		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
		Booking booking = createBookingEntity(user, session, seatReservations, priceResult, expiresAt);

		seatReservations.forEach(sr -> {
			sr.setBooking(booking);
			sr.setStatus(ReservationStatus.CONFIRMED);
			sr.setReservedUntil(expiresAt);
		});

		Booking savedBooking = bookingRepository.save(booking);
		seatReservationRepository.saveAll(seatReservations);

		if (priceResult.bonusPointsUsed() > 0) {
			bonusService.spendPoints(user.getId(), priceResult.bonusPointsUsed(), savedBooking);
		}

		log.info("Created booking {} for user {} with {} bonus points used", savedBooking.getId(), user.getId(),
				priceResult.bonusPointsUsed());

		auditService.logChange("Booking", savedBooking.getId(), "CREATED", null,
				String.format("User: %d, Session: %d, Total price: %s, Final price: %s", user.getId(), session.getId(),
						totalPrice, priceResult.finalPrice()));

		return bookingMapper.toBookingResponse(savedBooking);
	}

	private SeatReservation findOrCreateValidReservation(Session session, User user,
			BookingCreateRequest.SeatSelectionRequest seatSelection) {

		Long seatId = seatSelection.seatId();

		Optional<SeatReservation> existingReservation = seatReservationRepository
				.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(session.getId(), seatId,
						ReservationStatus.PENDING, user.getId());

		if (existingReservation.isPresent()) {
			SeatReservation reservation = existingReservation.get();

			if (reservation.getReservedUntil().isBefore(LocalDateTime.now())) {
				reservation.setReservedUntil(LocalDateTime.now().plusMinutes(tempHoldMinutes));
				return seatReservationRepository.save(reservation);
			}

			return reservation;
		}

		Seat seat = seatRepository.findById(seatId)
				.orElseThrow(() -> new SeatNotAvailableException("Seat not found: " + seatId));

		availabilityValidator.validateSeat(session.getId(), seatId);

		SeatReservation newReservation = SeatReservation.builder().seat(seat).session(session).ticketType(null)
				.seatPrice(null).status(ReservationStatus.PENDING)
				.reservedUntil(LocalDateTime.now().plusMinutes(tempHoldMinutes)).reservedByUser(user).build();

		return seatReservationRepository.save(newReservation);
	}

	private void updateReservationWithTicketType(SeatReservation reservation,
			BookingCreateRequest.SeatSelectionRequest seatSelection) {

		TicketType ticketType = ticketTypeRepository.findById(seatSelection.ticketTypeId())
				.orElseThrow(() -> new TicketTypeNotFoundException(seatSelection.ticketTypeId()));

		BigDecimal seatPrice = priceCalculator.calculateSeatPrice(reservation.getSession(), reservation.getSeat(),
				ticketType);

		reservation.setTicketType(ticketType);
		reservation.setSeatPrice(seatPrice);
	}

	private Booking createBookingEntity(User user, Session session, List<SeatReservation> seatReservations,
			BookingPriceCalculator.BookingPriceResult priceResult, LocalDateTime expiresAt) {
		return Booking.builder().user(user).session(session).status(BookingStatus.PENDING)
				.totalPrice(priceResult.totalPrice()).bonusPointsUsed(priceResult.bonusPointsUsed())
				.bonusDiscountAmount(priceResult.bonusDiscount()).finalPrice(priceResult.finalPrice())
				.expiresAt(expiresAt).seatReservations(seatReservations).build();
	}
}