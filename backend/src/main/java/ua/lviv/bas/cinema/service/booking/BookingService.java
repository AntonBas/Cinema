package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.mapper.BookingMapper;
import ua.lviv.bas.cinema.repository.BookedSeatRepository;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.service.user.BonusService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {
	private final BookingRepository bookingRepository;
	private final SessionRepository sessionRepository;
	private final SeatRepository seatRepository;
	private final TicketTypeRepository ticketTypeRepository;
	private final BookedSeatRepository bookedSeatRepository;
	private final BookingMapper bookingMapper;
	private final SeatAvailabilityService seatAvailabilityService;
	private final BonusService bonusService;

	public BookingResponse createBooking(BookingCreateRequest request, User user) {
		Session session = sessionRepository.findById(request.getSessionId())
				.orElseThrow(() -> new SessionNotFoundException(request.getSessionId()));

		validateSessionForBooking(session);

		List<BookedSeat> bookedSeats = new ArrayList<>();
		BigDecimal totalPrice = BigDecimal.ZERO;

		for (BookingCreateRequest.SeatSelectionRequest seatSelection : request.getSeats()) {
			Seat seat = seatRepository.findById(seatSelection.getSeatId())
					.orElseThrow(() -> new ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException(
							seatSelection.getSeatId()));

			TicketType ticketType = ticketTypeRepository.findById(seatSelection.getTicketTypeId())
					.orElseThrow(() -> new IllegalArgumentException("Ticket type not found"));

			seatAvailabilityService.validateSeatAvailability(session.getId(), seat.getId());

			BigDecimal seatPrice = seatAvailabilityService.calculateSeatPrice(session, seat, ticketType);
			totalPrice = totalPrice.add(seatPrice);

			BookedSeat bookedSeat = BookedSeat.builder().seat(seat).session(session).ticketType(ticketType)
					.seatPrice(seatPrice).status(BookedSeatStatus.PENDING).bookedAt(LocalDateTime.now())
					.reservedUntil(LocalDateTime.now().plusMinutes(20)).reservedByUser(user).build();

			bookedSeats.add(bookedSeat);
		}

		if (request.getBonusPointsToUse() > 0) {
			bonusService.validatePointsRedemption(user.getId(), request.getBonusPointsToUse());
		}

		BigDecimal bonusDiscount = calculateBonusDiscount(request.getBonusPointsToUse());
		BigDecimal finalPrice = totalPrice.subtract(bonusDiscount).max(BigDecimal.ZERO);

		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(20);
		Booking booking = Booking.builder().user(user).session(session).status(BookingStatus.PENDING)
				.totalPrice(totalPrice).bonusPointsUsed(request.getBonusPointsToUse())
				.bonusDiscountAmount(bonusDiscount).finalPrice(finalPrice).expiresAt(expiresAt).bookedSeats(bookedSeats)
				.build();

		bookedSeats.forEach(bs -> bs.setBooking(booking));
		Booking savedBooking = bookingRepository.save(booking);
		bookedSeatRepository.saveAll(bookedSeats);

		return buildBookingResponse(savedBooking);
	}

	@Transactional(readOnly = true)
	public BookingResponse getBookingById(Long bookingId, User user) {
		Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
				.orElseThrow(() -> new BookingNotFoundException(bookingId));
		return buildBookingResponse(booking);
	}

	@Transactional(readOnly = true)
	public BookingResponse getBookingById(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException(bookingId));
		return buildBookingResponse(booking);
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> getAllBookings() {
		return bookingRepository.findAll().stream().map(this::buildBookingResponse).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Page<BookingResponse> getAllBookings(Pageable pageable, BookingStatus status) {
		Page<Booking> bookings;
		if (status != null) {
			bookings = bookingRepository.findByStatus(status, pageable);
		} else {
			bookings = bookingRepository.findAll(pageable);
		}
		return bookings.map(this::buildBookingResponse);
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> getSessionBookings(Long sessionId) {
		return bookingRepository.findBySessionId(sessionId).stream().map(this::buildBookingResponse)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<BookingResponse> getUserBookings(Long userId, BookingStatus status) {
		List<Booking> bookings;
		if (status != null) {
			bookings = bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status);
		} else {
			bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId);
		}
		return bookings.stream().map(this::buildBookingResponse).collect(Collectors.toList());
	}

	public void cancelBooking(Long bookingId, User user) {
		Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
				.orElseThrow(() -> new BookingNotFoundException(bookingId));

		if (!canBookingBeCancelled(booking)) {
			throw BookingValidationException.cannotCancel();
		}

		booking.setStatus(BookingStatus.CANCELLED);
		booking.getBookedSeats().forEach(bs -> bs.setStatus(BookedSeatStatus.CANCELLED));
		bookingRepository.save(booking);
	}

	public void cancelBooking(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException(bookingId));

		if (!canBookingBeCancelled(booking)) {
			throw BookingValidationException.cannotCancel();
		}

		booking.setStatus(BookingStatus.CANCELLED);
		booking.getBookedSeats().forEach(bs -> bs.setStatus(BookedSeatStatus.CANCELLED));
		bookingRepository.save(booking);
	}

	public void confirmBooking(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException(bookingId));

		if (booking.getStatus() != BookingStatus.PENDING) {
			throw new IllegalStateException("Only pending bookings can be confirmed");
		}

		booking.setStatus(BookingStatus.CONFIRMED);
		booking.getBookedSeats().forEach(bs -> bs.setStatus(BookedSeatStatus.CONFIRMED));
		bookingRepository.save(booking);
	}

	public void expireBooking(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException(bookingId));

		if (booking.getStatus() == BookingStatus.PENDING) {
			booking.setStatus(BookingStatus.EXPIRED);
			booking.getBookedSeats().forEach(bs -> bs.setStatus(BookedSeatStatus.EXPIRED));
			bookingRepository.save(booking);
		}
	}

	public BigDecimal calculateTotalPrice(Long bookingId, User user) {
		Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
				.orElseThrow(() -> new BookingNotFoundException(bookingId));
		return booking.getTotalPrice();
	}

	public String generateBookingNumber(Booking booking) {
		return String.format("BK-%d-%05d", booking.getCreatedAt().getYear(), booking.getId());
	}

	private void validateSessionForBooking(Session session) {
		if (session.getStatus() != CinemaSessionStatus.SCHEDULED) {
			throw BookingValidationException.sessionNotAvailable();
		}

		if (session.getStartTime().isBefore(LocalDateTime.now())) {
			throw BookingValidationException.sessionAlreadyStarted();
		}

		if (session.getStartTime().isBefore(LocalDateTime.now().plusMinutes(30))) {
			throw BookingValidationException.sessionTooClose();
		}
	}

	private boolean canBookingBeCancelled(Booking booking) {
		return booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED;
	}

	private BookingResponse buildBookingResponse(Booking booking) {
		BookingResponse response = bookingMapper.toBookingResponse(booking);
		response.setBookingNumber(generateBookingNumber(booking));
		return response;
	}

	private BigDecimal calculateBonusDiscount(Integer bonusPoints) {
		if (bonusPoints == null || bonusPoints == 0) {
			return BigDecimal.ZERO;
		}
		return new BigDecimal("1.00").multiply(BigDecimal.valueOf(bonusPoints));
	}
}