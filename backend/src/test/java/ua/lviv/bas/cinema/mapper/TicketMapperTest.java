package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;

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

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class TicketMapperTest {

	private TicketMapper ticketMapper;

	private Ticket ticket;
	private Booking booking;
	private Session session;

	@BeforeEach
	void setUp() {
		ticketMapper = Mappers.getMapper(TicketMapper.class);

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

	@Test
	void toTicketResponse_ShouldMapTicketWithoutBuilder() {
		User user = new User();
		user.setId(2L);
		user.setEmail("test@example.com");

		Movie movie = new Movie();
		movie.setId(2L);
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setId(2L);
		hall.setName("Hall B");

		Session session = new Session();
		session.setId(2L);
		session.setMovie(movie);
		session.setHall(hall);
		session.setStartTime(LocalDateTime.of(2024, 1, 16, 20, 0));

		Booking booking = new Booking();
		booking.setId(124L);
		booking.setUser(user);
		booking.setSession(session);

		TicketType ticketType = new TicketType();
		ticketType.setId(4L);
		ticketType.setDisplayName("Senior Ticket");

		Ticket ticket = new Ticket();
		ticket.setId(789L);
		ticket.setBooking(booking);
		ticket.setUser(user);
		ticket.setTicketType(ticketType);
		ticket.setUniqueCode("TKT-XYZ789ABC123");
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setFinalPrice(new BigDecimal("200.00"));
		ticket.setPurchaseTime(LocalDateTime.of(2024, 1, 16, 15, 0));

		TicketResponse response = ticketMapper.toTicketResponse(ticket);

		assertNotNull(response);
		assertEquals(789L, response.getId());
		assertEquals("TKT-XYZ789ABC123", response.getTicketCode());
		assertEquals(TicketStatus.ACTIVE, response.getStatus());
		assertEquals("Senior Ticket", response.getTicketType());
		assertEquals(new BigDecimal("200.00"), response.getPrice());
		assertEquals("Test Movie", response.getMovieTitle());
		assertEquals(LocalDateTime.of(2024, 1, 16, 20, 0), response.getSessionTime());
		assertEquals("Hall B", response.getHallName());
	}
}