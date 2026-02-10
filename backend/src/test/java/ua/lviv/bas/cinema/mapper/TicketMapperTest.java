package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;

public class TicketMapperTest {

	private TicketMapper mapper = Mappers.getMapper(TicketMapper.class);

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

		assertThat(response.getId()).isEqualTo(456L);
		assertThat(response.getTicketCode()).isEqualTo("TKT-ABC123");
		assertThat(response.getStatus()).isEqualTo(TicketStatus.ACTIVE);
		assertThat(response.getPrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(response.getTicketType()).isEqualTo("Adult Ticket");
		assertThat(response.getMovieTitle()).isEqualTo("Inception");
		assertThat(response.getHallName()).isEqualTo("Hall A");
	}

	@Test
	void toTicketResponseFromProjection() {
		var projection = Mockito.mock(ua.lviv.bas.cinema.domain.projection.TicketInfoProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getUniqueCode()).thenReturn("PROJ-TICKET");
		Mockito.when(projection.getFinalPrice()).thenReturn(new BigDecimal("200.00"));
		Mockito.when(projection.getTicketTypeName()).thenReturn("Child Ticket");
		Mockito.when(projection.getMovieTitle()).thenReturn("Movie Title");
		Mockito.when(projection.getHallName()).thenReturn("Hall Name");

		TicketResponse response = mapper.toTicketResponse(projection);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTicketCode()).isEqualTo("PROJ-TICKET");
		assertThat(response.getPrice()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.getTicketType()).isEqualTo("Child Ticket");
		assertThat(response.getMovieTitle()).isEqualTo("Movie Title");
		assertThat(response.getHallName()).isEqualTo("Hall Name");
	}

	@Test
	void toTicketResponseWithNullBooking() {
		TicketType ticketType = TicketType.builder().displayName("Adult Ticket").build();

		Ticket ticket = Ticket.builder().id(456L).booking(null).uniqueCode("TKT-ABC123").ticketType(ticketType)
				.finalPrice(new BigDecimal("250.00")).status(TicketStatus.ACTIVE).build();

		TicketResponse response = mapper.toTicketResponse(ticket);

		assertThat(response.getId()).isEqualTo(456L);
		assertThat(response.getTicketCode()).isEqualTo("TKT-ABC123");
		assertThat(response.getPrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(response.getTicketType()).isEqualTo("Adult Ticket");
		assertThat(response.getMovieTitle()).isNull();
		assertThat(response.getHallName()).isNull();
	}

	@Test
	void toTicketResponseWithNullTicketType() {
		Movie movie = Movie.builder().id(1L).title("Inception").build();

		Session session = Session.builder().id(1L).movie(movie).build();

		Booking booking = Booking.builder().id(123L).session(session).build();

		Ticket ticket = Ticket.builder().id(456L).booking(booking).uniqueCode("TKT-ABC123").ticketType(null)
				.finalPrice(new BigDecimal("250.00")).status(TicketStatus.ACTIVE).build();

		TicketResponse response = mapper.toTicketResponse(ticket);

		assertThat(response.getId()).isEqualTo(456L);
		assertThat(response.getTicketCode()).isEqualTo("TKT-ABC123");
		assertThat(response.getPrice()).isEqualTo(new BigDecimal("250.00"));
		assertThat(response.getTicketType()).isNull();
		assertThat(response.getMovieTitle()).isEqualTo("Inception");
	}

	@Test
	void toTicketResponseWithNull() {
		TicketResponse response = mapper.toTicketResponse((Ticket) null);
		assertThat(response).isNull();
	}
}