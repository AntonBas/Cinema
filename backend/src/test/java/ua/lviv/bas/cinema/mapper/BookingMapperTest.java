package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
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

		Movie movie = Movie.builder().id(1L).title("Inception").build();

		CinemaHall cinemaHall = CinemaHall.builder().id(1L).name("Hall A").build();

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

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(123L);
		assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING);
		assertThat(response.getSessionId()).isEqualTo(1L);
		assertThat(response.getSessionTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 18, 30));
		assertThat(response.getMovieTitle()).isEqualTo("Inception");
		assertThat(response.getHallName()).isEqualTo("Hall A");
		assertThat(response.getTotalPrice()).isEqualTo(new BigDecimal("500.00"));
		assertThat(response.getBonusPointsUsed()).isEqualTo(50);
		assertThat(response.getBonusDiscountAmount()).isEqualTo(new BigDecimal("25.00"));
		assertThat(response.getFinalPrice()).isEqualTo(new BigDecimal("475.00"));
		assertThat(response.getLiqpayOrderId()).isEqualTo("ORDER_ABC123");
		assertThat(response.getExpiresAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 50));
		assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30));

		assertThat(response.getBookedSeats()).hasSize(2);

		BookingResponse.BookedSeatInfo seatInfo1 = response.getBookedSeats().get(0);
		assertThat(seatInfo1.getId()).isEqualTo(1L);
		assertThat(seatInfo1.getSeatId()).isEqualTo(1L);
		assertThat(seatInfo1.getRow()).isEqualTo(5);
		assertThat(seatInfo1.getSeatNumber()).isEqualTo(12);
		assertThat(seatInfo1.getTicketTypeName()).isEqualTo("Adult");
		assertThat(seatInfo1.getSeatPrice()).isEqualTo(new BigDecimal("250.00"));

		BookingResponse.BookedSeatInfo seatInfo2 = response.getBookedSeats().get(1);
		assertThat(seatInfo2.getId()).isEqualTo(2L);
		assertThat(seatInfo2.getSeatId()).isEqualTo(2L);
		assertThat(seatInfo2.getRow()).isEqualTo(5);
		assertThat(seatInfo2.getSeatNumber()).isEqualTo(13);
		assertThat(seatInfo2.getTicketTypeName()).isEqualTo("Adult");
		assertThat(seatInfo2.getSeatPrice()).isEqualTo(new BigDecimal("250.00"));
	}

	@Test
	void toBookingResponse_ShouldHandleNullPayment() {
		booking.setPayment(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response).isNotNull();
		assertThat(response.getLiqpayOrderId()).isNull();
		assertThat(response.getId()).isEqualTo(123L);
		assertThat(response.getMovieTitle()).isEqualTo("Inception");
	}

	@Test
	void toBookingResponse_ShouldHandleEmptyBookedSeats() {
		booking.setBookedSeats(Arrays.asList());

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response).isNotNull();
		assertThat(response.getBookedSeats()).isNotNull().isEmpty();
		assertThat(response.getId()).isEqualTo(123L);
	}

	@Test
	void toBookingResponse_ShouldReturnNull_WhenBookingIsNull() {
		BookingResponse response = bookingMapper.toBookingResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toBookingResponse_ShouldHandleDifferentBookingStatuses() {
		booking.setStatus(BookingStatus.CONFIRMED);
		BookingResponse response1 = bookingMapper.toBookingResponse(booking);
		assertThat(response1.getStatus()).isEqualTo(BookingStatus.CONFIRMED);

		booking.setStatus(BookingStatus.CANCELLED);
		BookingResponse response2 = bookingMapper.toBookingResponse(booking);
		assertThat(response2.getStatus()).isEqualTo(BookingStatus.CANCELLED);

		booking.setStatus(BookingStatus.EXPIRED);
		BookingResponse response3 = bookingMapper.toBookingResponse(booking);
		assertThat(response3.getStatus()).isEqualTo(BookingStatus.EXPIRED);
	}

	@Test
	void toBookingResponse_ShouldHandleNullSession() {
		booking.setSession(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(123L);
		assertThat(response.getSessionTime()).isNull();
		assertThat(response.getMovieTitle()).isNull();
		assertThat(response.getHallName()).isNull();
		assertThat(response.getSessionId()).isNull();
		assertThat(response.getStatus()).isEqualTo(BookingStatus.PENDING);
	}

	@Test
	void toBookingResponse_ShouldHandleSessionWithoutMovie() {
		session.setMovie(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(123L);
		assertThat(response.getSessionTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 18, 30));
		assertThat(response.getMovieTitle()).isNull();
		assertThat(response.getHallName()).isEqualTo("Hall A");
	}

	@Test
	void toBookingResponse_ShouldHandleSessionWithoutHall() {
		session.setHall(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(123L);
		assertThat(response.getSessionTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 18, 30));
		assertThat(response.getMovieTitle()).isEqualTo("Inception");
		assertThat(response.getHallName()).isNull();
	}

	@Test
	void toBookingResponse_ShouldHandleZeroBonusPoints() {
		booking.setBonusPointsUsed(0);
		booking.setBonusDiscountAmount(BigDecimal.ZERO);
		booking.setFinalPrice(new BigDecimal("500.00"));

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response).isNotNull();
		assertThat(response.getBonusPointsUsed()).isEqualTo(0);
		assertThat(response.getBonusDiscountAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(response.getFinalPrice()).isEqualTo(new BigDecimal("500.00"));
	}

	@Test
	void toBookingResponse_ShouldHandleNullBookedSeatProperties() {
		bookedSeat1.setSeat(null);
		bookedSeat1.setTicketType(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response).isNotNull();
		assertThat(response.getBookedSeats()).hasSize(2);

		BookingResponse.BookedSeatInfo seatInfo1 = response.getBookedSeats().get(0);
		assertThat(seatInfo1.getSeatId()).isNull();
		assertThat(seatInfo1.getRow()).isNull();
		assertThat(seatInfo1.getSeatNumber()).isNull();
		assertThat(seatInfo1.getTicketTypeName()).isNull();
		assertThat(seatInfo1.getSeatPrice()).isEqualTo(new BigDecimal("250.00"));
	}
}