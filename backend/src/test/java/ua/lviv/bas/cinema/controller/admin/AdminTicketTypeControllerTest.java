package ua.lviv.bas.cinema.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.exception.api.ApiErrorHandler;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.service.booking.types.TicketTypeService;

@ExtendWith(MockitoExtension.class)
public class AdminTicketTypeControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private TicketTypeService ticketTypeService;

	@InjectMocks
	private AdminTicketTypeController adminTicketTypeController;

	private TicketTypeResponse ticketTypeResponse;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(adminTicketTypeController).setControllerAdvice(new ApiErrorHandler())
				.build();

		ticketTypeResponse = TicketTypeResponse.builder().id(1L).code("CHILD").displayName("Child Ticket")
				.priceMultiplier(new BigDecimal("0.70")).minAge(0).maxAge(12).requiresDocument(true)
				.documentType("Birth Certificate").active(true).category(TicketTypeCategory.CHILD).build();
	}

	@Test
	void createTicketType_ShouldReturnCreated() throws Exception {
		TicketTypeCreateRequest createRequest = TicketTypeCreateRequest.builder().code("CHILD")
				.displayName("Child Ticket").priceMultiplier(new BigDecimal("0.70")).category(TicketTypeCategory.CHILD)
				.build();

		when(ticketTypeService.createTicketType(any(TicketTypeCreateRequest.class))).thenReturn(ticketTypeResponse);

		mockMvc.perform(post("/api/admin/ticket-types").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.code").value("CHILD"));

		verify(ticketTypeService).createTicketType(any(TicketTypeCreateRequest.class));
	}

	@Test
	void createTicketType_ShouldReturnConflictWhenDuplicate() throws Exception {
		TicketTypeCreateRequest createRequest = TicketTypeCreateRequest.builder().code("EXISTING")
				.displayName("Existing Ticket").priceMultiplier(new BigDecimal("1.00"))
				.category(TicketTypeCategory.STANDARD).build();

		when(ticketTypeService.createTicketType(any(TicketTypeCreateRequest.class)))
				.thenThrow(new TicketTypeDuplicateException("EXISTING"));

		mockMvc.perform(post("/api/admin/ticket-types").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isConflict());

		verify(ticketTypeService).createTicketType(any(TicketTypeCreateRequest.class));
	}

	@Test
	void getTicketTypes_ShouldReturnAll() throws Exception {
		TicketTypeResponse adultResponse = TicketTypeResponse.builder().id(2L).code("STANDARD")
				.displayName("Standard Ticket").priceMultiplier(new BigDecimal("1.00")).active(true)
				.category(TicketTypeCategory.STANDARD).build();

		List<TicketTypeResponse> responses = Arrays.asList(ticketTypeResponse, adultResponse);

		when(ticketTypeService.getTicketTypes(null, null, null)).thenReturn(responses);

		mockMvc.perform(get("/api/admin/ticket-types")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].code").value("CHILD"))
				.andExpect(jsonPath("$[1].code").value("STANDARD"));

		verify(ticketTypeService).getTicketTypes(null, null, null);
	}

	@Test
	void getTicketTypes_ShouldReturnWithFilters() throws Exception {
		List<TicketTypeResponse> activeResponses = Arrays.asList(ticketTypeResponse);

		when(ticketTypeService.getTicketTypes(true, TicketTypeCategory.CHILD, "CHILD")).thenReturn(activeResponses);

		mockMvc.perform(get("/api/admin/ticket-types").param("active", "true").param("category", "CHILD")
				.param("search", "CHILD")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));

		verify(ticketTypeService).getTicketTypes(true, TicketTypeCategory.CHILD, "CHILD");
	}

	@Test
	void getTicketTypeById_ShouldReturnTicketType() throws Exception {
		when(ticketTypeService.getTicketTypeById(1L)).thenReturn(ticketTypeResponse);

		mockMvc.perform(get("/api/admin/ticket-types/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.code").value("CHILD"));

		verify(ticketTypeService).getTicketTypeById(1L);
	}

	@Test
	void getTicketTypeById_ShouldReturnNotFound() throws Exception {
		when(ticketTypeService.getTicketTypeById(999L)).thenThrow(new TicketTypeNotFoundException(999L));

		mockMvc.perform(get("/api/admin/ticket-types/999")).andExpect(status().isNotFound());

		verify(ticketTypeService).getTicketTypeById(999L);
	}

	@Test
	void getTicketTypeByCode_ShouldReturnTicketType() throws Exception {
		when(ticketTypeService.getTicketTypeByCode("CHILD")).thenReturn(ticketTypeResponse);

		mockMvc.perform(get("/api/admin/ticket-types/code/CHILD")).andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("CHILD"));

		verify(ticketTypeService).getTicketTypeByCode("CHILD");
	}

	@Test
	void getTicketTypeByCode_ShouldReturnNotFound() throws Exception {
		when(ticketTypeService.getTicketTypeByCode("INVALID")).thenThrow(new TicketTypeNotFoundException("INVALID"));

		mockMvc.perform(get("/api/admin/ticket-types/code/INVALID")).andExpect(status().isNotFound());

		verify(ticketTypeService).getTicketTypeByCode("INVALID");
	}

	@Test
	void updateTicketType_ShouldReturnOk() throws Exception {
		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated Child Ticket")
				.build();

		TicketTypeResponse updatedResponse = TicketTypeResponse.builder().id(1L).code("CHILD")
				.displayName("Updated Child Ticket").priceMultiplier(new BigDecimal("0.70")).active(true)
				.category(TicketTypeCategory.CHILD).build();

		when(ticketTypeService.updateTicketType(eq(1L), any(TicketTypeUpdateRequest.class)))
				.thenReturn(updatedResponse);

		mockMvc.perform(put("/api/admin/ticket-types/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").value("Updated Child Ticket"));

		verify(ticketTypeService).updateTicketType(eq(1L), any(TicketTypeUpdateRequest.class));
	}

	@Test
	void updateTicketType_ShouldReturnNotFound() throws Exception {
		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated").build();

		when(ticketTypeService.updateTicketType(eq(999L), any(TicketTypeUpdateRequest.class)))
				.thenThrow(new TicketTypeNotFoundException(999L));

		mockMvc.perform(put("/api/admin/ticket-types/999").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isNotFound());

		verify(ticketTypeService).updateTicketType(eq(999L), any(TicketTypeUpdateRequest.class));
	}

	@Test
	void deleteTicketType_ShouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/api/admin/ticket-types/1")).andExpect(status().isNoContent());

		verify(ticketTypeService).deleteTicketType(1L);
	}

	@Test
	void deleteTicketType_ShouldReturnNotFound() throws Exception {
		doThrow(new TicketTypeNotFoundException(999L)).when(ticketTypeService).deleteTicketType(999L);

		mockMvc.perform(delete("/api/admin/ticket-types/999")).andExpect(status().isNotFound());

		verify(ticketTypeService).deleteTicketType(999L);
	}

	@Test
	void deleteTicketType_ShouldReturnConflictWhenInUse() throws Exception {
		doThrow(new TicketTypeInUseException(1L)).when(ticketTypeService).deleteTicketType(1L);

		mockMvc.perform(delete("/api/admin/ticket-types/1")).andExpect(status().isConflict());

		verify(ticketTypeService).deleteTicketType(1L);
	}

	@Test
	void toggleTicketTypeActive_ShouldReturnOk() throws Exception {
		TicketTypeResponse toggledResponse = TicketTypeResponse.builder().id(1L).code("CHILD")
				.displayName("Child Ticket").priceMultiplier(new BigDecimal("0.70")).active(false)
				.category(TicketTypeCategory.CHILD).build();

		when(ticketTypeService.toggleTicketTypeActiveStatus(1L)).thenReturn(toggledResponse);

		mockMvc.perform(patch("/api/admin/ticket-types/1/toggle-active")).andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(false));

		verify(ticketTypeService).toggleTicketTypeActiveStatus(1L);
	}

	@Test
	void toggleTicketTypeActive_ShouldReturnNotFound() throws Exception {
		when(ticketTypeService.toggleTicketTypeActiveStatus(999L)).thenThrow(new TicketTypeNotFoundException(999L));

		mockMvc.perform(patch("/api/admin/ticket-types/999/toggle-active")).andExpect(status().isNotFound());

		verify(ticketTypeService).toggleTicketTypeActiveStatus(999L);
	}
}