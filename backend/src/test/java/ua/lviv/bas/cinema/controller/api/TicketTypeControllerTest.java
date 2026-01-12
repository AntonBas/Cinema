package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.service.booking.TicketTypeService;

@ExtendWith(MockitoExtension.class)
public class TicketTypeControllerTest {

	@Mock
	private TicketTypeService ticketTypeService;

	@InjectMocks
	private TicketTypeController ticketTypeController;

	private TicketTypeResponse createTicketTypeResponse(Long id, String code, String displayName, Integer minAge,
			Integer maxAge, boolean active) {
		return TicketTypeResponse.builder().id(id).code(code).displayName(displayName)
				.priceMultiplier(new BigDecimal("0.70")).minAge(minAge).maxAge(maxAge).requiresDocument(false)
				.documentType(null).active(active).category(TicketTypeCategory.CHILD).build();
	}

	private TicketTypeSimpleResponse createTicketTypeSimpleResponse(Long id, String code, String displayName,
			boolean active) {
		return TicketTypeSimpleResponse.builder().id(id).code(code).displayName(displayName)
				.priceMultiplier(new BigDecimal("0.70")).active(active).build();
	}

	@Test
	void getAllActiveTicketTypes_ShouldReturnActiveTicketTypes() {
		TicketTypeResponse ticket1 = createTicketTypeResponse(1L, "CHILD", "Child Ticket", 0, 12, true);
		TicketTypeResponse ticket2 = createTicketTypeResponse(2L, "ADULT", "Adult Ticket", 18, null, true);
		List<TicketTypeResponse> activeTicketTypes = Arrays.asList(ticket1, ticket2);

		when(ticketTypeService.getAllTicketTypes(true)).thenReturn(activeTicketTypes);

		ResponseEntity<List<TicketTypeResponse>> response = ticketTypeController.getAllActiveTicketTypes();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
		assertEquals("CHILD", response.getBody().get(0).getCode());
		assertEquals("ADULT", response.getBody().get(1).getCode());
		verify(ticketTypeService).getAllTicketTypes(true);
	}

	@Test
	void getTicketTypeById_ShouldReturnTicketType_WhenExists() {
		TicketTypeResponse ticket = createTicketTypeResponse(1L, "CHILD", "Child Ticket", 0, 12, true);

		when(ticketTypeService.getTicketTypeById(1L)).thenReturn(ticket);

		ResponseEntity<TicketTypeResponse> response = ticketTypeController.getTicketTypeById(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1L, response.getBody().getId());
		assertEquals("CHILD", response.getBody().getCode());
		verify(ticketTypeService).getTicketTypeById(1L);
	}

	@Test
	void getTicketTypeById_ShouldThrowException_WhenNotExists() {
		TicketTypeNotFoundException exception = new TicketTypeNotFoundException(999L);

		when(ticketTypeService.getTicketTypeById(999L)).thenThrow(exception);

		TicketTypeNotFoundException thrown = assertThrows(TicketTypeNotFoundException.class,
				() -> ticketTypeController.getTicketTypeById(999L));

		assertEquals(exception.getMessage(), thrown.getMessage());
		verify(ticketTypeService).getTicketTypeById(999L);
	}

	@Test
	void getTicketTypeByCode_ShouldReturnTicketType() {
		TicketTypeResponse ticket = createTicketTypeResponse(1L, "CHILD", "Child Ticket", 0, 12, true);

		when(ticketTypeService.getTicketTypeByCode("CHILD")).thenReturn(ticket);

		ResponseEntity<TicketTypeResponse> response = ticketTypeController.getTicketTypeByCode("CHILD");

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("CHILD", response.getBody().getCode());
		verify(ticketTypeService).getTicketTypeByCode("CHILD");
	}

	@Test
	void getSimpleActiveTicketTypes_ShouldReturnSimpleList() {
		TicketTypeSimpleResponse simple1 = createTicketTypeSimpleResponse(1L, "CHILD", "Child Ticket", true);
		TicketTypeSimpleResponse simple2 = createTicketTypeSimpleResponse(2L, "ADULT", "Adult Ticket", true);
		List<TicketTypeSimpleResponse> simpleResponses = Arrays.asList(simple1, simple2);

		when(ticketTypeService.getSimpleTicketTypes(true)).thenReturn(simpleResponses);

		ResponseEntity<List<TicketTypeSimpleResponse>> response = ticketTypeController.getSimpleActiveTicketTypes();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
		assertEquals("CHILD", response.getBody().get(0).getCode());
		verify(ticketTypeService).getSimpleTicketTypes(true);
	}

	@Test
	void validateAgeForTicketType_ShouldReturnTrue_WhenValidAge() {
		when(ticketTypeService.validateAgeForTicketType(1L, 10)).thenReturn(true);

		ResponseEntity<Boolean> response = ticketTypeController.validateAgeForTicketType(1L, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(true, response.getBody());
		verify(ticketTypeService).validateAgeForTicketType(1L, 10);
	}

	@Test
	void validateAgeForTicketType_ShouldReturnFalse_WhenInvalidAge() {
		when(ticketTypeService.validateAgeForTicketType(1L, 20)).thenReturn(false);

		ResponseEntity<Boolean> response = ticketTypeController.validateAgeForTicketType(1L, 20);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(false, response.getBody());
		verify(ticketTypeService).validateAgeForTicketType(1L, 20);
	}

	@Test
	void getFormattedAgeRange_ShouldReturnAgeRange() {
		when(ticketTypeService.getFormattedAgeRange(1L)).thenReturn("0-12 years");

		ResponseEntity<String> response = ticketTypeController.getFormattedAgeRange(1L);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals("0-12 years", response.getBody());
		verify(ticketTypeService).getFormattedAgeRange(1L);
	}

	@Test
	void getTicketTypesForAge_ShouldReturnAvailableTicketTypes() {
		TicketTypeSimpleResponse simple1 = createTicketTypeSimpleResponse(1L, "CHILD", "Child Ticket", true);
		TicketTypeSimpleResponse simple2 = createTicketTypeSimpleResponse(2L, "ADULT", "Adult Ticket", true);
		List<TicketTypeSimpleResponse> allActive = Arrays.asList(simple1, simple2);

		when(ticketTypeService.getSimpleTicketTypes(true)).thenReturn(allActive);
		when(ticketTypeService.validateAgeForTicketType(1L, 10)).thenReturn(true);
		when(ticketTypeService.validateAgeForTicketType(2L, 10)).thenReturn(false);

		ResponseEntity<List<TicketTypeSimpleResponse>> response = ticketTypeController.getTicketTypesForAge(10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(1, response.getBody().size());
		assertEquals("CHILD", response.getBody().get(0).getCode());
		verify(ticketTypeService).getSimpleTicketTypes(true);
		verify(ticketTypeService).validateAgeForTicketType(1L, 10);
		verify(ticketTypeService).validateAgeForTicketType(2L, 10);
	}

	@Test
	void getTicketTypesForAge_ShouldReturnEmptyList_WhenNoSuitableTicketTypes() {
		TicketTypeSimpleResponse simple1 = createTicketTypeSimpleResponse(1L, "ADULT", "Adult Ticket", true);
		List<TicketTypeSimpleResponse> allActive = Arrays.asList(simple1);

		when(ticketTypeService.getSimpleTicketTypes(true)).thenReturn(allActive);
		when(ticketTypeService.validateAgeForTicketType(1L, 10)).thenReturn(false);

		ResponseEntity<List<TicketTypeSimpleResponse>> response = ticketTypeController.getTicketTypesForAge(10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().size());
		verify(ticketTypeService).getSimpleTicketTypes(true);
		verify(ticketTypeService).validateAgeForTicketType(1L, 10);
	}

	@Test
	void getTicketTypesForAge_ShouldHandleAllTypesValid() {
		TicketTypeSimpleResponse simple1 = createTicketTypeSimpleResponse(1L, "CHILD", "Child Ticket", true);
		TicketTypeSimpleResponse simple2 = createTicketTypeSimpleResponse(2L, "ADULT", "Adult Ticket", true);
		List<TicketTypeSimpleResponse> allActive = Arrays.asList(simple1, simple2);

		when(ticketTypeService.getSimpleTicketTypes(true)).thenReturn(allActive);
		when(ticketTypeService.validateAgeForTicketType(1L, 25)).thenReturn(true);
		when(ticketTypeService.validateAgeForTicketType(2L, 25)).thenReturn(true);

		ResponseEntity<List<TicketTypeSimpleResponse>> response = ticketTypeController.getTicketTypesForAge(25);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().size());
		verify(ticketTypeService).getSimpleTicketTypes(true);
		verify(ticketTypeService).validateAgeForTicketType(1L, 25);
		verify(ticketTypeService).validateAgeForTicketType(2L, 25);
	}

	@Test
	void getTicketTypesForAge_ShouldHandleEmptyActiveList() {
		List<TicketTypeSimpleResponse> allActive = Arrays.asList();

		when(ticketTypeService.getSimpleTicketTypes(true)).thenReturn(allActive);

		ResponseEntity<List<TicketTypeSimpleResponse>> response = ticketTypeController.getTicketTypesForAge(10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertNotNull(response.getBody());
		assertEquals(0, response.getBody().size());
		verify(ticketTypeService).getSimpleTicketTypes(true);
	}

	@Test
	void getTicketTypeByCode_ShouldThrowException_WhenCodeNotFound() {
		TicketTypeNotFoundException exception = new TicketTypeNotFoundException("INVALID");

		when(ticketTypeService.getTicketTypeByCode("INVALID")).thenThrow(exception);

		TicketTypeNotFoundException thrown = assertThrows(TicketTypeNotFoundException.class,
				() -> ticketTypeController.getTicketTypeByCode("INVALID"));

		assertEquals(exception.getMessage(), thrown.getMessage());
		verify(ticketTypeService).getTicketTypeByCode("INVALID");
	}
}