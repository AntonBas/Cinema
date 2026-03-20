package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;

public class BookingMapperTest {

	private BookingMapper bookingMapper = new BookingMapperImpl();
	private Booking booking;

	@BeforeEach
	void setUp() {
		User user = User.builder().id(1L).build();

		Movie movie = Movie.builder().id(1L).title("Inception").build();

		CinemaHall cinemaHall = CinemaHall.builder().id(1L).name("Hall A").build();

		Session session = Session.builder().id(1L).movie(movie).hall(cinemaHall)
				.startTime(LocalDateTime.of(2024, 1, 15, 18, 30)).build();

		Payment payment = Payment.builder().liqpayOrderId("ORDER_ABC123").build();

		Seat seat = Seat.builder().id(1L).row(5).number(12).build();

		TicketType ticketType = TicketType.builder().displayName("Adult").build();

		SeatReservation seatReservation = SeatReservation.builder().id(1L).seat(seat).ticketType(ticketType)
				.seatPrice(new BigDecimal("250.00")).build();

		booking = Booking.builder().id(123L).user(user).session(session).status(BookingStatus.PENDING)
				.totalPrice(new BigDecimal("500.00")).bonusPointsUsed(50).bonusDiscountAmount(new BigDecimal("25.00"))
				.finalPrice(new BigDecimal("475.00")).payment(payment).seatReservations(Arrays.asList(seatReservation))
				.build();
	}

	@Test
	void toBookingResponse() {
		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response.id()).isEqualTo(123L);
		assertThat(response.sessionId()).isEqualTo(1L);
		assertThat(response.movieTitle()).isEqualTo("Inception");
		assertThat(response.hallName()).isEqualTo("Hall A");
		assertThat(response.liqpayOrderId()).isEqualTo("ORDER_ABC123");
		assertThat(response.totalPrice()).isEqualTo(new BigDecimal("500.00"));
	}

	@Test
	void toBookingResponseWithNullSession() {
		booking.setSession(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response.id()).isEqualTo(123L);
		assertThat(response.sessionId()).isNull();
		assertThat(response.movieTitle()).isNull();
		assertThat(response.hallName()).isNull();
	}

	@Test
	void toBookingResponseWithNullPayment() {
		booking.setPayment(null);

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response.id()).isEqualTo(123L);
		assertThat(response.liqpayOrderId()).isNull();
	}

	@Test
	void toBookingResponseWithEmptySeats() {
		booking.setSeatReservations(Arrays.asList());

		BookingResponse response = bookingMapper.toBookingResponse(booking);

		assertThat(response.id()).isEqualTo(123L);
		assertThat(response.seatReservations()).isEmpty();
	}

	@Test
	void toBookingResponseWithNull() {
		BookingResponse response = bookingMapper.toBookingResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toSeatReservationInfo() {
		Seat seat = Seat.builder().id(1L).row(5).number(12).build();
		TicketType ticketType = TicketType.builder().displayName("Adult").build();
		SeatReservation seatReservation = SeatReservation.builder().id(1L).seat(seat).ticketType(ticketType)
				.seatPrice(new BigDecimal("250.00")).build();

		BookingResponse.SeatReservationInfo info = bookingMapper.toSeatReservationInfo(seatReservation);

		assertThat(info.seatId()).isEqualTo(1L);
		assertThat(info.row()).isEqualTo(5);
		assertThat(info.seatNumber()).isEqualTo(12);
		assertThat(info.ticketTypeName()).isEqualTo("Adult");
	}

	@Test
	void toSeatReservationInfoWithNullSeat() {
		TicketType ticketType = TicketType.builder().displayName("Adult").build();
		SeatReservation seatReservation = SeatReservation.builder().id(1L).seat(null).ticketType(ticketType)
				.seatPrice(new BigDecimal("250.00")).build();

		BookingResponse.SeatReservationInfo info = bookingMapper.toSeatReservationInfo(seatReservation);

		assertThat(info.seatId()).isNull();
		assertThat(info.row()).isNull();
		assertThat(info.seatNumber()).isNull();
	}

	@Test
	void toSeatReservationInfoWithNull() {
		BookingResponse.SeatReservationInfo info = bookingMapper.toSeatReservationInfo(null);
		assertThat(info).isNull();
	}
}