package ua.lviv.bas.cinema.mapper.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SeatReservationMapperTest {

    private final SeatReservationMapper seatReservationMapper = new SeatReservationMapperImpl();
    private Session session;
    private Seat seat1;
    private TicketType adultTicketType;
    private List<SeatReservationResponse.SeatInfo> seatInfos;
    private List<SeatReservationResponse.TicketPriceInfo> ticketPrices1;

    @BeforeEach
    void setUp() {
        var movie = Movie.builder().id(1L).title("Inception").build();
        var hall = CinemaHall.builder().id(1L).name("Hall A").build();
        session = Session.builder().id(1L).movie(movie).hall(hall).basePrice(new BigDecimal("250.00")).build();

        seat1 = Seat.builder().id(1L).row(5).number(12).seatType(SeatType.STANDARD).active(true).build();
        Seat.builder().id(2L).row(5).number(13).seatType(SeatType.VIP).active(true).build();

        adultTicketType = TicketType.builder().id(1L).displayName("Adult").build();
        TicketType.builder().id(2L).displayName("Child").build();

        ticketPrices1 = List.of(
                new SeatReservationResponse.TicketPriceInfo(1L, "Adult", new BigDecimal("250.00"), null, null, false,
                        null),
                new SeatReservationResponse.TicketPriceInfo(2L, "Child", new BigDecimal("200.00"), null, null, false,
                        null));

        var ticketPrices2 = List.of(
                new SeatReservationResponse.TicketPriceInfo(1L, "Adult", new BigDecimal("350.00"), null, null, false,
                        null),
                new SeatReservationResponse.TicketPriceInfo(2L, "Child", new BigDecimal("280.00"), null, null, false,
                        null));

        var seatInfo1 = new SeatReservationResponse.SeatInfo(1L, 5, 12, SeatType.STANDARD, true, false, true,
                ticketPrices1);
        var seatInfo2 = new SeatReservationResponse.SeatInfo(2L, 5, 13, SeatType.VIP, true, false, true, ticketPrices2);

        seatInfos = List.of(seatInfo1, seatInfo2);
    }

    @Test
    void toResponse() {
        var response = seatReservationMapper.toResponse(session, seatInfos, 2);

        assertThat(response).isNotNull();
        assertThat(response.sessionId()).isEqualTo(1L);
        assertThat(response.movieTitle()).isEqualTo("Inception");
        assertThat(response.basePrice()).isEqualTo(new BigDecimal("250.00"));
        assertThat(response.hallName()).isEqualTo("Hall A");
        assertThat(response.availableSeats()).isEqualTo(2);
        assertThat(response.seats()).hasSize(2);
    }

    @Test
    void toResponseWithEmptySeats() {
        var response = seatReservationMapper.toResponse(session, List.of(), 0);

        assertThat(response).isNotNull();
        assertThat(response.sessionId()).isEqualTo(1L);
        assertThat(response.seats()).isEmpty();
        assertThat(response.availableSeats()).isEqualTo(0);
    }

    @Test
    void toResponseWithNullSession() {
        var response = seatReservationMapper.toResponse(null, seatInfos, 2);

        assertThat(response).isNotNull();
        assertThat(response.sessionId()).isNull();
        assertThat(response.movieTitle()).isNull();
        assertThat(response.basePrice()).isNull();
        assertThat(response.hallName()).isNull();
        assertThat(response.availableSeats()).isEqualTo(2);
        assertThat(response.seats()).hasSize(2);
    }

    @Test
    void toSeatInfo() {
        var seatInfo = seatReservationMapper.toSeatInfo(seat1, true, false, ticketPrices1);

        assertThat(seatInfo).isNotNull();
        assertThat(seatInfo.id()).isEqualTo(1L);
        assertThat(seatInfo.row()).isEqualTo(5);
        assertThat(seatInfo.seatNumber()).isEqualTo(12);
        assertThat(seatInfo.seatType()).isEqualTo(SeatType.STANDARD);
        assertThat(seatInfo.available()).isTrue();
        assertThat(seatInfo.temporarilyReserved()).isFalse();
        assertThat(seatInfo.active()).isTrue();
        assertThat(seatInfo.ticketPrices()).hasSize(2);
        assertThat(seatInfo.ticketPrices().getFirst().ticketTypeId()).isEqualTo(1L);
        assertThat(seatInfo.ticketPrices().getFirst().finalPrice()).isEqualTo(new BigDecimal("250.00"));
    }

    @Test
    void toSeatInfoUnavailable() {
        var seatInfo = seatReservationMapper.toSeatInfo(seat1, false, true, ticketPrices1);

        assertThat(seatInfo).isNotNull();
        assertThat(seatInfo.available()).isFalse();
        assertThat(seatInfo.temporarilyReserved()).isTrue();
    }

    @Test
    void toTicketPriceInfo() {
        var price = new BigDecimal("250.00");
        var ticketPriceInfo = seatReservationMapper.toTicketPriceInfo(adultTicketType, price);

        assertThat(ticketPriceInfo).isNotNull();
        assertThat(ticketPriceInfo.ticketTypeId()).isEqualTo(1L);
        assertThat(ticketPriceInfo.ticketTypeName()).isEqualTo("Adult");
        assertThat(ticketPriceInfo.finalPrice()).isEqualTo(new BigDecimal("250.00"));
        assertThat(ticketPriceInfo.minAge()).isNull();
        assertThat(ticketPriceInfo.maxAge()).isNull();
        assertThat(ticketPriceInfo.requiresDocument()).isFalse();
        assertThat(ticketPriceInfo.documentType()).isNull();
    }

    @Test
    void toTicketPriceInfoWithNullTicketType() {
        var price = new BigDecimal("250.00");
        var ticketPriceInfo = seatReservationMapper.toTicketPriceInfo(null, price);

        assertThat(ticketPriceInfo).isNotNull();
        assertThat(ticketPriceInfo.ticketTypeId()).isNull();
        assertThat(ticketPriceInfo.ticketTypeName()).isNull();
        assertThat(ticketPriceInfo.finalPrice()).isEqualTo(new BigDecimal("250.00"));
        assertThat(ticketPriceInfo.minAge()).isNull();
        assertThat(ticketPriceInfo.maxAge()).isNull();
        assertThat(ticketPriceInfo.requiresDocument()).isFalse();
        assertThat(ticketPriceInfo.documentType()).isNull();
    }

    @Test
    void toTicketPriceInfoWithNullPrice() {
        var ticketPriceInfo = seatReservationMapper.toTicketPriceInfo(adultTicketType, null);

        assertThat(ticketPriceInfo).isNotNull();
        assertThat(ticketPriceInfo.ticketTypeId()).isEqualTo(1L);
        assertThat(ticketPriceInfo.ticketTypeName()).isEqualTo("Adult");
        assertThat(ticketPriceInfo.finalPrice()).isNull();
    }

    @Test
    void toTicketPriceInfoWithNullTicketTypeAndNullPrice() {
        var ticketPriceInfo = seatReservationMapper.toTicketPriceInfo(null, null);
        assertThat(ticketPriceInfo).isNull();
    }
}