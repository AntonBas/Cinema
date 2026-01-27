package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.service.booking.types.TicketTypeService;

@ExtendWith(MockitoExtension.class)
public class TicketTypeControllerTest {

	@Mock
	private TicketTypeService ticketTypeService;

	@InjectMocks
	private TicketTypeController ticketTypeController;

	private TicketTypeSimpleResponse createTicketTypeSimpleResponse(Long id, String code, String displayName,
			boolean active) {
		return TicketTypeSimpleResponse.builder().id(id).code(code).displayName(displayName)
				.priceMultiplier(new BigDecimal("0.70")).active(active).build();
	}

	@Test
	void getDropdownTypes_ShouldReturnActiveTicketTypes() {
		TicketTypeSimpleResponse simple1 = createTicketTypeSimpleResponse(1L, "CHILD", "Child Ticket", true);
		TicketTypeSimpleResponse simple2 = createTicketTypeSimpleResponse(2L, "ADULT", "Adult Ticket", true);
		List<TicketTypeSimpleResponse> simpleResponses = Arrays.asList(simple1, simple2);

		when(ticketTypeService.getActiveTicketTypesForDropdown()).thenReturn(simpleResponses);

		ResponseEntity<List<TicketTypeSimpleResponse>> response = ticketTypeController.getDropdownTypes();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
		assertEquals("CHILD", response.getBody().get(0).getCode());
		verify(ticketTypeService).getActiveTicketTypesForDropdown();
	}

	@Test
	void getDropdownTypes_ShouldReturnEmptyList() {
		List<TicketTypeSimpleResponse> emptyList = Arrays.asList();

		when(ticketTypeService.getActiveTicketTypesForDropdown()).thenReturn(emptyList);

		ResponseEntity<List<TicketTypeSimpleResponse>> response = ticketTypeController.getDropdownTypes();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().size());
		verify(ticketTypeService).getActiveTicketTypesForDropdown();
	}
}