package ua.lviv.bas.cinema.service.booking.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.domain.projection.TicketInfoProjection;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;
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

	private final Long USER_ID = 1L;
	private final Long OTHER_USER_ID = 2L;
	private final Long TICKET_ID = 100L;
	private final String TICKET_CODE = "TICKET-123";

	@Test
	void getTicketById_Success() {
		User user = createUser(USER_ID);
		Ticket ticket = createTicket(USER_ID, TICKET_ID, TICKET_CODE);
		TicketResponse expectedResponse = createTicketResponse();

		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(ticket));
		when(ticketMapper.toTicketResponse(ticket)).thenReturn(expectedResponse);

		TicketResponse result = ticketRetrievalService.getTicketById(TICKET_ID, user);

		assertThat(result).isEqualTo(expectedResponse);
		assertThat(result.getQrCodeUrl()).contains(TICKET_CODE);
	}

	@Test
	void getTicketById_NotFound_ThrowsException() {
		User user = createUser(USER_ID);

		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketRetrievalService.getTicketById(TICKET_ID, user))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void getTicketByCode_Success() {
		User user = createUser(USER_ID);
		Ticket ticket = createTicket(USER_ID, TICKET_ID, TICKET_CODE);
		TicketResponse expectedResponse = createTicketResponse();

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(ticket));
		when(ticketMapper.toTicketResponse(ticket)).thenReturn(expectedResponse);

		TicketResponse result = ticketRetrievalService.getTicketByCode(TICKET_CODE, user);

		assertThat(result).isEqualTo(expectedResponse);
		assertThat(result.getQrCodeUrl()).contains(TICKET_CODE);
	}

	@Test
	void getTicketByCode_NotFound_ThrowsException() {
		User user = createUser(USER_ID);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketRetrievalService.getTicketByCode(TICKET_CODE, user))
				.isInstanceOf(TicketNotFoundException.class).hasMessageContaining(TICKET_CODE);
	}

	@Test
	void getTicketByCode_WrongUser_ThrowsException() {
		User user = createUser(OTHER_USER_ID);
		Ticket ticket = createTicket(USER_ID, TICKET_ID, TICKET_CODE);

		when(ticketRepository.findByUniqueCode(TICKET_CODE)).thenReturn(Optional.of(ticket));

		assertThatThrownBy(() -> ticketRetrievalService.getTicketByCode(TICKET_CODE, user))
				.isInstanceOf(TicketValidationException.class);
	}

	@Test
	void getUserTickets_Success() {
		User user = createUser(USER_ID);
		TicketFilterRequest filter = TicketFilterRequest.builder().status(TicketStatus.ACTIVE).build();
		Pageable pageable = Pageable.unpaged();

		TicketInfoProjection projection = createTicketInfoProjection();
		TicketResponse ticketResponse = createTicketResponse();

		Page<TicketInfoProjection> projectionPage = new PageImpl<>(List.of(projection));

		when(ticketRepository.findUserTickets(eq(USER_ID), eq(filter.getStatus()), eq(filter.getMovieId()), any(),
				any(), any(), any(), eq(pageable))).thenReturn(projectionPage);
		when(ticketMapper.toTicketResponse(projection)).thenReturn(ticketResponse);

		Page<TicketResponse> result = ticketRetrievalService.getUserTickets(user, filter, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(ticketResponse);
	}

	@Test
	void getUserTickets_WithDateFilters_Success() {
		User user = createUser(USER_ID);
		LocalDateTime now = LocalDateTime.now();
		TicketFilterRequest filter = TicketFilterRequest.builder().status(TicketStatus.ACTIVE)
				.purchaseDateFrom(now.toLocalDate()).purchaseDateTo(now.toLocalDate())
				.sessionDateFrom(now.toLocalDate()).sessionDateTo(now.toLocalDate()).movieId(5L).build();
		Pageable pageable = Pageable.unpaged();

		TicketInfoProjection projection = createTicketInfoProjection();
		TicketResponse ticketResponse = createTicketResponse();

		Page<TicketInfoProjection> projectionPage = new PageImpl<>(List.of(projection));

		when(ticketRepository.findUserTickets(eq(USER_ID), eq(filter.getStatus()), eq(5L), any(), any(), any(), any(),
				eq(pageable))).thenReturn(projectionPage);
		when(ticketMapper.toTicketResponse(projection)).thenReturn(ticketResponse);

		Page<TicketResponse> result = ticketRetrievalService.getUserTickets(user, filter, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(ticketResponse);
	}

	private User createUser(Long id) {
		User user = new User();
		user.setId(id);
		return user;
	}

	private Ticket createTicket(Long userId, Long ticketId, String ticketCode) {
		User user = createUser(userId);
		Ticket ticket = new Ticket();
		ticket.setId(ticketId);
		ticket.setUser(user);
		ticket.setUniqueCode(ticketCode);
		ticket.setStatus(TicketStatus.ACTIVE);
		return ticket;
	}

	private TicketResponse createTicketResponse() {
		TicketResponse response = new TicketResponse();
		response.setId(TICKET_ID);
		response.setTicketCode(TICKET_CODE);
		return response;
	}

	private TicketInfoProjection createTicketInfoProjection() {
		return new TicketInfoProjection() {
			@Override
			public Long getId() {
				return TICKET_ID;
			}

			@Override
			public String getUniqueCode() {
				return TICKET_CODE;
			}

			@Override
			public TicketStatus getStatus() {
				return TicketStatus.ACTIVE;
			}

			@Override
			public LocalDateTime getPurchaseTime() {
				return LocalDateTime.now();
			}

			@Override
			public java.math.BigDecimal getFinalPrice() {
				return java.math.BigDecimal.valueOf(150);
			}

			@Override
			public String getTicketTypeName() {
				return "Adult";
			}

			@Override
			public String getMovieTitle() {
				return "Test Movie";
			}

			@Override
			public LocalDateTime getSessionStartTime() {
				return LocalDateTime.now().plusDays(1);
			}

			@Override
			public String getHallName() {
				return "Hall 1";
			}

			@Override
			public Integer getRow() {
				return 1;
			}

			@Override
			public Integer getSeatNumber() {
				return 10;
			}

			@Override
			public Long getUserId() {
				return USER_ID;
			}

			@Override
			public Long getMovieId() {
				return 5L;
			}
		};
	}
}