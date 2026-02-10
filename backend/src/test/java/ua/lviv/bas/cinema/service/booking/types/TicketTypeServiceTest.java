package ua.lviv.bas.cinema.service.booking.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.TicketTypeMapper;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;

@ExtendWith(MockitoExtension.class)
public class TicketTypeServiceTest {

	@Mock
	private TicketTypeRepository ticketTypeRepository;

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketTypeMapper ticketTypeMapper;

	@Mock
	private TicketTypeValidationService validationService;

	@InjectMocks
	private TicketTypeService ticketTypeService;

	private final Long TICKET_TYPE_ID = 1L;
	private final String TICKET_TYPE_CODE = "ADULT";
	private final BigDecimal PRICE_MULTIPLIER = new BigDecimal("1.0");

	@Test
	void createTicketType_Success() {
		TicketTypeCreateRequest request = createTicketTypeRequest();
		TicketType ticketType = createTicketType();
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.existsByCode(TICKET_TYPE_CODE)).thenReturn(false);
		when(ticketTypeMapper.toTicketType(request)).thenReturn(ticketType);
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.createTicketType(request);

		assertThat(result).isEqualTo(response);
		verify(validationService).validateAgeRange(18, 65);
		verify(ticketTypeRepository).save(ticketType);
	}

	@Test
	void createTicketType_DuplicateCode_ThrowsException() {
		TicketTypeCreateRequest request = createTicketTypeRequest();

		when(ticketTypeRepository.existsByCode(TICKET_TYPE_CODE)).thenReturn(true);

		assertThatThrownBy(() -> ticketTypeService.createTicketType(request))
				.isInstanceOf(TicketTypeDuplicateException.class);
	}

	@Test
	void getTicketTypeById_Success() {
		TicketType ticketType = createTicketType();
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.getTicketTypeById(TICKET_TYPE_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getTicketTypeById_NotFound_ThrowsException() {
		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketTypeService.getTicketTypeById(TICKET_TYPE_ID))
				.isInstanceOf(TicketTypeNotFoundException.class);
	}

	@Test
	void getTicketTypeByCode_Success() {
		TicketType ticketType = createTicketType();
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.findByCode(TICKET_TYPE_CODE)).thenReturn(Optional.of(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.getTicketTypeByCode(TICKET_TYPE_CODE);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getTicketTypes_WithFilters_Success() {
		TicketType ticketType = createTicketType();
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.findByFilters(true, TicketTypeCategory.STANDARD, "search"))
				.thenReturn(List.of(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		List<TicketTypeResponse> result = ticketTypeService.getTicketTypes(true, TicketTypeCategory.STANDARD, "search");

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void updateTicketType_Success() {
		TicketType ticketType = createTicketType();
		TicketTypeUpdateRequest request = TicketTypeUpdateRequest.builder().displayName("Updated Name").minAge(21)
				.maxAge(70).build();
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.updateTicketType(TICKET_TYPE_ID, request);

		assertThat(result).isEqualTo(response);
		verify(validationService).validateAgeRange(21, 70);
		verify(ticketTypeMapper).updateTicketTypeFromRequest(ticketType, request);
	}

	@Test
	void deleteTicketType_Success() {
		TicketType ticketType = createTicketType();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID), any(),
				any(LocalDateTime.class))).thenReturn(0L);

		ticketTypeService.deleteTicketType(TICKET_TYPE_ID);

		verify(ticketTypeRepository).delete(ticketType);
	}

	@Test
	void deleteTicketType_WithFutureTickets_ThrowsException() {
		TicketType ticketType = createTicketType();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID), any(),
				any(LocalDateTime.class))).thenReturn(3L);

		assertThatThrownBy(() -> ticketTypeService.deleteTicketType(TICKET_TYPE_ID))
				.isInstanceOf(TicketTypeInUseException.class);
	}

	@Test
	void toggleTicketTypeActiveStatus_ActivateSuccess() {
		TicketType ticketType = createTicketType();
		ticketType.setActive(false);
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.toggleTicketTypeActiveStatus(TICKET_TYPE_ID);

		assertThat(result).isEqualTo(response);
		assertThat(ticketType.isActive()).isTrue();
		verify(ticketRepository, never()).countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(any(), any(),
				any());
	}

	@Test
	void toggleTicketTypeActiveStatus_DeactivateSuccess() {
		TicketType ticketType = createTicketType();
		ticketType.setActive(true);
		TicketTypeResponse response = createTicketTypeResponse();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID), any(),
				any(LocalDateTime.class))).thenReturn(0L);
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.toggleTicketTypeActiveStatus(TICKET_TYPE_ID);

		assertThat(result).isEqualTo(response);
		assertThat(ticketType.isActive()).isFalse();
	}

	@Test
	void toggleTicketTypeActiveStatus_DeactivateWithFutureTickets_ThrowsException() {
		TicketType ticketType = createTicketType();
		ticketType.setActive(true);

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(eq(TICKET_TYPE_ID), any(),
				any(LocalDateTime.class))).thenReturn(2L);

		assertThatThrownBy(() -> ticketTypeService.toggleTicketTypeActiveStatus(TICKET_TYPE_ID))
				.isInstanceOf(TicketTypeInUseException.class);
	}

	@Test
	void getActiveTicketTypesForDropdown_Success() {
		TicketType ticketType = createTicketType();
		TicketTypeSimpleResponse simpleResponse = TicketTypeSimpleResponse.builder().build();

		when(ticketTypeRepository.findByActiveTrue()).thenReturn(List.of(ticketType));
		when(ticketTypeMapper.toTicketTypeSimpleResponse(ticketType)).thenReturn(simpleResponse);

		List<TicketTypeSimpleResponse> result = ticketTypeService.getActiveTicketTypesForDropdown();

		assertThat(result).hasSize(1);
	}

	@Test
	void validateAgeForTicketType_Success() {
		TicketType ticketType = createTicketType();

		when(ticketTypeRepository.findById(TICKET_TYPE_ID)).thenReturn(Optional.of(ticketType));
		when(validationService.isAgeValidForTicketType(ticketType, 25)).thenReturn(true);

		boolean result = ticketTypeService.validateAgeForTicketType(TICKET_TYPE_ID, 25);

		assertThat(result).isTrue();
	}

	private TicketTypeCreateRequest createTicketTypeRequest() {
		return TicketTypeCreateRequest.builder().code(TICKET_TYPE_CODE).displayName("Adult")
				.priceMultiplier(PRICE_MULTIPLIER).minAge(18).maxAge(65).category(TicketTypeCategory.STANDARD).build();
	}

	private TicketType createTicketType() {
		return TicketType.builder().id(TICKET_TYPE_ID).code(TICKET_TYPE_CODE).displayName("Adult")
				.priceMultiplier(PRICE_MULTIPLIER).minAge(18).maxAge(65).category(TicketTypeCategory.STANDARD)
				.active(true).build();
	}

	private TicketTypeResponse createTicketTypeResponse() {
		return TicketTypeResponse.builder().id(TICKET_TYPE_ID).code(TICKET_TYPE_CODE).displayName("Adult")
				.priceMultiplier(PRICE_MULTIPLIER).active(true).build();
	}
}