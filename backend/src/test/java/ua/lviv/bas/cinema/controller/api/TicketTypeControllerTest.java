package ua.lviv.bas.cinema.controller.api;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.exception.api.ApiErrorHandler;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.TicketTypeMapper;
import ua.lviv.bas.cinema.service.booking.TicketTypeService;

@ExtendWith(MockitoExtension.class)
class TicketTypeControllerTest {

	private MockMvc mockMvc;

	@Mock
	private TicketTypeService ticketTypeService;

	@Mock
	private TicketTypeMapper ticketTypeMapper;

	@InjectMocks
	private TicketTypeController ticketTypeController;

	private TicketType ticketType;
	private TicketTypeResponse ticketTypeResponse;
	private TicketTypeSimpleResponse ticketTypeSimpleResponse;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(ticketTypeController).setControllerAdvice(new ApiErrorHandler())
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
	void getAllActiveTicketTypes_ShouldReturnActiveTicketTypes() throws Exception {
		List<TicketType> activeTicketTypes = Arrays.asList(ticketType);
		List<TicketTypeResponse> responses = Arrays.asList(ticketTypeResponse);

		when(ticketTypeService.getAllActiveTicketTypes()).thenReturn(activeTicketTypes);
		when(ticketTypeMapper.toResponseDtoList(activeTicketTypes)).thenReturn(responses);

		mockMvc.perform(get("/api/ticket-types")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1))
				.andExpect(jsonPath("$[0].active").value(true));

		verify(ticketTypeService).getAllActiveTicketTypes();
		verify(ticketTypeMapper).toResponseDtoList(activeTicketTypes);
	}

	@Test
	void getTicketTypeById_ShouldReturnTicketType_WhenExists() throws Exception {
		when(ticketTypeService.getTicketTypeById(1L)).thenReturn(ticketType);
		when(ticketTypeMapper.toResponseDto(ticketType)).thenReturn(ticketTypeResponse);

		mockMvc.perform(get("/api/ticket-types/1")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));

		verify(ticketTypeService).getTicketTypeById(1L);
		verify(ticketTypeMapper).toResponseDto(ticketType);
	}

	@Test
	void getTicketTypeById_ShouldReturnNotFound_WhenNotExists() throws Exception {
		when(ticketTypeService.getTicketTypeById(999L)).thenThrow(new TicketTypeNotFoundException(999L));

		mockMvc.perform(get("/api/ticket-types/999")).andExpect(status().isNotFound());

		verify(ticketTypeService).getTicketTypeById(999L);
	}

	@Test
	void getSimpleActiveTicketTypes_ShouldReturnSimpleList() throws Exception {
		List<TicketType> activeTicketTypes = Arrays.asList(ticketType);
		List<TicketTypeSimpleResponse> simpleResponses = Arrays.asList(ticketTypeSimpleResponse);

		when(ticketTypeService.getAllActiveTicketTypes()).thenReturn(activeTicketTypes);
		when(ticketTypeMapper.toSimpleDtoList(activeTicketTypes)).thenReturn(simpleResponses);

		mockMvc.perform(get("/api/ticket-types/simple")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].code").value("CHILD"));

		verify(ticketTypeService).getAllActiveTicketTypes();
		verify(ticketTypeMapper).toSimpleDtoList(activeTicketTypes);
	}

	@Test
	void validateAgeForTicketType_ShouldReturnTrue_WhenValidAge() throws Exception {
		when(ticketTypeService.validateAgeForTicketType(1L, 10)).thenReturn(true);

		mockMvc.perform(get("/api/ticket-types/age-validation").param("ticketTypeId", "1").param("age", "10"))
				.andExpect(status().isOk()).andExpect(content().string("true"));

		verify(ticketTypeService).validateAgeForTicketType(1L, 10);
	}

	@Test
	void getFormattedAgeRange_ShouldReturnAgeRange() throws Exception {
		when(ticketTypeService.getFormattedAgeRange(1L)).thenReturn("0-12 years");

		mockMvc.perform(get("/api/ticket-types/1/age-range")).andExpect(status().isOk())
				.andExpect(content().string("0-12 years"));

		verify(ticketTypeService).getFormattedAgeRange(1L);
	}

	@Test
	void getTicketTypesForAge_ShouldReturnAvailableTicketTypes() throws Exception {
		TicketType adultType = TicketType.builder().id(2L).code("ADULT").displayName("Adult Ticket").minAge(18)
				.maxAge(null).priceMultiplier(new BigDecimal("1.00")).active(true).build();

		TicketTypeSimpleResponse adultSimpleResponse = TicketTypeSimpleResponse.builder().id(2L).code("ADULT")
				.displayName("Adult Ticket").priceMultiplier(new BigDecimal("1.00")).active(true).build();

		List<TicketType> allActive = Arrays.asList(ticketType, adultType);
		List<TicketTypeSimpleResponse> allSimpleResponses = Arrays.asList(ticketTypeSimpleResponse,
				adultSimpleResponse);

		when(ticketTypeService.getAllActiveTicketTypes()).thenReturn(allActive);
		when(ticketTypeMapper.toSimpleDtoList(anyList())).thenReturn(allSimpleResponses);

		mockMvc.perform(get("/api/ticket-types/available-for-age").param("age", "10")).andExpect(status().isOk());

		verify(ticketTypeService).getAllActiveTicketTypes();
		verify(ticketTypeMapper).toSimpleDtoList(anyList());
	}
}