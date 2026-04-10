package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ua.lviv.bas.cinema.domain.ticket.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.ticketType.request.TicketTypeRequest;
import ua.lviv.bas.cinema.dto.ticketType.response.TicketTypeResponse;
import ua.lviv.bas.cinema.exception.api.ApiErrorHandler;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.service.ticket.TicketTypeService;

@ExtendWith(MockitoExtension.class)
public class AdminTicketTypeControllerTest {

	private MockMvc mockMvc;
	private ObjectMapper objectMapper;

	@Mock
	private TicketTypeService ticketTypeService;

	@InjectMocks
	private AdminTicketTypeController adminTicketTypeController;

	private TicketTypeResponse ticketTypeResponse;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());

		mockMvc = MockMvcBuilders.standaloneSetup(adminTicketTypeController).setControllerAdvice(new ApiErrorHandler())
				.build();

		ticketTypeResponse = new TicketTypeResponse(1L, "Child Ticket", new BigDecimal("0.70"), 0, 12, true,
				"Birth Certificate", true, TicketTypeCategory.CHILD);
	}

	@Test
	void createTicketTypeShouldReturnCreated() throws Exception {
		TicketTypeRequest request = new TicketTypeRequest("Child Ticket", new BigDecimal("0.70"), 0, 12, true,
				"Birth Certificate", true, TicketTypeCategory.CHILD);

		when(ticketTypeService.createTicketType(any(TicketTypeRequest.class))).thenReturn(ticketTypeResponse);

		mockMvc.perform(post("/api/admin/ticket-types").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1)).andExpect(jsonPath("$.displayName").value("Child Ticket"));

		verify(ticketTypeService).createTicketType(any(TicketTypeRequest.class));
	}

	@Test
	void createTicketTypeShouldReturnConflictWhenDuplicate() throws Exception {
		TicketTypeRequest request = new TicketTypeRequest("Existing Ticket", new BigDecimal("1.00"), null, null, false,
				null, true, TicketTypeCategory.STANDARD);

		when(ticketTypeService.createTicketType(any(TicketTypeRequest.class)))
				.thenThrow(new TicketTypeDuplicateException("Existing Ticket"));

		mockMvc.perform(post("/api/admin/ticket-types").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isConflict());

		verify(ticketTypeService).createTicketType(any(TicketTypeRequest.class));
	}

	@Test
	void getTicketTypesShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<TicketTypeResponse> page = new PageImpl<>(List.of(ticketTypeResponse), pageable, 1);

		when(ticketTypeService.getTicketTypes(eq(null), eq(null), eq(null), any(Pageable.class))).thenReturn(page);

		PageResponse<TicketTypeResponse> result = adminTicketTypeController.getTicketTypes(null, null, null, pageable);

		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).id()).isEqualTo(1L);
		assertThat(result.content().get(0).displayName()).isEqualTo("Child Ticket");
		assertThat(result.totalElements()).isEqualTo(1L);
		assertThat(result.number()).isZero();
		assertThat(result.size()).isEqualTo(10);

		verify(ticketTypeService).getTicketTypes(eq(null), eq(null), eq(null), any(Pageable.class));
	}

	@Test
	void getTicketTypesWithFiltersShouldReturnFilteredPage() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<TicketTypeResponse> page = new PageImpl<>(List.of(ticketTypeResponse), pageable, 1);

		when(ticketTypeService.getTicketTypes(eq(true), eq(TicketTypeCategory.CHILD), eq("Child"), any(Pageable.class)))
				.thenReturn(page);

		PageResponse<TicketTypeResponse> result = adminTicketTypeController.getTicketTypes(true,
				TicketTypeCategory.CHILD, "Child", pageable);

		assertThat(result).isNotNull();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0).id()).isEqualTo(1L);
		assertThat(result.content().get(0).category()).isEqualTo(TicketTypeCategory.CHILD);

		verify(ticketTypeService).getTicketTypes(eq(true), eq(TicketTypeCategory.CHILD), eq("Child"),
				any(Pageable.class));
	}

	@Test
	void updateTicketTypeShouldReturnOk() throws Exception {
		TicketTypeRequest request = new TicketTypeRequest("Updated Child Ticket", new BigDecimal("0.70"), 0, 12, true,
				"Birth Certificate", true, TicketTypeCategory.CHILD);

		TicketTypeResponse updatedResponse = new TicketTypeResponse(1L, "Updated Child Ticket", new BigDecimal("0.70"),
				0, 12, true, "Birth Certificate", true, TicketTypeCategory.CHILD);

		when(ticketTypeService.updateTicketType(eq(1L), any(TicketTypeRequest.class))).thenReturn(updatedResponse);

		mockMvc.perform(put("/api/admin/ticket-types/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.displayName").value("Updated Child Ticket"));

		verify(ticketTypeService).updateTicketType(eq(1L), any(TicketTypeRequest.class));
	}

	@Test
	void updateTicketTypeShouldReturnNotFound() throws Exception {
		TicketTypeRequest request = new TicketTypeRequest("Updated", new BigDecimal("1.00"), null, null, false, null,
				true, TicketTypeCategory.STANDARD);

		when(ticketTypeService.updateTicketType(eq(999L), any(TicketTypeRequest.class)))
				.thenThrow(new TicketTypeNotFoundException(999L));

		mockMvc.perform(put("/api/admin/ticket-types/999").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isNotFound());

		verify(ticketTypeService).updateTicketType(eq(999L), any(TicketTypeRequest.class));
	}

	@Test
	void deleteTicketTypeShouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/api/admin/ticket-types/1")).andExpect(status().isNoContent());

		verify(ticketTypeService).deleteTicketType(1L);
	}

	@Test
	void deleteTicketTypeShouldReturnNotFound() throws Exception {
		doThrow(new TicketTypeNotFoundException(999L)).when(ticketTypeService).deleteTicketType(999L);

		mockMvc.perform(delete("/api/admin/ticket-types/999")).andExpect(status().isNotFound());

		verify(ticketTypeService).deleteTicketType(999L);
	}

	@Test
	void deleteTicketTypeShouldReturnConflictWhenInUse() throws Exception {
		doThrow(new TicketTypeInUseException(1L, "In use")).when(ticketTypeService).deleteTicketType(1L);

		mockMvc.perform(delete("/api/admin/ticket-types/1")).andExpect(status().isConflict());

		verify(ticketTypeService).deleteTicketType(1L);
	}

	@Test
	void toggleActiveShouldReturnOk() throws Exception {
		TicketTypeResponse toggledResponse = new TicketTypeResponse(1L, "Child Ticket", new BigDecimal("0.70"), 0, 12,
				true, "Birth Certificate", false, TicketTypeCategory.CHILD);

		when(ticketTypeService.toggleActiveStatus(1L)).thenReturn(toggledResponse);

		mockMvc.perform(patch("/api/admin/ticket-types/1/toggle")).andExpect(status().isOk())
				.andExpect(jsonPath("$.active").value(false));

		verify(ticketTypeService).toggleActiveStatus(1L);
	}

	@Test
	void toggleActiveShouldReturnNotFound() throws Exception {
		when(ticketTypeService.toggleActiveStatus(999L)).thenThrow(new TicketTypeNotFoundException(999L));

		mockMvc.perform(patch("/api/admin/ticket-types/999/toggle")).andExpect(status().isNotFound());

		verify(ticketTypeService).toggleActiveStatus(999L);
	}

	@Test
	void toggleActiveShouldReturnConflictWhenInUse() throws Exception {
		when(ticketTypeService.toggleActiveStatus(1L))
				.thenThrow(new TicketTypeInUseException(1L, "Cannot deactivate ticket type in use"));

		mockMvc.perform(patch("/api/admin/ticket-types/1/toggle")).andExpect(status().isConflict());

		verify(ticketTypeService).toggleActiveStatus(1L);
	}
}