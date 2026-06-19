package ua.lviv.bas.cinema.mapper.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class BookingMapperTest {

    private final BookingMapper bookingMapper = new BookingMapperImpl();
    private Booking booking;

    @BeforeEach
    void setUp() {
        var user = User.builder().id(1L).build();
        var movie = Movie.builder().id(1L).title("Inception").build();
        var cinemaHall = CinemaHall.builder().id(1L).name("Hall A").build();
        var session = Session.builder().id(1L).movie(movie).hall(cinemaHall)
                .startTime(LocalDateTime.of(2024, 1, 15, 18, 30)).build();
        var payment = Payment.builder().liqpayOrderId("ORDER_ABC123").build();
        var seat = Seat.builder().id(1L).row(5).number(12).build();
        var ticketType = TicketType.builder().displayName("Adult").build();
        var seatReservation = SeatReservation.builder().id(1L).seat(seat).ticketType(ticketType)
                .seatPrice(new BigDecimal("250.00")).build();

        booking = Booking.builder().id(123L).user(user).session(session).status(BookingStatus.PENDING)
                .totalPrice(new BigDecimal("500.00")).bonusPointsUsed(50).bonusDiscountAmount(new BigDecimal("25.00"))
                .finalPrice(new BigDecimal("475.00")).payment(payment).seatReservations(List.of(seatReservation))
                .build();

        booking.setCreatedDate(LocalDateTime.of(2024, 1, 15, 14, 30));
    }

    @Test
    void toResponse() {
        var response = bookingMapper.toResponse(booking);

        assertThat(response.id()).isEqualTo(123L);
        assertThat(response.bookingNumber()).isEqualTo("BK-2024-00123");
        assertThat(response.sessionId()).isEqualTo(1L);
        assertThat(response.movieTitle()).isEqualTo("Inception");
        assertThat(response.hallName()).isEqualTo("Hall A");
        assertThat(response.liqpayOrderId()).isEqualTo("ORDER_ABC123");
        assertThat(response.totalPrice()).isEqualTo(new BigDecimal("500.00"));
        assertThat(response.bonusPointsUsed()).isEqualTo(50);
        assertThat(response.bonusDiscountAmount()).isEqualTo(new BigDecimal("25.00"));
        assertThat(response.finalPrice()).isEqualTo(new BigDecimal("475.00"));
        assertThat(response.status()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void toResponseWithNullSession() {
        booking.setSession(null);
        var response = bookingMapper.toResponse(booking);

        assertThat(response.id()).isEqualTo(123L);
        assertThat(response.bookingNumber()).isEqualTo("BK-2024-00123");
        assertThat(response.sessionId()).isNull();
        assertThat(response.movieTitle()).isNull();
        assertThat(response.hallName()).isNull();
        assertThat(response.sessionTime()).isNull();
    }

    @Test
    void toResponseWithNullPayment() {
        booking.setPayment(null);
        var response = bookingMapper.toResponse(booking);

        assertThat(response.id()).isEqualTo(123L);
        assertThat(response.bookingNumber()).isEqualTo("BK-2024-00123");
        assertThat(response.liqpayOrderId()).isNull();
    }

    @Test
    void toResponseWithEmptySeats() {
        booking.setSeatReservations(List.of());
        var response = bookingMapper.toResponse(booking);

        assertThat(response.id()).isEqualTo(123L);
        assertThat(response.bookingNumber()).isEqualTo("BK-2024-00123");
        assertThat(response.seatReservations()).isEmpty();
    }

    @Test
    void toResponseWithNull() {
        var response = bookingMapper.toResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toSeatReservationInfo() {
        var seat = Seat.builder().id(1L).row(5).number(12).build();
        var ticketType = TicketType.builder().displayName("Adult").build();
        var seatReservation = SeatReservation.builder().id(1L).seat(seat).ticketType(ticketType)
                .seatPrice(new BigDecimal("250.00")).build();

        var info = bookingMapper.toSeatReservationInfo(seatReservation);

        assertThat(info.seatId()).isEqualTo(1L);
        assertThat(info.row()).isEqualTo(5);
        assertThat(info.seatNumber()).isEqualTo(12);
        assertThat(info.ticketTypeName()).isEqualTo("Adult");
        assertThat(info.seatPrice()).isEqualTo(new BigDecimal("250.00"));
    }

    @Test
    void toSeatReservationInfoWithNullSeat() {
        var ticketType = TicketType.builder().displayName("Adult").build();
        var seatReservation = SeatReservation.builder().id(1L).seat(null).ticketType(ticketType)
                .seatPrice(new BigDecimal("250.00")).build();

        var info = bookingMapper.toSeatReservationInfo(seatReservation);

        assertThat(info.seatId()).isNull();
        assertThat(info.row()).isNull();
        assertThat(info.seatNumber()).isNull();
        assertThat(info.ticketTypeName()).isEqualTo("Adult");
    }

    @Test
    void toSeatReservationInfoWithNull() {
        var info = bookingMapper.toSeatReservationInfo(null);
        assertThat(info).isNull();
    }
}