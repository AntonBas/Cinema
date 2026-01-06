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

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.exception.api.ApiErrorHandler;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.TicketTypeMapper;
import ua.lviv.bas.cinema.service.booking.TicketTypeService;

@ExtendWith(MockitoExtension.class)
class AdminTicketTypeControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper = new ObjectMapper();

	@Mock
	private TicketTypeService ticketTypeService;

	@Mock
	private TicketTypeMapper ticketTypeMapper;

	@InjectMocks
	private AdminTicketTypeController adminTicketTypeController;

	private TicketType ticketType;
	private TicketTypeResponse ticketTypeResponse;
	private TicketTypeSimpleResponse ticketTypeSimpleResponse;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(adminTicketTypeController).setControllerAdvice(new ApiErrorHandler())
				.build();

		ticketType = TicketType.builder().id(1L).code("CHILD").displayName("Child Ticket")
				.priceMultiplier(new BigDecimal("0.70")).minAge(0).maxAge(12).requiresDocument(true)
				.documentType("Birth Certificate").active(true).category(TicketTypeCategory.CHILD).build();

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

		when(ticketTypeService.createTicketType(any(TicketTypeCreateRequest.class))).thenReturn(ticketType);
		when(ticketTypeMapper.toResponseDto(ticketType)).thenReturn(ticketTypeResponse);

		mockMvc.perform(post("/api/admin/ticket-types").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(createRequest))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.code").value("CHILD"));

		verify(ticketTypeService).createTicketType(any(TicketTypeCreateRequest.class));
		verify(ticketTypeMapper).toResponseDto(ticketType);
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
	void getTicketTypeById_ShouldReturnTicketType_WhenExists() throws Exception {
		when(ticketTypeService.getTicketTypeById(1L)).thenReturn(ticketType);
		when(ticketTypeMapper.toResponseDto(ticketType)).thenReturn(ticketTypeResponse);

		mockMvc.perform(get("/api/admin/ticket-types/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.code").value("CHILD"));

		verify(ticketTypeService).getTicketTypeById(1L);
		verify(ticketTypeMapper).toResponseDto(ticketType);
	}

	@Test
	void getTicketTypeById_ShouldReturnNotFound_WhenNotExists() throws Exception {
		when(ticketTypeService.getTicketTypeById(999L)).thenThrow(new TicketTypeNotFoundException(999L));

		mockMvc.perform(get("/api/admin/ticket-types/999")).andExpect(status().isNotFound());

		verify(ticketTypeService).getTicketTypeById(999L);
	}

	@Test
	void deleteTicketType_ShouldReturnNoContent_WhenSuccessful() throws Exception {
		doNothing().when(ticketTypeService).deleteTicketType(1L);

		mockMvc.perform(delete("/api/admin/ticket-types/1")).andExpect(status().isNoContent());

		verify(ticketTypeService).deleteTicketType(1L);
	}

	@Test
	void deleteTicketType_ShouldReturnConflict_WhenInUse() throws Exception {
		doThrow(new TicketTypeInUseException(1L)).when(ticketTypeService).deleteTicketType(1L);

		mockMvc.perform(delete("/api/admin/ticket-types/1")).andExpect(status().isConflict());

		verify(ticketTypeService).deleteTicketType(1L);
	}

	@Test
	void toggleTicketTypeActive_ShouldReturnOk_WhenSuccessful() throws Exception {
		when(ticketTypeService.toggleTicketTypeActiveStatus(1L)).thenReturn(ticketType);
		when(ticketTypeMapper.toResponseDto(ticketType)).thenReturn(ticketTypeResponse);

		mockMvc.perform(patch("/api/admin/ticket-types/1/toggle-active")).andExpect(status().isOk());

		verify(ticketTypeService).toggleTicketTypeActiveStatus(1L);
		verify(ticketTypeMapper).toResponseDto(ticketType);
	}

	@Test
	void getAllTicketTypes_ShouldReturnAll_WhenNoFilter() throws Exception {
		List<TicketType> ticketTypes = Arrays.asList(ticketType);
		List<TicketTypeResponse> responses = Arrays.asList(ticketTypeResponse);

		when(ticketTypeService.getAllTicketTypes()).thenReturn(ticketTypes);
		when(ticketTypeMapper.toResponseDtoList(ticketTypes)).thenReturn(responses);

		mockMvc.perform(get("/api/admin/ticket-types")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value(1));

		verify(ticketTypeService).getAllTicketTypes();
		verify(ticketTypeMapper).toResponseDtoList(ticketTypes);
	}

	@Test
	void getAllTicketTypes_ShouldReturnActive_WhenActiveTrue() throws Exception {
		List<TicketType> activeTicketTypes = Arrays.asList(ticketType);
		List<TicketTypeResponse> responses = Arrays.asList(ticketTypeResponse);

		when(ticketTypeService.getAllActiveTicketTypes()).thenReturn(activeTicketTypes);
		when(ticketTypeMapper.toResponseDtoList(activeTicketTypes)).thenReturn(responses);

		mockMvc.perform(get("/api/admin/ticket-types").param("active", "true")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].active").value(true));

		verify(ticketTypeService).getAllActiveTicketTypes();
	}

	@Test
	void updateTicketType_ShouldReturnOk_WhenValidRequest() throws Exception {
		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated")
				.priceModifier(new BigDecimal("0.80")).build();

		when(ticketTypeService.updateTicketType(eq(1L), any(TicketTypeUpdateRequest.class))).thenReturn(ticketType);
		when(ticketTypeMapper.toResponseDto(ticketType)).thenReturn(ticketTypeResponse);

		mockMvc.perform(put("/api/admin/ticket-types/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updateRequest))).andExpect(status().isOk());

		verify(ticketTypeService).updateTicketType(eq(1L), any(TicketTypeUpdateRequest.class));
		verify(ticketTypeMapper).toResponseDto(ticketType);
	}

	@Test
	void getTicketTypeByCode_ShouldReturnTicketType_WhenExists() throws Exception {
		when(ticketTypeService.getTicketTypeByCode("CHILD")).thenReturn(ticketType);
		when(ticketTypeMapper.toResponseDto(ticketType)).thenReturn(ticketTypeResponse);

		mockMvc.perform(get("/api/admin/ticket-types/code/CHILD")).andExpect(status().isOk())
				.andExpect(jsonPath("$.code").value("CHILD"));

		verify(ticketTypeService).getTicketTypeByCode("CHILD");
		verify(ticketTypeMapper).toResponseDto(ticketType);
	}

	@Test
	void getSimpleTicketTypes_ShouldReturnSimpleList_WhenActiveTrue() throws Exception {
		List<TicketType> activeTicketTypes = Arrays.asList(ticketType);
		List<TicketTypeSimpleResponse> simpleResponses = Arrays.asList(ticketTypeSimpleResponse);

		when(ticketTypeService.getAllActiveTicketTypes()).thenReturn(activeTicketTypes);
		when(ticketTypeMapper.toSimpleDtoList(activeTicketTypes)).thenReturn(simpleResponses);

		mockMvc.perform(get("/api/admin/ticket-types/simple").param("active", "true")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("CHILD"));

		verify(ticketTypeService).getAllActiveTicketTypes();
		verify(ticketTypeMapper).toSimpleDtoList(activeTicketTypes);
	}
}