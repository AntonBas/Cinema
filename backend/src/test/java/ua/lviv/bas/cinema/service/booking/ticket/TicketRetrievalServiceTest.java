package ua.lviv.bas.cinema.service.booking.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.TicketMapper;
import ua.lviv.bas.cinema.repository.TicketRepository;

@ExtendWith(MockitoExtension.class)
public class TicketRetrievalServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketMapper ticketMapper;

	@InjectMocks
	private TicketRetrievalService ticketRetrievalService;

	private User testUser;
	private Ticket testTicket;
	private TicketResponse testTicketResponse;

	private static final Long USER_ID = 1L;
	private static final Long TICKET_ID = 2L;
	private static final String TICKET_CODE = "TKT-123456";

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(USER_ID);
		testUser.setEmail("test@example.com");

		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall 1");

		Session session = new Session();
		session.setMovie(movie);
		session.setHall(hall);
		session.setStartTime(LocalDateTime.now().plusHours(2));

		Booking booking = new Booking();
		booking.setSession(session);

		testTicket = new Ticket();
		testTicket.setId(TICKET_ID);
		testTicket.setUser(testUser);
		testTicket.setBooking(booking);
		testTicket.setUniqueCode(TICKET_CODE);
		testTicket.setStatus(TicketStatus.ACTIVE);
		testTicket.setPurchaseTime(LocalDateTime.now());

		testTicketResponse = new TicketResponse();
		testTicketResponse.setId(TICKET_ID);
		testTicketResponse.setTicketCode(TICKET_CODE);
		testTicketResponse.setStatus(TicketStatus.ACTIVE);
		testTicketResponse.setMovieTitle("Test Movie");
		testTicketResponse.setHallName("Hall 1");
	}

	@Test
	void getTicketById_Success() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

		TicketResponse response = ticketRetrievalService.getTicketById(TICKET_ID, testUser);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(TICKET_ID);
		assertThat(response.getTicketCode()).isEqualTo(TICKET_CODE);
		verify(ticketRepository).findById(TICKET_ID);
	}

	@Test
	void getTicketById_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketRetrievalService.getTicketById(TICKET_ID, testUser))
				.isInstanceOf(TicketNotFoundException.class);
	}

	@Test
	void getTicketById_WhenAccessDenied_ShouldThrowException() {
		User otherUser = new User();
		otherUser.setId(999L);

		when(ticketRepository.findById(TICKET_ID)).thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> ticketRetrievalService.getTicketById(TICKET_ID, otherUser))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void getTicketByCode_Success() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(testTicket));
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

		TicketResponse response = ticketRetrievalService.getTicketByCode(TICKET_CODE, testUser);

		assertThat(response).isNotNull();
		assertThat(response.getTicketCode()).isEqualTo(TICKET_CODE);
		verify(ticketRepository).findByUniqueCode(TICKET_CODE);
	}

	@Test
	void getTicketByCode_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketRetrievalService.getTicketByCode(TICKET_CODE, testUser))
				.isInstanceOf(TicketNotFoundException.class);
	}

	@Test
	void getUserTickets_WithStatusFilter() {
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(ticketRepository.findByUserIdAndStatusOrderByPurchaseTimeDesc(USER_ID, TicketStatus.ACTIVE))
				.thenReturn(tickets);
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

		List<TicketResponse> responses = ticketRetrievalService.getUserTickets(testUser, TicketStatus.ACTIVE);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).getStatus()).isEqualTo(TicketStatus.ACTIVE);
		verify(ticketRepository).findByUserIdAndStatusOrderByPurchaseTimeDesc(USER_ID, TicketStatus.ACTIVE);
	}

	@Test
	void getUserTickets_WithoutStatusFilter() {
		List<Ticket> tickets = Arrays.asList(testTicket);
		when(ticketRepository.findByUserIdOrderByPurchaseTimeDesc(USER_ID)).thenReturn(tickets);
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

		List<TicketResponse> responses = ticketRetrievalService.getUserTickets(testUser, null);

		assertThat(responses).hasSize(1);
		verify(ticketRepository).findByUserIdOrderByPurchaseTimeDesc(USER_ID);
	}

	@Test
	void getUpcomingTickets_Success() {
		List<Ticket> tickets = Arrays.asList(testTicket);

		when(ticketRepository.findUpcomingTickets(eq(USER_ID), any(LocalDateTime.class))).thenReturn(tickets);
		when(ticketMapper.toTicketResponse(testTicket)).thenReturn(testTicketResponse);

		List<TicketResponse> responses = ticketRetrievalService.getUpcomingTickets(testUser);

		assertThat(responses).hasSize(1);
		verify(ticketRepository).findUpcomingTickets(eq(USER_ID), any(LocalDateTime.class));
	}
}