package ua.lviv.bas.cinema.mapper.ticket;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
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

		assertThat(response.id()).isEqualTo(456L);
		assertThat(response.ticketCode()).isEqualTo("TKT-ABC123");
		assertThat(response.status()).isEqualTo(TicketStatus.ACTIVE);
		assertThat(response.price()).isEqualTo(new BigDecimal("250.00"));
		assertThat(response.ticketType()).isEqualTo("Adult Ticket");
		assertThat(response.movieTitle()).isEqualTo("Inception");
		assertThat(response.hallName()).isEqualTo("Hall A");
	}

	@Test
	void toTicketResponseFromProjection() {
		var projection = Mockito.mock(ua.lviv.bas.cinema.repository.ticket.projection.TicketInfoProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getUniqueCode()).thenReturn("PROJ-TICKET");
		Mockito.when(projection.getFinalPrice()).thenReturn(new BigDecimal("200.00"));
		Mockito.when(projection.getTicketTypeName()).thenReturn("Child Ticket");
		Mockito.when(projection.getMovieTitle()).thenReturn("Movie Title");
		Mockito.when(projection.getHallName()).thenReturn("Hall Name");

		TicketResponse response = mapper.toTicketResponse(projection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.ticketCode()).isEqualTo("PROJ-TICKET");
		assertThat(response.price()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.ticketType()).isEqualTo("Child Ticket");
		assertThat(response.movieTitle()).isEqualTo("Movie Title");
		assertThat(response.hallName()).isEqualTo("Hall Name");
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

	@Test
	void toTicketResponseWithNull() {
		TicketResponse response = mapper.toTicketResponse((Ticket) null);
		assertThat(response).isNull();
	}
}