package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;

@ExtendWith(MockitoExtension.class)
public class TicketMapperTest {

	private TicketMapper ticketMapper = new TicketMapperImpl();

	private Ticket ticket;
	private Booking booking;
	private Session session;

	@BeforeEach
	void setUp() {
		User user = User.builder().id(1L).email("user@example.com").firstName("John").lastName("Doe").build();

		Movie movie = Movie.builder().id(1L).title("Inception").build();

		CinemaHall cinemaHall = CinemaHall.builder().id(1L).name("Hall A").build();

		session = Session.builder().id(1L).movie(movie).hall(cinemaHall)
				.startTime(LocalDateTime.of(2024, 1, 15, 18, 30)).build();

		Payment payment = Payment.builder().id(1L).amount(new BigDecimal("500.00")).build();

		booking = Booking.builder().id(123L).user(user).session(session).build();

		TicketType adultTicket = TicketType.builder().id(1L).displayName("Adult Ticket")
				.priceMultiplier(new BigDecimal("1.0")).build();

		ticket = Ticket.builder().id(456L).booking(booking).user(user).ticketType(adultTicket)
				.uniqueCode("TKT-ABC123DEF456").status(TicketStatus.ACTIVE).originalPrice(new BigDecimal("250.00"))
				.finalPrice(new BigDecimal("250.00")).purchaseTime(LocalDateTime.of(2024, 1, 15, 14, 35))
				.createdAt(LocalDateTime.of(2024, 1, 15, 14, 35)).payment(payment).build();
	}

	@Test
	void toTicketResponse_ShouldMapAllFieldsCorrectly() {
		TicketResponse response = ticketMapper.toTicketResponse(ticket);

		assertNotNull(response);
		assertEquals(456L, response.getId());
		assertEquals("TKT-ABC123DEF456", response.getTicketCode());
		assertEquals(TicketStatus.ACTIVE, response.getStatus());
		assertEquals(LocalDateTime.of(2024, 1, 15, 14, 35), response.getPurchaseTime());
		assertEquals(new BigDecimal("250.00"), response.getPrice());
		assertEquals("Adult Ticket", response.getTicketType());
		assertEquals("Inception", response.getMovieTitle());
		assertEquals(LocalDateTime.of(2024, 1, 15, 18, 30), response.getSessionTime());
		assertEquals("Hall A", response.getHallName());

		assertNull(response.getQrCodeUrl());
		assertNull(response.getRow());
		assertNull(response.getSeatNumber());
	}

	@Test
	void toTicketResponse_ShouldHandleNullTicket() {
		TicketResponse response = ticketMapper.toTicketResponse(null);
		assertNull(response);
	}

	@Test
	void toTicketResponse_ShouldHandleDifferentTicketStatuses() {
		ticket.setStatus(TicketStatus.USED);
		TicketResponse response1 = ticketMapper.toTicketResponse(ticket);
		assertEquals(TicketStatus.USED, response1.getStatus());

		ticket.setStatus(TicketStatus.CANCELLED);
		TicketResponse response2 = ticketMapper.toTicketResponse(ticket);
		assertEquals(TicketStatus.CANCELLED, response2.getStatus());

		ticket.setStatus(TicketStatus.ACTIVE);
		TicketResponse response3 = ticketMapper.toTicketResponse(ticket);
		assertEquals(TicketStatus.ACTIVE, response3.getStatus());
	}

	@Test
	void toTicketResponse_ShouldHandleNullTicketType() {
		ticket.setTicketType(null);

		TicketResponse response = ticketMapper.toTicketResponse(ticket);

		assertNotNull(response);
		assertEquals(456L, response.getId());
		assertEquals("TKT-ABC123DEF456", response.getTicketCode());
		assertEquals(TicketStatus.ACTIVE, response.getStatus());
		assertEquals(new BigDecimal("250.00"), response.getPrice());
		assertNull(response.getTicketType());
		assertEquals("Inception", response.getMovieTitle());
		assertNull(response.getQrCodeUrl());
		assertNull(response.getRow());
		assertNull(response.getSeatNumber());
	}

	@Test
	void toTicketResponse_ShouldHandleNullBooking() {
		ticket.setBooking(null);

		TicketResponse response = ticketMapper.toTicketResponse(ticket);

		assertNotNull(response);
		assertEquals(456L, response.getId());
		assertEquals("TKT-ABC123DEF456", response.getTicketCode());
		assertEquals(TicketStatus.ACTIVE, response.getStatus());
		assertEquals(new BigDecimal("250.00"), response.getPrice());
		assertEquals("Adult Ticket", response.getTicketType());
		assertNull(response.getMovieTitle());
		assertNull(response.getSessionTime());
		assertNull(response.getHallName());
		assertNull(response.getQrCodeUrl());
		assertNull(response.getRow());
		assertNull(response.getSeatNumber());
	}

	@Test
	void toTicketResponse_ShouldHandleBookingWithoutSession() {
		booking.setSession(null);

		TicketResponse response = ticketMapper.toTicketResponse(ticket);

		assertNotNull(response);
		assertEquals(456L, response.getId());
		assertEquals("TKT-ABC123DEF456", response.getTicketCode());
		assertEquals(TicketStatus.ACTIVE, response.getStatus());
		assertEquals(new BigDecimal("250.00"), response.getPrice());
		assertEquals("Adult Ticket", response.getTicketType());
		assertNull(response.getMovieTitle());
		assertNull(response.getSessionTime());
		assertNull(response.getHallName());
		assertNull(response.getQrCodeUrl());
		assertNull(response.getRow());
		assertNull(response.getSeatNumber());
	}

	@Test
	void toTicketResponse_ShouldHandleSessionWithoutMovie() {
		session.setMovie(null);

		TicketResponse response = ticketMapper.toTicketResponse(ticket);

		assertNotNull(response);
		assertEquals(456L, response.getId());
		assertEquals("TKT-ABC123DEF456", response.getTicketCode());
		assertEquals(TicketStatus.ACTIVE, response.getStatus());
		assertEquals(new BigDecimal("250.00"), response.getPrice());
		assertEquals("Adult Ticket", response.getTicketType());
		assertNull(response.getMovieTitle());
		assertEquals(LocalDateTime.of(2024, 1, 15, 18, 30), response.getSessionTime());
		assertEquals("Hall A", response.getHallName());
		assertNull(response.getQrCodeUrl());
		assertNull(response.getRow());
		assertNull(response.getSeatNumber());
	}

	@Test
	void toTicketResponse_ShouldHandleSessionWithoutHall() {
		session.setHall(null);

		TicketResponse response = ticketMapper.toTicketResponse(ticket);

		assertNotNull(response);
		assertEquals(456L, response.getId());
		assertEquals("TKT-ABC123DEF456", response.getTicketCode());
		assertEquals(TicketStatus.ACTIVE, response.getStatus());
		assertEquals(new BigDecimal("250.00"), response.getPrice());
		assertEquals("Adult Ticket", response.getTicketType());
		assertEquals("Inception", response.getMovieTitle());
		assertEquals(LocalDateTime.of(2024, 1, 15, 18, 30), response.getSessionTime());
		assertNull(response.getHallName());
		assertNull(response.getQrCodeUrl());
		assertNull(response.getRow());
		assertNull(response.getSeatNumber());
	}

	@Test
	void toTicketResponse_ShouldHandleDifferentTicketTypes() {
		TicketType childTicket = TicketType.builder().id(2L).displayName("Child Ticket")
				.priceMultiplier(new BigDecimal("0.5")).build();

		TicketType studentTicket = TicketType.builder().id(3L).displayName("Student Ticket")
				.priceMultiplier(new BigDecimal("0.7")).build();

		ticket.setTicketType(childTicket);
		TicketResponse response1 = ticketMapper.toTicketResponse(ticket);
		assertEquals("Child Ticket", response1.getTicketType());

		ticket.setTicketType(studentTicket);
		TicketResponse response2 = ticketMapper.toTicketResponse(ticket);
		assertEquals("Student Ticket", response2.getTicketType());

		assertEquals(456L, response1.getId());
		assertEquals(456L, response2.getId());
		assertEquals("TKT-ABC123DEF456", response1.getTicketCode());
		assertEquals("TKT-ABC123DEF456", response2.getTicketCode());
		assertEquals(new BigDecimal("250.00"), response1.getPrice());
		assertEquals(new BigDecimal("250.00"), response2.getPrice());
	}

	@Test
	void toTicketResponse_ShouldHandleZeroPrice() {
		ticket.setOriginalPrice(BigDecimal.ZERO);
		ticket.setFinalPrice(BigDecimal.ZERO);

		TicketResponse response = ticketMapper.toTicketResponse(ticket);

		assertNotNull(response);
		assertEquals(BigDecimal.ZERO, response.getPrice());
		assertEquals(456L, response.getId());
		assertEquals("TKT-ABC123DEF456", response.getTicketCode());
		assertEquals(TicketStatus.ACTIVE, response.getStatus());
		assertEquals("Adult Ticket", response.getTicketType());
	}

	@Test
	void toTicketResponse_ShouldHandleDifferentPrices() {
		ticket.setFinalPrice(new BigDecimal("199.99"));
		TicketResponse response1 = ticketMapper.toTicketResponse(ticket);
		assertEquals(new BigDecimal("199.99"), response1.getPrice());

		ticket.setFinalPrice(new BigDecimal("0.01"));
		TicketResponse response2 = ticketMapper.toTicketResponse(ticket);
		assertEquals(new BigDecimal("0.01"), response2.getPrice());

		ticket.setFinalPrice(new BigDecimal("1000.00"));
		TicketResponse response3 = ticketMapper.toTicketResponse(ticket);
		assertEquals(new BigDecimal("1000.00"), response3.getPrice());
	}

	@Test
	void toTicketResponseList_ShouldMapAllTickets() {
		Ticket ticket2 = Ticket.builder().id(457L).booking(booking).user(booking.getUser())
				.ticketType(TicketType.builder().id(2L).displayName("Child Ticket")
						.priceMultiplier(new BigDecimal("0.5")).build())
				.uniqueCode("TKT-DEF456GHI789").status(TicketStatus.USED).originalPrice(new BigDecimal("125.00"))
				.finalPrice(new BigDecimal("125.00")).purchaseTime(LocalDateTime.of(2024, 1, 15, 14, 36))
				.createdAt(LocalDateTime.of(2024, 1, 15, 14, 36)).build();

		List<Ticket> tickets = Arrays.asList(ticket, ticket2);

		List<TicketResponse> responses = ticketMapper.toTicketResponseList(tickets);

		assertNotNull(responses);
		assertEquals(2, responses.size());

		assertEquals(456L, responses.get(0).getId());
		assertEquals("TKT-ABC123DEF456", responses.get(0).getTicketCode());
		assertEquals(TicketStatus.ACTIVE, responses.get(0).getStatus());
		assertEquals("Adult Ticket", responses.get(0).getTicketType());
		assertEquals(new BigDecimal("250.00"), responses.get(0).getPrice());

		assertEquals(457L, responses.get(1).getId());
		assertEquals("TKT-DEF456GHI789", responses.get(1).getTicketCode());
		assertEquals(TicketStatus.USED, responses.get(1).getStatus());
		assertEquals("Child Ticket", responses.get(1).getTicketType());
		assertEquals(new BigDecimal("125.00"), responses.get(1).getPrice());

		assertNull(responses.get(0).getQrCodeUrl());
		assertNull(responses.get(0).getRow());
		assertNull(responses.get(0).getSeatNumber());
		assertNull(responses.get(1).getQrCodeUrl());
		assertNull(responses.get(1).getRow());
		assertNull(responses.get(1).getSeatNumber());
	}

	@Test
	void toTicketResponseList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<TicketResponse> responses = ticketMapper.toTicketResponseList(Arrays.asList());

		assertNotNull(responses);
		assertTrue(responses.isEmpty());
	}

	@Test
	void toTicketResponseList_ShouldReturnNull_WhenInputIsNull() {
		List<TicketResponse> responses = ticketMapper.toTicketResponseList(null);
		assertNull(responses);
	}

	@Test
	void toTicketResponse_ShouldIgnoreIgnoredFields() {
		TicketResponse response = ticketMapper.toTicketResponse(ticket);

		assertNotNull(response);
		assertEquals(456L, response.getId());
		assertEquals("TKT-ABC123DEF456", response.getTicketCode());

		assertNull(response.getQrCodeUrl());
		assertNull(response.getRow());
		assertNull(response.getSeatNumber());

		assertEquals("Adult Ticket", response.getTicketType());
		assertEquals("Inception", response.getMovieTitle());
		assertEquals("Hall A", response.getHallName());
		assertEquals(new BigDecimal("250.00"), response.getPrice());
	}
}