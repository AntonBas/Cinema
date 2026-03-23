package ua.lviv.bas.cinema.service.booking.creation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.BookingMapper;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;
import ua.lviv.bas.cinema.service.shared.PriceCalculatorService;
import ua.lviv.bas.cinema.service.user.BonusService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingCreationService {
	private final BookingRepository bookingRepository;
	private final SessionRepository sessionRepository;
	private final TicketTypeRepository ticketTypeRepository;
	private final SeatReservationRepository seatReservationRepository;
	private final BookingMapper bookingMapper;
	private final BonusService bonusService;
	private final PriceCalculatorService priceCalculator;
	private final NumberGeneratorService numberGenerator;
	private final BookingPriceCalculator bookingPriceCalculator;
	private final BookingValidator bookingValidator;

	@Value("${booking.expiration-minutes:20}")
	private int expirationMinutes;

	public BookingResponse createBooking(BookingCreateRequest request, User user) {
		Session session = sessionRepository.findById(request.sessionId())
				.orElseThrow(() -> new SessionNotFoundException(request.sessionId()));

		bookingValidator.validateSessionForBooking(session);

		List<SeatReservation> seatReservations = new ArrayList<>();

		for (BookingCreateRequest.SeatSelectionRequest seatSelection : request.seats()) {
			SeatReservation reservation = findAndValidateTempReservation(session, user, seatSelection);
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

		return buildBookingResponse(savedBooking);
	}

	private SeatReservation findAndValidateTempReservation(Session session, User user,
			BookingCreateRequest.SeatSelectionRequest seatSelection) {

		SeatReservation reservation = seatReservationRepository
				.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(session.getId(), seatSelection.seatId(),
						ReservationStatus.PENDING, user.getId())
				.orElseThrow(
						() -> SeatNotAvailableException.forSeatAndSession(seatSelection.seatId(), session.getId()));

		if (reservation.getReservedUntil().isBefore(LocalDateTime.now())) {
			seatReservationRepository.delete(reservation);
			throw SeatNotAvailableException.forSeatAndSession(seatSelection.seatId(), session.getId());
		}

		return reservation;
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

	private BookingResponse buildBookingResponse(Booking booking) {
		BookingResponse response = bookingMapper.toBookingResponse(booking);
		return new BookingResponse(response.id(), numberGenerator.generateBookingNumber(booking), response.status(),
				response.sessionId(), response.sessionTime(), response.movieTitle(), response.hallName(),
				response.totalPrice(), response.bonusPointsUsed(), response.bonusDiscountAmount(),
				response.finalPrice(), response.liqpayOrderId(), response.expiresAt(), response.createdAt(),
				response.seatReservations());
	}
}