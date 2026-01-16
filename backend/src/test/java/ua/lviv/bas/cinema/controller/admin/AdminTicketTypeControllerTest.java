package ua.lviv.bas.cinema.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.exception.api.ApiErrorHandler;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.service.booking.TicketTypeService;

@ExtendWith(MockitoExtension.class)
public class AdminTicketTypeControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private TicketTypeService ticketTypeService;

	@InjectMocks
	private AdminTicketTypeController adminTicketTypeController;

	private TicketTypeResponse ticketTypeResponse;
	private TicketTypeSimpleResponse ticketTypeSimpleResponse;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(adminTicketTypeController).setControllerAdvice(new ApiErrorHandler())
				.build();

		ticketTypeResponse = TicketTypeResponse.builder().id(1L).code("CHILD").displayName("Child Ticket")
				.priceMultiplier(new BigDecimal("0.70")).minAge(0).maxAge(12).requiresDocument(true)
				.documentType("Birth Certificate").active(true).category(TicketTypeCategory.CHILD).build();

		ticketTypeSimpleResponse = TicketTypeSimpleResponse.builder().id(1L).code("CHILD").displayName("Child Ticket")
				.priceMultiplier(new BigDecimal("0.70")).active(true).build();
	}

	@Test
	void createTicketType_ShouldReturnCreated_WhenValidRequest() throws Exception {
		TicketTypeCreateRequest createRequest = TicketTypeCreateRequest.builder().code("CHILD")
				.displayName("Child Ticket").priceMultiplier(new BigDecimal("0.70")).minAge(0).maxAge(12)
				.requiresDocument(true).documentType("Birth Certificate").active(true)
				.category(TicketTypeCategory.CHILD).build();

		when(ticketTypeService.createTicketType(any(TicketTypeCreateRequest.class))).thenReturn(ticketTypeResponse);

		mockMvc.perform(post("/api/admin/ticket-types").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.code").value("CHILD"));

		verify(ticketTypeService).createTicketType(any(TicketTypeCreateRequest.class));
	}

	@Test
	void createTicketType_ShouldReturnConflict_WhenDuplicateCode() throws Exception {
		TicketTypeCreateRequest createRequest = TicketTypeCreateRequest.builder().code("EXISTING")
				.displayName("Existing Ticket").priceMultiplier(new BigDecimal("1.00")).build();

		when(ticketTypeService.createTicketType(any(TicketTypeCreateRequest.class)))
				.thenThrow(new TicketTypeDuplicateException("EXISTING"));

		mockMvc.perform(post("/api/admin/ticket-types").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isConflict());

		verify(ticketTypeService).createTicketType(any(TicketTypeCreateRequest.class));
	}

	@Test
	void createTicketType_ShouldReturnBadRequest_WhenInvalidData() throws Exception {
		TicketTypeCreateRequest createRequest = TicketTypeCreateRequest.builder().code("").displayName("")
				.priceMultiplier(new BigDecimal("1.00")).build();

		mockMvc.perform(post("/api/admin/ticket-types").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isBadRequest());
	}

	@Test
	void getTicketTypeById_ShouldReturnTicketType_WhenExists() throws Exception {
		when(ticketTypeService.getTicketTypeById(1L)).thenReturn(ticketTypeResponse);

		mockMvc.perform(get("/api/admin/ticket-types/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.code").value("CHILD"));

		verify(ticketTypeService).getTicketTypeById(1L);
	}

	@Test
	void getTicketTypeById_ShouldReturnNotFound_WhenNotExists() throws Exception {
		when(ticketTypeService.getTicketTypeById(999L)).thenThrow(new TicketTypeNotFoundException(999L));

		mockMvc.perform(get("/api/admin/ticket-types/999")).andExpect(status().isNotFound());

		verify(ticketTypeService).getTicketTypeById(999L);
	}

	@Test
	void getTicketTypeByCode_ShouldReturnTicketType_WhenExists() throws Exception {
		when(ticketTypeService.getTicketTypeByCode("CHILD")).thenReturn(ticketTypeResponse);

		mockMvc.perform(get("/api/admin/ticket-types/code/CHILD")).andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("CHILD"));

		verify(ticketTypeService).getTicketTypeByCode("CHILD");
	}

	@Test
	void getTicketTypeByCode_ShouldReturnNotFound_WhenNotExists() throws Exception {
		when(ticketTypeService.getTicketTypeByCode("NONEXISTENT"))
				.thenThrow(new TicketTypeNotFoundException("NONEXISTENT"));

		mockMvc.perform(get("/api/admin/ticket-types/code/NONEXISTENT")).andExpect(status().isNotFound());

		verify(ticketTypeService).getTicketTypeByCode("NONEXISTENT");
	}

	@Test
	void getAllTicketTypes_ShouldReturnAll_WhenNoFilter() throws Exception {
		TicketTypeResponse adultResponse = TicketTypeResponse.builder().id(2L).code("STANDARD")
				.displayName("Standard Ticket").priceMultiplier(new BigDecimal("1.00")).active(true)
				.category(TicketTypeCategory.STANDARD).build();

		List<TicketTypeResponse> responses = Arrays.asList(ticketTypeResponse, adultResponse);

		when(ticketTypeService.getAllTicketTypes(null)).thenReturn(responses);

		mockMvc.perform(get("/api/admin/ticket-types")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].code").value("CHILD"))
				.andExpect(jsonPath("$[1].code").value("STANDARD"));

		verify(ticketTypeService).getAllTicketTypes(null);
	}

	@Test
	void getAllTicketTypes_ShouldReturnActive_WhenActiveTrue() throws Exception {
		List<TicketTypeResponse> activeResponses = Arrays.asList(ticketTypeResponse);

		when(ticketTypeService.getAllTicketTypes(true)).thenReturn(activeResponses);

		mockMvc.perform(get("/api/admin/ticket-types").param("active", "true")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].active").value(true));

		verify(ticketTypeService).getAllTicketTypes(true);
	}

	@Test
	void getAllTicketTypes_ShouldReturnInactive_WhenActiveFalse() throws Exception {
		TicketTypeResponse inactiveResponse = TicketTypeResponse.builder().id(3L).code("OLD")
				.displayName("Old Ticket Type").priceMultiplier(new BigDecimal("0.50")).active(false)
				.category(TicketTypeCategory.SPECIAL).build();

		List<TicketTypeResponse> inactiveResponses = Arrays.asList(inactiveResponse);

		when(ticketTypeService.getAllTicketTypes(false)).thenReturn(inactiveResponses);

		mockMvc.perform(get("/api/admin/ticket-types").param("active", "false")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].active").value(false));

		verify(ticketTypeService).getAllTicketTypes(false);
	}

	@Test
	void updateTicketType_ShouldReturnOk_WhenValidRequest() throws Exception {
		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated Child Ticket")
				.priceMultiplier(new BigDecimal("0.75")).build();

		TicketTypeResponse updatedResponse = TicketTypeResponse.builder().id(1L).code("CHILD")
				.displayName("Updated Child Ticket").priceMultiplier(new BigDecimal("0.75")).minAge(0).maxAge(12)
				.requiresDocument(true).documentType("Birth Certificate").active(true)
				.category(TicketTypeCategory.CHILD).build();

		when(ticketTypeService.updateTicketType(eq(1L), any(TicketTypeUpdateRequest.class)))
				.thenReturn(updatedResponse);

		mockMvc.perform(put("/api/admin/ticket-types/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").value("Updated Child Ticket"))
				.andExpect(jsonPath("$.priceMultiplier").value(0.75));

		verify(ticketTypeService).updateTicketType(eq(1L), any(TicketTypeUpdateRequest.class));
	}

	@Test
	void updateTicketType_ShouldReturnNotFound_WhenNotExists() throws Exception {
		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated").build();

		when(ticketTypeService.updateTicketType(eq(999L), any(TicketTypeUpdateRequest.class)))
				.thenThrow(new TicketTypeNotFoundException(999L));

		mockMvc.perform(put("/api/admin/ticket-types/999").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isNotFound());

		verify(ticketTypeService).updateTicketType(eq(999L), any(TicketTypeUpdateRequest.class));
	}

	@Test
	void deleteTicketType_ShouldReturnNoContent_WhenSuccessful() throws Exception {
		doNothing().when(ticketTypeService).deleteTicketType(1L);

		mockMvc.perform(delete("/api/admin/ticket-types/1")).andExpect(status().isNoContent());

		verify(ticketTypeService).deleteTicketType(1L);
	}

	@Test
	void deleteTicketType_ShouldReturnNotFound_WhenNotExists() throws Exception {
		doThrow(new TicketTypeNotFoundException(999L)).when(ticketTypeService).deleteTicketType(999L);

		mockMvc.perform(delete("/api/admin/ticket-types/999")).andExpect(status().isNotFound());

		verify(ticketTypeService).deleteTicketType(999L);
	}

	@Test
	void deleteTicketType_ShouldReturnConflict_WhenInUse() throws Exception {
		doThrow(new TicketTypeInUseException(1L)).when(ticketTypeService).deleteTicketType(1L);

		mockMvc.perform(delete("/api/admin/ticket-types/1")).andExpect(status().isConflict());

		verify(ticketTypeService).deleteTicketType(1L);
	}

	@Test
	void toggleTicketTypeActive_ShouldReturnOk_WhenSuccessful() throws Exception {
		TicketTypeResponse toggledResponse = TicketTypeResponse.builder().id(1L).code("CHILD")
				.displayName("Child Ticket").priceMultiplier(new BigDecimal("0.70")).active(false).build();

		when(ticketTypeService.toggleTicketTypeActiveStatus(1L)).thenReturn(toggledResponse);

		mockMvc.perform(patch("/api/admin/ticket-types/1/toggle-active")).andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(false));

		verify(ticketTypeService).toggleTicketTypeActiveStatus(1L);
	}

	@Test
	void toggleTicketTypeActive_ShouldReturnNotFound_WhenNotExists() throws Exception {
		when(ticketTypeService.toggleTicketTypeActiveStatus(999L)).thenThrow(new TicketTypeNotFoundException(999L));

		mockMvc.perform(patch("/api/admin/ticket-types/999/toggle-active")).andExpect(status().isNotFound());

		verify(ticketTypeService).toggleTicketTypeActiveStatus(999L);
	}

	@Test
	void getSimpleTicketTypes_ShouldReturnSimpleList_WhenActiveTrue() throws Exception {
		TicketTypeSimpleResponse adultSimpleResponse = TicketTypeSimpleResponse.builder().id(2L).code("ADULT")
				.displayName("Adult Ticket").priceMultiplier(new BigDecimal("1.00")).active(true).build();

		List<TicketTypeSimpleResponse> simpleResponses = Arrays.asList(ticketTypeSimpleResponse, adultSimpleResponse);

		when(ticketTypeService.getSimpleTicketTypes(true)).thenReturn(simpleResponses);

		mockMvc.perform(get("/api/admin/ticket-types/simple").param("active", "true")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].code").value("CHILD"))
				.andExpect(jsonPath("$[1].code").value("ADULT"));

		verify(ticketTypeService).getSimpleTicketTypes(true);
	}

	@Test
	void getSimpleTicketTypes_ShouldUseDefaultActiveTrue_WhenNoParam() throws Exception {
		List<TicketTypeSimpleResponse> simpleResponses = Arrays.asList(ticketTypeSimpleResponse);

		when(ticketTypeService.getSimpleTicketTypes(true)).thenReturn(simpleResponses);

		mockMvc.perform(get("/api/admin/ticket-types/simple")).andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1));

		verify(ticketTypeService).getSimpleTicketTypes(true);
	}
}