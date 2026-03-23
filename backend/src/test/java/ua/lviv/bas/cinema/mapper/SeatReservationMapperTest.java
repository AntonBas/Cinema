package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;

public class SeatReservationMapperTest {

	private SeatReservationMapper seatReservationMapper = new SeatReservationMapperImpl();
	private Session session;
	private Seat seat1;
	@SuppressWarnings("unused")
	private Seat seat2;
	private TicketType adultTicketType;
	@SuppressWarnings("unused")
	private TicketType childTicketType;
	private List<SeatReservationResponse.SeatInfo> seatInfos;
	private List<SeatReservationResponse.TicketPriceInfo> ticketPrices1;
	private List<SeatReservationResponse.TicketPriceInfo> ticketPrices2;

	@BeforeEach
	void setUp() {
		Movie movie = Movie.builder().id(1L).title("Inception").build();

		CinemaHall hall = CinemaHall.builder().id(1L).name("Hall A").build();

		session = Session.builder().id(1L).movie(movie).hall(hall).basePrice(new BigDecimal("250.00")).build();

		seat1 = Seat.builder().id(1L).row(5).number(12).seatType(SeatType.STANDARD).active(true).build();

		seat2 = Seat.builder().id(2L).row(5).number(13).seatType(SeatType.VIP).active(true).build();

		adultTicketType = TicketType.builder().id(1L).displayName("Adult").build();

		childTicketType = TicketType.builder().id(2L).displayName("Child").build();

		ticketPrices1 = Arrays.asList(
				new SeatReservationResponse.TicketPriceInfo(1L, "Adult", new BigDecimal("250.00")),
				new SeatReservationResponse.TicketPriceInfo(2L, "Child", new BigDecimal("200.00")));

		ticketPrices2 = Arrays.asList(
				new SeatReservationResponse.TicketPriceInfo(1L, "Adult", new BigDecimal("350.00")),
				new SeatReservationResponse.TicketPriceInfo(2L, "Child", new BigDecimal("280.00")));

		SeatReservationResponse.SeatInfo seatInfo1 = new SeatReservationResponse.SeatInfo(1L, 5, 12, SeatType.STANDARD,
				true, false, true, ticketPrices1);

		SeatReservationResponse.SeatInfo seatInfo2 = new SeatReservationResponse.SeatInfo(2L, 5, 13, SeatType.VIP, true,
				false, true, ticketPrices2);

		seatInfos = Arrays.asList(seatInfo1, seatInfo2);
	}

	@Test
	void toResponse() {
		int availableSeatsCount = 2;
		SeatReservationResponse response = seatReservationMapper.toResponse(session, seatInfos, availableSeatsCount);

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
		int availableSeatsCount = 0;
		SeatReservationResponse response = seatReservationMapper.toResponse(session, List.of(), availableSeatsCount);

		assertThat(response).isNotNull();
		assertThat(response.sessionId()).isEqualTo(1L);
		assertThat(response.seats()).isEmpty();
		assertThat(response.availableSeats()).isEqualTo(0);
	}

	@Test
	void toResponseWithNullSession() {
		int availableSeatsCount = 2;
		SeatReservationResponse response = seatReservationMapper.toResponse(null, seatInfos, availableSeatsCount);

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
		Boolean available = true;
		Boolean temporarilyReserved = false;
		List<SeatReservationResponse.TicketPriceInfo> ticketPrices = ticketPrices1;

		SeatReservationResponse.SeatInfo seatInfo = seatReservationMapper.toSeatInfo(seat1, available,
				temporarilyReserved, ticketPrices);

		assertThat(seatInfo).isNotNull();
		assertThat(seatInfo.id()).isEqualTo(1L);
		assertThat(seatInfo.row()).isEqualTo(5);
		assertThat(seatInfo.seatNumber()).isEqualTo(12);
		assertThat(seatInfo.seatType()).isEqualTo(SeatType.STANDARD);
		assertThat(seatInfo.available()).isTrue();
		assertThat(seatInfo.temporarilyReserved()).isFalse();
		assertThat(seatInfo.active()).isTrue();
		assertThat(seatInfo.ticketPrices()).hasSize(2);
		assertThat(seatInfo.ticketPrices().get(0).ticketTypeId()).isEqualTo(1L);
		assertThat(seatInfo.ticketPrices().get(0).finalPrice()).isEqualTo(new BigDecimal("250.00"));
	}

	@Test
	void toSeatInfoWithUnavailableSeat() {
		Boolean available = false;
		Boolean temporarilyReserved = true;
		List<SeatReservationResponse.TicketPriceInfo> ticketPrices = ticketPrices1;

		SeatReservationResponse.SeatInfo seatInfo = seatReservationMapper.toSeatInfo(seat1, available,
				temporarilyReserved, ticketPrices);

		assertThat(seatInfo).isNotNull();
		assertThat(seatInfo.available()).isFalse();
		assertThat(seatInfo.temporarilyReserved()).isTrue();
	}

	@Test
	void toSeatInfoWithNullSeat() {
		Boolean available = true;
		Boolean temporarilyReserved = false;
		List<SeatReservationResponse.TicketPriceInfo> ticketPrices = ticketPrices1;

		SeatReservationResponse.SeatInfo seatInfo = seatReservationMapper.toSeatInfo(null, available,
				temporarilyReserved, ticketPrices);

		assertThat(seatInfo).isNotNull();
		assertThat(seatInfo.id()).isNull();
		assertThat(seatInfo.row()).isNull();
		assertThat(seatInfo.seatNumber()).isNull();
		assertThat(seatInfo.seatType()).isNull();
		assertThat(seatInfo.active()).isNull();
		assertThat(seatInfo.ticketPrices()).isEqualTo(ticketPrices);
	}

	@Test
	void toTicketPriceInfo() {
		BigDecimal price = new BigDecimal("250.00");

		SeatReservationResponse.TicketPriceInfo ticketPriceInfo = seatReservationMapper
				.toTicketPriceInfo(adultTicketType, price);

		assertThat(ticketPriceInfo).isNotNull();
		assertThat(ticketPriceInfo.ticketTypeId()).isEqualTo(1L);
		assertThat(ticketPriceInfo.ticketTypeName()).isEqualTo("Adult");
		assertThat(ticketPriceInfo.finalPrice()).isEqualTo(new BigDecimal("250.00"));
	}

	@Test
	void toTicketPriceInfoWithNullTicketType() {
		BigDecimal price = new BigDecimal("250.00");

		SeatReservationResponse.TicketPriceInfo ticketPriceInfo = seatReservationMapper.toTicketPriceInfo(null, price);

		assertThat(ticketPriceInfo).isNotNull();
		assertThat(ticketPriceInfo.ticketTypeId()).isNull();
		assertThat(ticketPriceInfo.ticketTypeName()).isNull();
		assertThat(ticketPriceInfo.finalPrice()).isEqualTo(new BigDecimal("250.00"));
	}

	@Test
	void toTicketPriceInfoWithNullPrice() {
		SeatReservationResponse.TicketPriceInfo ticketPriceInfo = seatReservationMapper
				.toTicketPriceInfo(adultTicketType, null);

		assertThat(ticketPriceInfo).isNotNull();
		assertThat(ticketPriceInfo.ticketTypeId()).isEqualTo(1L);
		assertThat(ticketPriceInfo.ticketTypeName()).isEqualTo("Adult");
		assertThat(ticketPriceInfo.finalPrice()).isNull();
	}

	@Test
	void toTicketPriceInfoWithNullTicketTypeAndNullPrice() {
		SeatReservationResponse.TicketPriceInfo ticketPriceInfo = seatReservationMapper.toTicketPriceInfo(null, null);

		assertThat(ticketPriceInfo).isNull();
	}
}