package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;

@ExtendWith(MockitoExtension.class)
public class BookingMapperTest {

	private BookingMapper bookingMapper = new BookingMapperImpl();

	private Booking booking;
	private BookedSeat bookedSeat1;
	private BookedSeat bookedSeat2;
	private Session session;
	private Payment payment;

	@BeforeEach
	void setUp() {
		User user = User.builder().id(1L).email("user@example.com").firstName("John").lastName("Doe").build();

		ua.lviv.bas.cinema.domain.Movie movie = ua.lviv.bas.cinema.domain.Movie.builder().id(1L).title("Inception")
				.build();

		ua.lviv.bas.cinema.domain.CinemaHall cinemaHall = ua.lviv.bas.cinema.domain.CinemaHall.builder().id(1L)
				.name("Hall A").build();

		session = Session.builder().id(1L).movie(movie).hall(cinemaHall)
				.startTime(LocalDateTime.of(2024, 1, 15, 18, 30)).build();

		payment = Payment.builder().id(1L).status(PaymentStatus.PENDING).liqpayOrderId("ORDER_ABC123").build();

		Seat seat1 = Seat.builder().id(1L).row(5).number(12).seatType(SeatType.STANDARD).build();

		Seat seat2 = Seat.builder().id(2L).row(5).number(13).seatType(SeatType.STANDARD).build();

		TicketType adultTicket = TicketType.builder().id(1L).code("ADULT").displayName("Adult")
				.priceMultiplier(new BigDecimal("1.0")).build();

		bookedSeat1 = BookedSeat.builder().id(1L).seat(seat1).ticketType(adultTicket)
				.seatPrice(new BigDecimal("250.00")).status(BookedSeatStatus.PENDING).build();

		bookedSeat2 = BookedSeat.builder().id(2L).seat(seat2).ticketType(adultTicket)
				.seatPrice(new BigDecimal("250.00")).status(BookedSeatStatus.PENDING).build();

		LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 14, 30);
		LocalDateTime expiresAt = LocalDateTime.of(2024, 1, 15, 14, 50);

		booking = Booking.builder().id(123L).user(user).session(session).status(BookingStatus.PENDING)
				.totalPrice(new BigDecimal("500.00")).bonusPointsUsed(50).bonusDiscountAmount(new BigDecimal("25.00"))
				.finalPrice(new BigDecimal("475.00")).expiresAt(expiresAt).createdAt(createdAt)
				.bookedSeats(Arrays.asList(bookedSeat1, bookedSeat2)).payment(payment).build();

		bookedSeat1.setBooking(booking);
		bookedSeat2.setBooking(booking);
	}

	@Test
	void toBookingResponse_ShouldMapAllFieldsCorrectly() {
		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertNotNull(response);
		assertEquals(123L, response.getId());
		assertEquals(BookingStatus.PENDING, response.getStatus());
		assertEquals(LocalDateTime.of(2024, 1, 15, 18, 30), response.getSessionTime());
		assertEquals("Inception", response.getMovieTitle());
		assertEquals("Hall A", response.getHallName());
		assertEquals(new BigDecimal("500.00"), response.getTotalPrice());
		assertEquals(50, response.getBonusPointsUsed());
		assertEquals(new BigDecimal("25.00"), response.getBonusDiscountAmount());
		assertEquals(new BigDecimal("475.00"), response.getFinalPrice());
		assertEquals(PaymentStatus.PENDING, response.getPaymentStatus());
		assertEquals("ORDER_ABC123", response.getLiqpayOrderId());
		assertEquals(LocalDateTime.of(2024, 1, 15, 14, 50), response.getExpiresAt());
		assertEquals(LocalDateTime.of(2024, 1, 15, 14, 30), response.getCreatedAt());

		assertNotNull(response.getBookedSeats());
		assertEquals(2, response.getBookedSeats().size());

		assertNull(response.getBookingNumber());
	}

	@Test
	void toBookingResponse_ShouldHandleNullPayment() {
		booking.setPayment(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertNotNull(response);
		assertNull(response.getPaymentStatus());
		assertNull(response.getLiqpayOrderId());
		assertEquals(123L, response.getId());
		assertEquals("Inception", response.getMovieTitle());
	}

	@Test
	void toBookingResponse_ShouldHandleEmptyBookedSeats() {
		booking.setBookedSeats(Arrays.asList());

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertNotNull(response);
		assertNotNull(response.getBookedSeats());
		assertTrue(response.getBookedSeats().isEmpty());
		assertEquals(123L, response.getId());
	}

	@Test
	void toBookingResponse_ShouldReturnNull_WhenBookingIsNull() {
		BookingResponse response = bookingMapper.toBookingResponse(null);
		assertNull(response);
	}

	@Test
	void toBookingResponse_ShouldHandleDifferentBookingStatuses() {
		booking.setStatus(BookingStatus.CONFIRMED);
		BookingResponse response1 = bookingMapper.toBookingResponse(booking);
		assertEquals(BookingStatus.CONFIRMED, response1.getStatus());

		booking.setStatus(BookingStatus.CANCELLED);
		BookingResponse response2 = bookingMapper.toBookingResponse(booking);
		assertEquals(BookingStatus.CANCELLED, response2.getStatus());

		booking.setStatus(BookingStatus.EXPIRED);
		BookingResponse response3 = bookingMapper.toBookingResponse(booking);
		assertEquals(BookingStatus.EXPIRED, response3.getStatus());
	}

	@Test
	void toBookingResponse_ShouldHandleDifferentPaymentStatuses() {
		payment.setStatus(PaymentStatus.SUCCESS);
		BookingResponse response1 = bookingMapper.toBookingResponse(booking);
		assertEquals(PaymentStatus.SUCCESS, response1.getPaymentStatus());

		payment.setStatus(PaymentStatus.FAILED);
		BookingResponse response2 = bookingMapper.toBookingResponse(booking);
		assertEquals(PaymentStatus.FAILED, response2.getPaymentStatus());

		payment.setStatus(PaymentStatus.PROCESSING);
		BookingResponse response3 = bookingMapper.toBookingResponse(booking);
		assertEquals(PaymentStatus.PROCESSING, response3.getPaymentStatus());
	}

	@Test
	void toBookingResponse_ShouldHandleNullSession() {
		booking.setSession(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertNotNull(response);
		assertEquals(123L, response.getId());
		assertNull(response.getSessionTime());
		assertNull(response.getMovieTitle());
		assertNull(response.getHallName());
		assertEquals(BookingStatus.PENDING, response.getStatus());
	}

	@Test
	void toBookingResponse_ShouldHandleSessionWithoutMovie() {
		session.setMovie(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertNotNull(response);
		assertEquals(123L, response.getId());
		assertEquals(LocalDateTime.of(2024, 1, 15, 18, 30), response.getSessionTime());
		assertNull(response.getMovieTitle());
		assertEquals("Hall A", response.getHallName());
	}

	@Test
	void toBookingResponse_ShouldHandleSessionWithoutHall() {
		session.setHall(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertNotNull(response);
		assertEquals(123L, response.getId());
		assertEquals(LocalDateTime.of(2024, 1, 15, 18, 30), response.getSessionTime());
		assertEquals("Inception", response.getMovieTitle());
		assertNull(response.getHallName());
	}

	@Test
	void toBookingResponse_ShouldHandleZeroBonusPoints() {
		booking.setBonusPointsUsed(0);
		booking.setBonusDiscountAmount(BigDecimal.ZERO);
		booking.setFinalPrice(new BigDecimal("500.00"));

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertNotNull(response);
		assertEquals(0, response.getBonusPointsUsed());
		assertEquals(BigDecimal.ZERO, response.getBonusDiscountAmount());
		assertEquals(new BigDecimal("500.00"), response.getFinalPrice());
	}
}