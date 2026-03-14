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

import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeUserResponse;
import ua.lviv.bas.cinema.service.booking.types.TicketTypeService;

@ExtendWith(MockitoExtension.class)
public class TicketTypeControllerTest {

	@Mock
	private TicketTypeService ticketTypeService;

	@InjectMocks
	private TicketTypeController ticketTypeController;

	private TicketTypeUserResponse createTicketTypeUserResponse(Long id, String displayName, BigDecimal priceMultiplier,
			boolean requiresDocument, String documentType) {
		return TicketTypeUserResponse.builder().id(id).displayName(displayName).priceMultiplier(priceMultiplier)
				.requiresDocument(requiresDocument).documentType(documentType).build();
	}

	@Test
	void getDropdownTypes_ShouldReturnActiveTicketTypes() {
		TicketTypeUserResponse response1 = createTicketTypeUserResponse(1L, "Child Ticket", new BigDecimal("0.70"),
				true, "Birth Certificate");
		TicketTypeUserResponse response2 = createTicketTypeUserResponse(2L, "Adult Ticket", new BigDecimal("1.00"),
				false, null);
		List<TicketTypeUserResponse> responses = Arrays.asList(response1, response2);

		when(ticketTypeService.getActiveTicketTypesForUser()).thenReturn(responses);

		ResponseEntity<List<TicketTypeUserResponse>> response = ticketTypeController.getDropdownTypes();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());

		TicketTypeUserResponse first = response.getBody().get(0);
		assertEquals(1L, first.getId());
		assertEquals("Child Ticket", first.getDisplayName());
		assertEquals(new BigDecimal("0.70"), first.getPriceMultiplier());
		assertEquals(true, first.isRequiresDocument());
		assertEquals("Birth Certificate", first.getDocumentType());

		TicketTypeUserResponse second = response.getBody().get(1);
		assertEquals(2L, second.getId());
		assertEquals("Adult Ticket", second.getDisplayName());
		assertEquals(new BigDecimal("1.00"), second.getPriceMultiplier());
		assertEquals(false, second.isRequiresDocument());
		assertEquals(null, second.getDocumentType());

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