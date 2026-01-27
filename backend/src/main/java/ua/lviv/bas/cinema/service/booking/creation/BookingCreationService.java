package ua.lviv.bas.cinema.service.booking.creation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.BookingMapper;
import ua.lviv.bas.cinema.repository.BookedSeatRepository;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.service.booking.availability.AvailabilityValidator;
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
	private final SeatRepository seatRepository;
	private final TicketTypeRepository ticketTypeRepository;
	private final BookedSeatRepository bookedSeatRepository;
	private final BookingMapper bookingMapper;
	private final AvailabilityValidator availabilityValidator;
	private final BonusService bonusService;
	private final PriceCalculatorService priceCalculator;
	private final NumberGeneratorService numberGenerator;
	private final BookingPriceCalculator bookingPriceCalculator;
	private final BookingValidator bookingValidator;

	@Value("${booking.expiration-minutes:20}")
	private int expirationMinutes;

	@Value("${booking.session-too-close-minutes:30}")
	private int sessionTooCloseMinutes;

	@Value("${booking.max-bonus-points-percentage:30}")
	private int maxBonusPointsPercentage;

	public BookingResponse createBooking(BookingCreateRequest request, User user) {
		Session session = sessionRepository.findById(request.getSessionId())
				.orElseThrow(() -> new SessionNotFoundException(request.getSessionId()));

		bookingValidator.validateSessionForBooking(session);

		List<BookedSeat> bookedSeats = request.getSeats().stream()
				.map(seatSelection -> createBookedSeat(session, user, seatSelection)).collect(Collectors.toList());

		BigDecimal totalPrice = bookedSeats.stream().map(BookedSeat::getSeatPrice).reduce(BigDecimal.ZERO,
				BigDecimal::add);

		BookingPriceCalculator.BookingPriceResult priceResult = bookingPriceCalculator.calculateFinalPrice(totalPrice,
				request.getBonusPointsToUse(), user.getId());

		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);
		Booking booking = createBookingEntity(user, session, bookedSeats, priceResult, expiresAt);

		bookedSeats.forEach(bs -> bs.setBooking(booking));
		Booking savedBooking = bookingRepository.save(booking);
		bookedSeatRepository.saveAll(bookedSeats);

		if (priceResult.bonusPointsUsed() > 0) {
			bonusService.spendBonusPointsForBooking(user.getId(), priceResult.bonusPointsUsed(), savedBooking,
					"BOOKING_" + savedBooking.getId());
		}

		log.info("Created booking {} for user {} with {} bonus points used", savedBooking.getId(), user.getId(),
				priceResult.bonusPointsUsed());

		return buildBookingResponse(savedBooking);
	}

	private BookedSeat createBookedSeat(Session session, User user,
			BookingCreateRequest.SeatSelectionRequest seatSelection) {
		Seat seat = seatRepository.findById(seatSelection.getSeatId())
				.orElseThrow(() -> new SeatNotFoundException(seatSelection.getSeatId()));

		TicketType ticketType = ticketTypeRepository.findById(seatSelection.getTicketTypeId())
				.orElseThrow(() -> new TicketTypeNotFoundException(seatSelection.getTicketTypeId()));

		availabilityValidator.validateSeat(session.getId(), seat.getId());

		BigDecimal seatPrice = priceCalculator.calculateSeatPrice(session, seat, ticketType);

		return BookedSeat.builder().seat(seat).session(session).ticketType(ticketType).seatPrice(seatPrice)
				.status(BookedSeatStatus.PENDING).bookedAt(LocalDateTime.now())
				.reservedUntil(LocalDateTime.now().plusMinutes(expirationMinutes)).reservedByUser(user).build();
	}

	private Booking createBookingEntity(User user, Session session, List<BookedSeat> bookedSeats,
			BookingPriceCalculator.BookingPriceResult priceResult, LocalDateTime expiresAt) {
		return Booking.builder().user(user).session(session).status(BookingStatus.PENDING)
				.totalPrice(priceResult.totalPrice()).bonusPointsUsed(priceResult.bonusPointsUsed())
				.bonusDiscountAmount(priceResult.bonusDiscount()).finalPrice(priceResult.finalPrice())
				.expiresAt(expiresAt).bookedSeats(bookedSeats).build();
	}

	private BookingResponse buildBookingResponse(Booking booking) {
		BookingResponse response = bookingMapper.toBookingResponse(booking);
		response.setBookingNumber(numberGenerator.generateBookingNumber(booking));
		return response;
	}
}