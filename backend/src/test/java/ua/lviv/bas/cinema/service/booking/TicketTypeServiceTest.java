package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
				.priceMultiplier(BigDecimal.ONE).active(true).build();

		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setCode("ADULT");

		TicketTypeResponse response = TicketTypeResponse.builder().id(1L).code("ADULT").build();

		when(ticketTypeRepository.existsByCode("ADULT")).thenReturn(false);
		when(ticketTypeMapper.toTicketType(request)).thenReturn(ticketType);
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.createTicketType(request);

		assertThat(result.getCode()).isEqualTo("ADULT");
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
	}

	@Test
	void createTicketType_WhenInvalidAgeRange_ShouldThrowException() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("CHILD").displayName("Child")
				.priceMultiplier(BigDecimal.ONE).minAge(15).maxAge(10).build();

		assertThatThrownBy(() -> ticketTypeService.createTicketType(request))
				.isInstanceOf(TicketTypeValidationException.class);
	}

	@Test
	void getTicketTypeById_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setCode("ADULT");

		TicketTypeResponse response = TicketTypeResponse.builder().id(1L).code("ADULT").build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.getTicketTypeById(1L);

		assertThat(result.getCode()).isEqualTo("ADULT");
	}

	@Test
	void getTicketTypeById_WhenNotFound_ShouldThrowException() {
		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketTypeService.getTicketTypeById(1L))
				.isInstanceOf(TicketTypeNotFoundException.class);
	}

	@Test
	void getTicketTypeByCode_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setCode("ADULT");

		TicketTypeResponse response = TicketTypeResponse.builder().code("ADULT").build();

		when(ticketTypeRepository.findByCode("ADULT")).thenReturn(Optional.of(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.getTicketTypeByCode("ADULT");

		assertThat(result.getCode()).isEqualTo("ADULT");
	}

	@Test
	void getAllTicketTypes_WhenActiveNull() {
		TicketType ticketType1 = new TicketType();
		ticketType1.setActive(true);

		TicketType ticketType2 = new TicketType();
		ticketType2.setActive(false);

		TicketTypeResponse response1 = TicketTypeResponse.builder().id(1L).build();
		TicketTypeResponse response2 = TicketTypeResponse.builder().id(2L).build();

		when(ticketTypeRepository.findAll()).thenReturn(Arrays.asList(ticketType1, ticketType2));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType1)).thenReturn(response1);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType2)).thenReturn(response2);

		List<TicketTypeResponse> result = ticketTypeService.getAllTicketTypes(null);

		assertThat(result).hasSize(2);
	}

	@Test
	void getAllTicketTypes_WhenActiveTrue() {
		TicketType ticketType = new TicketType();
		ticketType.setActive(true);

		TicketTypeResponse response = TicketTypeResponse.builder().id(1L).build();

		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Arrays.asList(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		List<TicketTypeResponse> result = ticketTypeService.getAllTicketTypes(true);

		assertThat(result).hasSize(1);
	}

	@Test
	void getSimpleTicketTypes_WhenActiveTrue() {
		TicketType ticketType = new TicketType();
		ticketType.setActive(true);

		TicketTypeSimpleResponse response = TicketTypeSimpleResponse.builder().id(1L).build();

		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Arrays.asList(ticketType));
		when(ticketTypeMapper.toTicketTypeSimpleResponse(ticketType)).thenReturn(response);

		List<TicketTypeSimpleResponse> result = ticketTypeService.getSimpleTicketTypes(true);

		assertThat(result).hasSize(1);
	}

	@Test
	void updateTicketType_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setMinAge(0);
		ticketType.setMaxAge(12);

		TicketTypeUpdateRequest request = TicketTypeUpdateRequest.builder().displayName("Updated Name").build();

		TicketTypeResponse response = TicketTypeResponse.builder().id(1L).displayName("Updated Name").build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		doNothing().when(ticketTypeMapper).updateTicketTypeFromRequest(ticketType, request);
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.updateTicketType(1L, request);

		assertThat(result.getDisplayName()).isEqualTo("Updated Name");
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
	}

	@Test
	void deleteTicketType_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeId(1L)).thenReturn(false);
		doNothing().when(ticketTypeRepository).delete(ticketType);

		ticketTypeService.deleteTicketType(1L);

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
	}

	@Test
	void toggleTicketTypeActiveStatus_Success() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setActive(true);

		TicketTypeResponse response = TicketTypeResponse.builder().id(1L).active(false).build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeIdAndStatusIn(eq(1L), anyList())).thenReturn(false);
		when(ticketTypeRepository.save(ticketType)).thenReturn(ticketType);
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		TicketTypeResponse result = ticketTypeService.toggleTicketTypeActiveStatus(1L);

		assertThat(result.isActive()).isFalse();
	}

	@Test
	void toggleTicketTypeActiveStatus_WhenActiveTicketsExist_ShouldThrowException() {
		TicketType ticketType = new TicketType();
		ticketType.setId(1L);
		ticketType.setActive(true);

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeIdAndStatusIn(eq(1L), anyList())).thenReturn(true);
		when(ticketRepository.countByTicketTypeIdAndStatusIn(eq(1L), anyList())).thenReturn(3L);

		assertThatThrownBy(() -> ticketTypeService.toggleTicketTypeActiveStatus(1L))
				.isInstanceOf(TicketTypeInUseException.class);
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
	void isAgeValidForTicketType_WhenNullAge() {
		TicketType ticketType = new TicketType();
		ticketType.setMinAge(null);
		ticketType.setMaxAge(null);

		boolean result = ticketTypeService.isAgeValidForTicketType(ticketType, null);

		assertThat(result).isTrue();
	}

	@Test
	void existsByCode_Success() {
		when(ticketTypeRepository.existsByCode("ADULT")).thenReturn(true);

		boolean result = ticketTypeService.existsByCode("ADULT");

		assertThat(result).isTrue();
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

	@Test
	void getAllTicketTypes_WhenActiveFalse() {
		TicketType ticketType = new TicketType();
		ticketType.setActive(false);

		TicketTypeResponse response = TicketTypeResponse.builder().id(1L).build();

		when(ticketTypeRepository.findByActiveFalse()).thenReturn(Arrays.asList(ticketType));
		when(ticketTypeMapper.toTicketTypeResponse(ticketType)).thenReturn(response);

		List<TicketTypeResponse> result = ticketTypeService.getAllTicketTypes(false);

		assertThat(result).hasSize(1);
	}

	@Test
	void getSimpleTicketTypes_WhenActiveNull() {
		TicketType ticketType = new TicketType();
		ticketType.setActive(true);

		TicketTypeSimpleResponse response = TicketTypeSimpleResponse.builder().id(1L).build();

		when(ticketTypeRepository.findByActiveTrue()).thenReturn(Arrays.asList(ticketType));
		when(ticketTypeMapper.toTicketTypeSimpleResponse(ticketType)).thenReturn(response);

		List<TicketTypeSimpleResponse> result = ticketTypeService.getSimpleTicketTypes(null);

		assertThat(result).hasSize(1);
	}
}