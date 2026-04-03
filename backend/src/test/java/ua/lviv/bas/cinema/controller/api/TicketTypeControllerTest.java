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

import ua.lviv.bas.cinema.dto.ticketType.response.TicketTypeUserResponse;
import ua.lviv.bas.cinema.service.ticket.TicketTypeService;

@ExtendWith(MockitoExtension.class)
public class TicketTypeControllerTest {

	@Mock
	private TicketTypeService ticketTypeService;

	@InjectMocks
	private TicketTypeController ticketTypeController;

	private TicketTypeUserResponse createTicketTypeUserResponse(Long id, String displayName, BigDecimal priceMultiplier,
			Integer minAge, Integer maxAge, boolean requiresDocument, String documentType) {
		return new TicketTypeUserResponse(id, displayName, priceMultiplier, minAge, maxAge, requiresDocument,
				documentType);
	}

	@Test
	void getDropdownTypes_ShouldReturnActiveTicketTypes() {
		TicketTypeUserResponse response1 = createTicketTypeUserResponse(1L, "Child Ticket", new BigDecimal("0.70"), 0,
				12, true, "Birth Certificate");
		TicketTypeUserResponse response2 = createTicketTypeUserResponse(2L, "Adult Ticket", new BigDecimal("1.00"), 18,
				65, false, null);
		TicketTypeUserResponse response3 = createTicketTypeUserResponse(3L, "Student Ticket", new BigDecimal("0.80"),
				12, 25, true, "Student ID");

		List<TicketTypeUserResponse> responses = Arrays.asList(response1, response2, response3);

		when(ticketTypeService.getActiveTicketTypesForUser()).thenReturn(responses);

		ResponseEntity<List<TicketTypeUserResponse>> response = ticketTypeController.getDropdownTypes();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(3, response.getBody().size());

		TicketTypeUserResponse first = response.getBody().get(0);
		assertEquals(1L, first.id());
		assertEquals("Child Ticket", first.displayName());
		assertEquals(new BigDecimal("0.70"), first.priceMultiplier());
		assertEquals(0, first.minAge());
		assertEquals(12, first.maxAge());
		assertEquals(true, first.requiresDocument());
		assertEquals("Birth Certificate", first.documentType());

		TicketTypeUserResponse second = response.getBody().get(1);
		assertEquals(2L, second.id());
		assertEquals("Adult Ticket", second.displayName());
		assertEquals(new BigDecimal("1.00"), second.priceMultiplier());
		assertEquals(18, second.minAge());
		assertEquals(65, second.maxAge());
		assertEquals(false, second.requiresDocument());
		assertEquals(null, second.documentType());

		TicketTypeUserResponse third = response.getBody().get(2);
		assertEquals(3L, third.id());
		assertEquals("Student Ticket", third.displayName());
		assertEquals(new BigDecimal("0.80"), third.priceMultiplier());
		assertEquals(12, third.minAge());
		assertEquals(25, third.maxAge());
		assertEquals(true, third.requiresDocument());
		assertEquals("Student ID", third.documentType());

		verify(ticketTypeService).getActiveTicketTypesForUser();
	}

	@Test
	void getDropdownTypes_ShouldReturnEmptyList() {
		List<TicketTypeUserResponse> emptyList = Arrays.asList();

		when(ticketTypeService.getActiveTicketTypesForUser()).thenReturn(emptyList);

		ResponseEntity<List<TicketTypeUserResponse>> response = ticketTypeController.getDropdownTypes();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().size());
		verify(ticketTypeService).getActiveTicketTypesForUser();
	}
}