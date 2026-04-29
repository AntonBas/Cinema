package ua.lviv.bas.cinema.mapper.ticket;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.ticket.response.TicketCashierResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TicketMapperTest {

    private final TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

    @Test
    void toTicketResponse() {
        Movie movie = Movie.builder().id(1L).title("Inception").build();
        CinemaHall hall = CinemaHall.builder().id(1L).name("Hall A").build();
        Session session = Session.builder().id(1L).movie(movie).hall(hall)
                .startTime(LocalDateTime.of(2024, 1, 15, 18, 30)).build();
        Booking booking = Booking.builder().id(123L).session(session).build();
        TicketType ticketType = TicketType.builder().displayName("Adult Ticket").build();

        Ticket ticket = Ticket.builder().id(456L).booking(booking).uniqueCode("TKT-ABC123").ticketType(ticketType)
                .finalPrice(new BigDecimal("250.00")).status(TicketStatus.ACTIVE)
                .purchaseTime(LocalDateTime.of(2024, 1, 15, 14, 35)).build();

        TicketResponse response = mapper.toTicketResponse(ticket);

        assertThat(response.id()).isEqualTo(456L);
        assertThat(response.ticketCode()).isEqualTo("TKT-ABC123");
        assertThat(response.status()).isEqualTo(TicketStatus.ACTIVE);
        assertThat(response.price()).isEqualTo(new BigDecimal("250.00"));
        assertThat(response.ticketType()).isEqualTo("Adult Ticket");
        assertThat(response.movieTitle()).isEqualTo("Inception");
        assertThat(response.hallName()).isEqualTo("Hall A");
    }

    @Test
    void toTicketCashierResponse() {
        Movie movie = Movie.builder().id(1L).title("Inception").build();
        CinemaHall hall = CinemaHall.builder().id(1L).name("Hall A").build();
        Session session = Session.builder().id(1L).movie(movie).hall(hall)
                .startTime(LocalDateTime.of(2024, 1, 15, 18, 30)).build();
        Booking booking = Booking.builder().id(123L).session(session).build();
        TicketType ticketType = TicketType.builder()
                .displayName("Student")
                .requiresDocument(true)
                .documentType("Student ID")
                .build();
        User user = new User();
        user.setEmail("student@example.com");
        Seat seat = Seat.builder().row(5).number(12).build();
        SeatReservation seatReservation = SeatReservation.builder().seat(seat).build();

        Ticket ticket = Ticket.builder()
                .id(456L).booking(booking).user(user).seatReservation(seatReservation)
                .uniqueCode("TKT-ABC123").ticketType(ticketType)
                .finalPrice(new BigDecimal("150.00")).status(TicketStatus.ACTIVE)
                .build();

        TicketCashierResponse response = mapper.toTicketCashierResponse(ticket);

        assertThat(response.id()).isEqualTo(456L);
        assertThat(response.uniqueCode()).isEqualTo("TKT-ABC123");
        assertThat(response.status()).isEqualTo(TicketStatus.ACTIVE);
        assertThat(response.movieTitle()).isEqualTo("Inception");
        assertThat(response.sessionTime()).isEqualTo(LocalDateTime.of(2024, 1, 15, 18, 30));
        assertThat(response.hallName()).isEqualTo("Hall A");
        assertThat(response.seatRow()).isEqualTo("5");
        assertThat(response.seatNumber()).isEqualTo(12);
        assertThat(response.ticketType()).isEqualTo("Student");
        assertThat(response.requiresDocument()).isTrue();
        assertThat(response.documentType()).isEqualTo("Student ID");
        assertThat(response.userEmail()).isEqualTo("student@example.com");
        assertThat(response.finalPrice()).isEqualTo(new BigDecimal("150.00"));
    }

    @Test
    void toTicketCashierResponseWithoutDocument() {
        Movie movie = Movie.builder().id(1L).title("Inception").build();
        CinemaHall hall = CinemaHall.builder().id(1L).name("Hall A").build();
        Session session = Session.builder().id(1L).movie(movie).hall(hall)
                .startTime(LocalDateTime.of(2024, 1, 15, 18, 30)).build();
        Booking booking = Booking.builder().id(123L).session(session).build();
        TicketType ticketType = TicketType.builder()
                .displayName("Adult").requiresDocument(false).build();
        User user = new User();
        user.setEmail("user@example.com");
        Seat seat = Seat.builder().row(3).number(8).build();
        SeatReservation seatReservation = SeatReservation.builder().seat(seat).build();

        Ticket ticket = Ticket.builder()
                .id(789L).booking(booking).user(user).seatReservation(seatReservation)
                .uniqueCode("TKT-XYZ789").ticketType(ticketType)
                .finalPrice(new BigDecimal("200.00")).status(TicketStatus.ACTIVE)
                .build();

        TicketCashierResponse response = mapper.toTicketCashierResponse(ticket);

        assertThat(response.id()).isEqualTo(789L);
        assertThat(response.uniqueCode()).isEqualTo("TKT-XYZ789");
        assertThat(response.ticketType()).isEqualTo("Adult");
        assertThat(response.requiresDocument()).isFalse();
        assertThat(response.documentType()).isNull();
        assertThat(response.finalPrice()).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    void toTicketCashierResponseWithNull() {
        TicketCashierResponse response = mapper.toTicketCashierResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toTicketResponseWithNullBooking() {
        TicketType ticketType = TicketType.builder().displayName("Adult Ticket").build();
        Ticket ticket = Ticket.builder().id(456L).booking(null).uniqueCode("TKT-ABC123").ticketType(ticketType)
                .finalPrice(new BigDecimal("250.00")).status(TicketStatus.ACTIVE).build();

        TicketResponse response = mapper.toTicketResponse(ticket);

        assertThat(response.id()).isEqualTo(456L);
        assertThat(response.ticketCode()).isEqualTo("TKT-ABC123");
        assertThat(response.price()).isEqualTo(new BigDecimal("250.00"));
        assertThat(response.ticketType()).isEqualTo("Adult Ticket");
        assertThat(response.movieTitle()).isNull();
        assertThat(response.hallName()).isNull();
    }

    @Test
    void toTicketResponseWithNullTicketType() {
        Movie movie = Movie.builder().id(1L).title("Inception").build();
        Session session = Session.builder().id(1L).movie(movie).build();
        Booking booking = Booking.builder().id(123L).session(session).build();
        Ticket ticket = Ticket.builder().id(456L).booking(booking).uniqueCode("TKT-ABC123").ticketType(null)
                .finalPrice(new BigDecimal("250.00")).status(TicketStatus.ACTIVE).build();

        TicketResponse response = mapper.toTicketResponse(ticket);

        assertThat(response.id()).isEqualTo(456L);
        assertThat(response.ticketCode()).isEqualTo("TKT-ABC123");
        assertThat(response.price()).isEqualTo(new BigDecimal("250.00"));
        assertThat(response.ticketType()).isNull();
        assertThat(response.movieTitle()).isEqualTo("Inception");
    }
}