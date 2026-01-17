package ua.lviv.bas.cinema.service.booking;

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
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeValidationException;
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

	@InjectMocks
	private TicketTypeService ticketTypeService;

	@Test
	void createTicketType_Success() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("ADULT").displayName("Adult")
				.priceMultiplier(BigDecimal.ONE).build();

		TicketType ticketType = new TicketType();
		ticketType.setId(1L);

		TicketTypeResponse response = TicketTypeResponse.builder().id(1L).code("ADULT").build();

		when(ticketTypeRepository.existsByCode("ADULT")).thenReturn(false);
		when(ticketTypeMapper.toTicketType(request)).thenReturn(ticketType);
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.createTicketType(request);

		assertThat(result.getId()).isEqualTo(1L);
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
	void createTicketType_WhenInvalidAgeRange_ShouldThrowException() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("CHILD").minAge(15).maxAge(10).build();

		assertThatThrownBy(() -> ticketTypeService.createTicketType(request))
				.isInstanceOf(TicketTypeValidationException.class);

		verify(ticketTypeRepository, never()).existsByCode(any());
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
	void getSimpleTicketTypes_WhenActiveTrue() {
		TicketType ticketType = new TicketType();
		TicketTypeSimpleResponse response = TicketTypeSimpleResponse.builder().build();

		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Arrays.asList(ticketType));
		when(ticketTypeMapper.toTicketTypeSimpleResponse(ticketType)).thenReturn(response);

		List<TicketTypeSimpleResponse> result = ticketTypeService.getSimpleTicketTypes(true);

		assertThat(result).hasSize(1);
		verify(ticketTypeRepository).findByActiveTrue();
	}

	@Test
	void updateTicketType_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);

		TicketTypeUpdateRequest request = TicketTypeUpdateRequest.builder().displayName("Updated").build();
		TicketTypeResponse response = TicketTypeResponse.builder().displayName("Updated").build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.updateTicketType(1L, request);

		assertThat(result.getDisplayName()).isEqualTo("Updated");
		verify(ticketTypeRepository).findById(1L);
		verify(ticketTypeRepository).save(ticketType);
	}

	@Test
	void updateTicketType_WhenInvalidAgeRange_ShouldThrowException() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setMinAge(10);
		ticketType.setMaxAge(20);

		TicketTypeUpdateRequest request = TicketTypeUpdateRequest.builder().minAge(25).maxAge(15).build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));

		assertThatThrownBy(() -> ticketTypeService.updateTicketType(1L, request))
				.isInstanceOf(TicketTypeValidationException.class);

		verify(ticketTypeRepository).findById(1L);
		verify(ticketTypeRepository, never()).save(any());
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
	void toggleTicketTypeActiveStatus_WhenActiveTicketsExist_ShouldThrowException() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setActive(true);

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeIdAndStatusIn(any(), any())).thenReturn(true);
		when(ticketRepository.countByTicketTypeIdAndStatusIn(any(), any())).thenReturn(3L);

		assertThatThrownBy(() -> ticketTypeService.toggleTicketTypeActiveStatus(1L))
				.isInstanceOf(TicketTypeInUseException.class);

		verify(ticketTypeRepository, never()).save(any());
	}

	@Test
	void validateAgeForTicketType_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setMinAge(18);
		ticketType.setMaxAge(65);

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));

		boolean result = ticketTypeService.validateAgeForTicketType(1L, 25);

		assertThat(result).isTrue();
	}

	@Test
	void isAgeValidForTicketType_WhenAgeValid() {
		TicketType ticketType = new TicketType();
		ticketType.setMinAge(18);
		ticketType.setMaxAge(65);

		boolean result = ticketTypeService.isAgeValidForTicketType(ticketType, 25);

		assertThat(result).isTrue();
	}

	@Test
	void isAgeValidForTicketType_WhenAgeTooLow() {
		TicketType ticketType = new TicketType();
		ticketType.setMinAge(18);
		ticketType.setMaxAge(65);

		boolean result = ticketTypeService.isAgeValidForTicketType(ticketType, 15);

		assertThat(result).isFalse();
	}

	@Test
	void existsByCode_Success() {
		when(ticketTypeRepository.existsByCode("ADULT")).thenReturn(true);

		boolean result = ticketTypeService.existsByCode("ADULT");

		assertThat(result).isTrue();
		verify(ticketTypeRepository).existsByCode("ADULT");
	}

	@Test
	void getFormattedAgeRange_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setMinAge(18);
		ticketType.setMaxAge(65);

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));

		String result = ticketTypeService.getFormattedAgeRange(1L);

		assertThat(result).isEqualTo("18-65 years");
	}

	@Test
	void getFormattedAgeRange_WhenNoAgeRestrictions() {
		TicketType ticketType = new TicketType();
		ticketType.setMinAge(null);
		ticketType.setMaxAge(null);

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));

		String result = ticketTypeService.getFormattedAgeRange(1L);

		assertThat(result).isEqualTo("No age restrictions");
	}
}