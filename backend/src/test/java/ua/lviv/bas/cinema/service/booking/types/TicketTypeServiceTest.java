package ua.lviv.bas.cinema.service.booking.types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
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

	@Test
	void createTicketType_Success() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("ADULT").displayName("Adult")
				.priceMultiplier(BigDecimal.ONE).minAge(18).maxAge(65).build();

		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		TicketTypeResponse response = TicketTypeResponse.builder().id(1L).code("ADULT").build();

		when(ticketTypeRepository.existsByCode("ADULT")).thenReturn(false);
		when(ticketTypeMapper.toTicketType(request)).thenReturn(ticketType);
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.createTicketType(request);

		assertThat(result.getId()).isEqualTo(1L);
		verify(validationService).validateAgeRange(18, 65);
		verify(ticketTypeRepository).existsByCode("ADULT");
		verify(ticketTypeRepository).save(ticketType);
	}

	@Test
	void createTicketType_WhenCodeExists_ShouldThrowException() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("ADULT").displayName("Adult")
				.priceMultiplier(BigDecimal.ONE).build();

		when(ticketTypeRepository.existsByCode("ADULT")).thenReturn(true);

		assertThatThrownBy(() -> ticketTypeService.createTicketType(request))
				.isInstanceOf(TicketTypeDuplicateException.class);

		verify(ticketTypeRepository).existsByCode("ADULT");
		verify(ticketTypeRepository, never()).save(any());
	}

	@Test
	void getTicketTypeById_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		TicketTypeResponse response = TicketTypeResponse.builder().id(1L).build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.getTicketTypeById(1L);

		assertThat(result.getId()).isEqualTo(1L);
		verify(ticketTypeRepository).findById(1L);
	}

	@Test
	void getTicketTypeById_WhenNotFound_ShouldThrowException() {
		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketTypeService.getTicketTypeById(1L))
				.isInstanceOf(TicketTypeNotFoundException.class);

		verify(ticketTypeRepository).findById(1L);
	}

	@Test
	void getTicketTypeByCode_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		TicketTypeResponse response = TicketTypeResponse.builder().id(1L).build();

		when(ticketTypeRepository.findByCode("ADULT")).thenReturn(Optional.of(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.getTicketTypeByCode("ADULT");

		assertThat(result.getId()).isEqualTo(1L);
		verify(ticketTypeRepository).findByCode("ADULT");
	}

	@Test
	void getAllTicketTypes_WhenActiveNull() {
		TicketType ticketType = new TicketType();
		TicketTypeResponse response = TicketTypeResponse.builder().build();

		when(ticketTypeRepository.findAll()).thenReturn(Arrays.asList(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		List<TicketTypeResponse> result = ticketTypeService.getAllTicketTypes(null);

		assertThat(result).hasSize(1);
		verify(ticketTypeRepository).findAll();
	}

	@Test
	void getAllTicketTypes_WhenActiveTrue() {
		TicketType ticketType = new TicketType();
		TicketTypeResponse response = TicketTypeResponse.builder().build();

		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Arrays.asList(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		List<TicketTypeResponse> result = ticketTypeService.getAllTicketTypes(true);

		assertThat(result).hasSize(1);
		verify(ticketTypeRepository).findByActiveTrue();
	}

	@Test
	void getTicketTypesWithFilters_WhenSearchNotEmpty() {
		TicketType ticketType = new TicketType();
		TicketTypeResponse response = TicketTypeResponse.builder().build();

		when(ticketTypeRepository.findByFilters(true, TicketTypeCategory.STANDARD, "ADULT"))
				.thenReturn(Arrays.asList(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		List<TicketTypeResponse> result = ticketTypeService.getTicketTypesWithFilters(true, TicketTypeCategory.STANDARD,
				"ADULT");

		assertThat(result).hasSize(1);
		verify(ticketTypeRepository).findByFilters(true, TicketTypeCategory.STANDARD, "ADULT");
	}

	@Test
	void getTicketTypesWithFilters_WhenSearchEmptyAndAllFiltersNull() {
		TicketType ticketType = new TicketType();
		TicketTypeResponse response = TicketTypeResponse.builder().build();

		when(ticketTypeRepository.findAll()).thenReturn(Arrays.asList(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		List<TicketTypeResponse> result = ticketTypeService.getTicketTypesWithFilters(null, null, " ");

		assertThat(result).hasSize(1);
		verify(ticketTypeRepository).findAll();
	}

	@Test
	void updateTicketType_Success() {
		TicketType ticketType = TicketType.builder().id(1L).minAge(18).maxAge(65).displayName("Old").build();

		TicketTypeUpdateRequest request = TicketTypeUpdateRequest.builder().displayName("Updated").minAge(21).maxAge(70)
				.build();

		TicketTypeResponse response = TicketTypeResponse.builder().displayName("Updated").build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.updateTicketType(1L, request);

		assertThat(result.getDisplayName()).isEqualTo("Updated");
		verify(ticketTypeRepository).findById(1L);
		verify(validationService).validateAgeRange(21, 70);
		verify(ticketTypeRepository).save(ticketType);
	}

	@Test
	void deleteTicketType_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeId(1L)).thenReturn(false);

		ticketTypeService.deleteTicketType(1L);

		verify(ticketTypeRepository).findById(1L);
		verify(ticketTypeRepository).delete(ticketType);
	}

	@Test
	void deleteTicketType_WhenInUse_ShouldThrowException() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeId(1L)).thenReturn(true);
		when(ticketRepository.countByTicketTypeId(1L)).thenReturn(5L);

		assertThatThrownBy(() -> ticketTypeService.deleteTicketType(1L)).isInstanceOf(TicketTypeInUseException.class);

		verify(ticketTypeRepository).findById(1L);
		verify(ticketTypeRepository, never()).delete(any());
	}

	@Test
	void toggleTicketTypeActiveStatus_ActivateSuccess() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setActive(false);

		TicketTypeResponse response = TicketTypeResponse.builder().active(true).build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.toggleTicketTypeActiveStatus(1L);

		assertThat(result.isActive()).isTrue();
		verify(ticketTypeRepository).save(ticketType);
	}

	@Test
	void toggleTicketTypeActiveStatus_DeactivateWithActiveTickets_ShouldThrowException() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setActive(true);

		List<TicketStatus> activeStatuses = List.of(TicketStatus.ACTIVE, TicketStatus.PENDING);

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeIdAndStatusIn(1L, activeStatuses)).thenReturn(true);
		when(ticketRepository.countByTicketTypeIdAndStatusIn(1L, activeStatuses)).thenReturn(3L);

		assertThatThrownBy(() -> ticketTypeService.toggleTicketTypeActiveStatus(1L))
				.isInstanceOf(TicketTypeInUseException.class);

		verify(ticketTypeRepository, never()).save(any());
	}

	@Test
	void getActiveTicketTypesForDropdown_Success() {
		TicketType ticketType = new TicketType();
		TicketTypeSimpleResponse response = TicketTypeSimpleResponse.builder().build();

		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Arrays.asList(ticketType));
		when(ticketTypeMapper.toTicketTypeSimpleResponse(ticketType)).thenReturn(response);

		List<TicketTypeSimpleResponse> result = ticketTypeService.getActiveTicketTypesForDropdown();

		assertThat(result).hasSize(1);
		verify(ticketTypeRepository).findByActiveTrue();
	}

	@Test
	void validateAgeForTicketType_Success() {
		TicketType ticketType = TicketType.builder().minAge(18).maxAge(65).build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(validationService.isAgeValidForTicketType(ticketType, 25)).thenReturn(true);

		boolean result = ticketTypeService.validateAgeForTicketType(1L, 25);

		assertThat(result).isTrue();
		verify(validationService).isAgeValidForTicketType(ticketType, 25);
	}

	@Test
	void existsByCode_Success() {
		when(ticketTypeRepository.existsByCode("ADULT")).thenReturn(true);

		boolean result = ticketTypeService.existsByCode("ADULT");

		assertThat(result).isTrue();
		verify(ticketTypeRepository).existsByCode("ADULT");
	}
}