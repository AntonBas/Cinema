package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
import ua.lviv.bas.cinema.dto.booking.response.BookingSummaryResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.mapper.BookingMapper;
import ua.lviv.bas.cinema.repository.BookedSeatRepository;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;

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

	public BookingResponse createBooking(BookingCreateRequest request, User user) {
		Session session = sessionRepository.findById(request.getSessionId())
				.orElseThrow(() -> new SessionNotFoundException(request.getSessionId()));

		validateSessionForBooking(session);

		List<BookedSeat> bookedSeats = new ArrayList<>();
		BigDecimal totalPrice = BigDecimal.ZERO;
		StringBuilder seatInfoBuilder = new StringBuilder();

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
					.status(BookedSeatStatus.PENDING).build();

			bookedSeats.add(bookedSeat);

			if (seatInfoBuilder.length() > 0)
				seatInfoBuilder.append(", ");
			seatInfoBuilder.append(
					String.format("Row %d Seat %d (%s)", seat.getRow(), seat.getNumber(), ticketType.getDisplayName()));
		}

		LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
		Booking booking = Booking.builder().user(user).session(session).status(BookingStatus.PENDING)
				.expiresAt(expiresAt).bookedSeats(bookedSeats).build();

		bookedSeats.forEach(bs -> bs.setBooking(booking));

		Booking savedBooking = bookingRepository.save(booking);
		bookedSeatRepository.saveAll(bookedSeats);

		return buildBookingResponse(savedBooking, totalPrice);
	}

	@Transactional(readOnly = true)
	public BookingResponse getBookingById(Long bookingId, User user) {
		Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
				.orElseThrow(() -> new BookingNotFoundException(bookingId));

		BigDecimal totalPrice = calculateTotalPrice(booking);
		return buildBookingResponse(booking, totalPrice);
	}

	@Transactional(readOnly = true)
	public List<BookingSummaryResponse> getUserBookings(User user, BookingStatus status) {
		List<Booking> bookings;
		if (status != null) {
			bookings = bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(user.getId(), status);
		} else {
			bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
		}

		return bookings.stream().map(booking -> {
			BigDecimal totalPrice = calculateTotalPrice(booking);
			boolean canCancel = canBookingBeCancelled(booking);
			return buildBookingSummaryResponse(booking, totalPrice, canCancel);
		}).toList();
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

	public String generateBookingNumber(Booking booking) {
		return String.format("BK-%d-%05d", booking.getCreatedAt().getYear(), booking.getId());
	}

	public String formatDateTime(LocalDateTime dateTime) {
		return dateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
	}

	public BigDecimal calculateTotalPrice(Booking booking) {
		return booking.getBookedSeats().stream().map(
				bs -> seatAvailabilityService.calculateSeatPrice(bs.getSession(), bs.getSeat(), bs.getTicketType()))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
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

	private BookingResponse buildBookingResponse(Booking booking, BigDecimal totalPrice) {
		BookingResponse response = bookingMapper.toBookingResponse(booking);
		response.setBookingNumber(generateBookingNumber(booking));
		response.setTotalPrice(totalPrice);
		return response;
	}

	private BookingSummaryResponse buildBookingSummaryResponse(Booking booking, BigDecimal totalPrice,
			boolean canCancel) {
		BookingSummaryResponse response = bookingMapper.toBookingSummaryResponse(booking);
		response.setBookingNumber(generateBookingNumber(booking));
		response.setTotalPrice(totalPrice);
		response.setCanCancel(canCancel);
		return response;
	}
}