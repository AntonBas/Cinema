package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeValidationException;
import ua.lviv.bas.cinema.mapper.TicketTypeMapper;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceTest {

	@Mock
	private TicketTypeRepository ticketTypeRepository;

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private TicketTypeMapper ticketTypeMapper;

	@InjectMocks
	private TicketTypeService ticketTypeService;

	private TicketTypeCreateRequest validCreateRequest;
	private TicketType ticketType;

	@BeforeEach
	void setUp() {
		validCreateRequest = TicketTypeCreateRequest.builder().code("CHILD").displayName("Child Ticket")
				.priceMultiplier(new BigDecimal("0.70")).minAge(0).maxAge(12).requiresDocument(true)
				.documentType("Birth Certificate").active(true).category(TicketTypeCategory.CHILD).build();

		ticketType = TicketType.builder().id(1L).code("CHILD").displayName("Child Ticket")
				.priceMultiplier(new BigDecimal("0.70")).minAge(0).maxAge(12).requiresDocument(true)
				.documentType("Birth Certificate").active(true).category(TicketTypeCategory.CHILD).build();
	}

	@Test
	void createTicketType_ShouldCreateSuccessfully() {
		when(ticketTypeRepository.existsByCode("CHILD")).thenReturn(false);
		when(ticketTypeMapper.toEntity(validCreateRequest)).thenReturn(ticketType);
		when(ticketTypeRepository.save(any(TicketType.class))).thenReturn(ticketType);

		TicketType result = ticketTypeService.createTicketType(validCreateRequest);

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo("CHILD");
		assertThat(result.getDisplayName()).isEqualTo("Child Ticket");
		verify(ticketTypeRepository).existsByCode("CHILD");
		verify(ticketTypeMapper).toEntity(validCreateRequest);
		verify(ticketTypeRepository).save(ticketType);
	}

	@Test
	void createTicketType_ShouldThrowDuplicateException_WhenCodeExists() {
		when(ticketTypeRepository.existsByCode("CHILD")).thenReturn(true);

		assertThatThrownBy(() -> ticketTypeService.createTicketType(validCreateRequest))
				.isInstanceOf(TicketTypeDuplicateException.class).hasMessageContaining("CHILD");

		verify(ticketTypeRepository).existsByCode("CHILD");
		verify(ticketTypeMapper, never()).toEntity(any());
		verify(ticketTypeRepository, never()).save(any());
	}

	@Test
	void createTicketType_ShouldThrowValidationException_WhenInvalidAgeRange() {
		TicketTypeCreateRequest invalidRequest = TicketTypeCreateRequest.builder().code("INVALID")
				.displayName("Invalid").minAge(20).maxAge(10).build();

		assertThatThrownBy(() -> ticketTypeService.createTicketType(invalidRequest))
				.isInstanceOf(TicketTypeValidationException.class).hasMessageContaining("Invalid age range");

		verify(ticketTypeRepository, never()).existsByCode(any());
		verify(ticketTypeMapper, never()).toEntity(any());
		verify(ticketTypeRepository, never()).save(any());
	}

	@Test
	void getTicketTypeById_ShouldReturnTicketType_WhenExists() {
		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));

		TicketType result = ticketTypeService.getTicketTypeById(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		verify(ticketTypeRepository).findById(1L);
	}

	@Test
	void getTicketTypeById_ShouldThrowNotFoundException_WhenNotExists() {
		when(ticketTypeRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketTypeService.getTicketTypeById(999L))
				.isInstanceOf(TicketTypeNotFoundException.class).hasMessageContaining("999");

		verify(ticketTypeRepository).findById(999L);
	}

	@Test
	void getTicketTypeByCode_ShouldReturnTicketType_WhenExists() {
		when(ticketTypeRepository.findByCode("CHILD")).thenReturn(Optional.of(ticketType));

		TicketType result = ticketTypeService.getTicketTypeByCode("CHILD");

		assertThat(result).isNotNull();
		assertThat(result.getCode()).isEqualTo("CHILD");
		verify(ticketTypeRepository).findByCode("CHILD");
	}

	@Test
	void getTicketTypeByCode_ShouldThrowNotFoundException_WhenNotExists() {
		when(ticketTypeRepository.findByCode("NONEXISTENT")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ticketTypeService.getTicketTypeByCode("NONEXISTENT"))
				.isInstanceOf(TicketTypeNotFoundException.class).hasMessageContaining("NONEXISTENT");

		verify(ticketTypeRepository).findByCode("NONEXISTENT");
	}

	@Test
	void getAllTicketTypes_ShouldReturnAllTicketTypes() {
		List<TicketType> ticketTypes = Arrays.asList(ticketType,
				TicketType.builder().id(2L).code("ADULT").displayName("Adult").build());
		when(ticketTypeRepository.findAll()).thenReturn(ticketTypes);

		List<TicketType> result = ticketTypeService.getAllTicketTypes();

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getCode()).isEqualTo("CHILD");
		assertThat(result.get(1).getCode()).isEqualTo("ADULT");
		verify(ticketTypeRepository).findAll();
	}

	@Test
	void getAllActiveTicketTypes_ShouldReturnActiveTicketTypes() {
		List<TicketType> activeTicketTypes = Arrays.asList(ticketType,
				TicketType.builder().id(2L).code("ADULT").displayName("Adult").active(true).build());
		when(ticketTypeRepository.findByActiveTrue()).thenReturn(activeTicketTypes);

		List<TicketType> result = ticketTypeService.getAllActiveTicketTypes();

		assertThat(result).hasSize(2);
		assertThat(result.get(0).isActive()).isTrue();
		assertThat(result.get(1).isActive()).isTrue();
		verify(ticketTypeRepository).findByActiveTrue();
	}

	@Test
	void updateTicketType_ShouldUpdateSuccessfully() {
		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketTypeRepository.save(any(TicketType.class))).thenReturn(ticketType);

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated Child Ticket")
				.priceModifier(new BigDecimal("0.65")).build();

		TicketType result = ticketTypeService.updateTicketType(1L, updateRequest);

		assertThat(result).isNotNull();
		verify(ticketTypeMapper).updateEntity(ticketType, updateRequest);
		verify(ticketTypeRepository).save(ticketType);
	}

	@Test
	void updateTicketType_ShouldThrowNotFoundException_WhenIdNotExists() {
		when(ticketTypeRepository.findById(999L)).thenReturn(Optional.empty());

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated").build();

		assertThatThrownBy(() -> ticketTypeService.updateTicketType(999L, updateRequest))
				.isInstanceOf(TicketTypeNotFoundException.class);

		verify(ticketTypeRepository).findById(999L);
		verify(ticketTypeMapper, never()).updateEntity(any(), any());
		verify(ticketTypeRepository, never()).save(any());
	}

	@Test
	void updateTicketType_ShouldValidateAgeRange_WhenUpdatingAges() {
		TicketType existing = TicketType.builder().id(1L).code("TEST").displayName("Test").minAge(10).maxAge(20)
				.build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(ticketTypeRepository.save(any(TicketType.class))).thenReturn(existing);

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().minAge(15).maxAge(25).build();

		TicketType result = ticketTypeService.updateTicketType(1L, updateRequest);

		assertThat(result).isNotNull();
		verify(ticketTypeRepository).findById(1L);
		verify(ticketTypeRepository).save(existing);
	}

	@Test
	void updateTicketType_ShouldThrowValidationException_WhenInvalidAgeRange() {
		TicketType existing = TicketType.builder().id(1L).code("TEST").build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(existing));

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().minAge(30).maxAge(20).build();

		assertThatThrownBy(() -> ticketTypeService.updateTicketType(1L, updateRequest))
				.isInstanceOf(TicketTypeValidationException.class).hasMessageContaining("Invalid age range");

		verify(ticketTypeRepository).findById(1L);
		verify(ticketTypeMapper, never()).updateEntity(any(), any());
		verify(ticketTypeRepository, never()).save(any());
	}

	@Test
	void deleteTicketType_ShouldDeleteSuccessfully_WhenNotInUse() {
		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeId(1L)).thenReturn(false);
		doNothing().when(ticketTypeRepository).delete(ticketType);

		ticketTypeService.deleteTicketType(1L);

		verify(ticketTypeRepository).findById(1L);
		verify(ticketRepository).existsByTicketTypeId(1L);
		verify(ticketTypeRepository).delete(ticketType);
	}

	@Test
	void deleteTicketType_ShouldThrowInUseException_WhenInUse() {
		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeId(1L)).thenReturn(true);
		when(ticketRepository.countByTicketTypeId(1L)).thenReturn(5L);

		assertThatThrownBy(() -> ticketTypeService.deleteTicketType(1L)).isInstanceOf(TicketTypeInUseException.class)
				.hasMessageContaining("used in 5 ticket(s)");

		verify(ticketTypeRepository).findById(1L);
		verify(ticketRepository).existsByTicketTypeId(1L);
		verify(ticketRepository).countByTicketTypeId(1L);
		verify(ticketTypeRepository, never()).delete(any());
	}

	@Test
	void toggleTicketTypeActiveStatus_ShouldActivateInactiveType() {
		TicketType inactiveType = TicketType.builder().id(1L).code("INACTIVE").displayName("Inactive").active(false)
				.build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(inactiveType));
		when(ticketTypeRepository.save(any(TicketType.class))).thenReturn(inactiveType);

		TicketType result = ticketTypeService.toggleTicketTypeActiveStatus(1L);

		assertThat(result.isActive()).isTrue();
		verify(ticketTypeRepository).findById(1L);
		verify(ticketTypeRepository).save(inactiveType);
	}

	@Test
	void toggleTicketTypeActiveStatus_ShouldDeactivateActiveType_WhenNoActiveTickets() {
		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeIdAndStatusIn(anyLong(), anyList())).thenReturn(false);
		when(ticketTypeRepository.save(any(TicketType.class))).thenReturn(ticketType);

		TicketType result = ticketTypeService.toggleTicketTypeActiveStatus(1L);

		assertThat(result.isActive()).isFalse();
		verify(ticketRepository).existsByTicketTypeIdAndStatusIn(eq(1L), anyList());
		verify(ticketTypeRepository).save(ticketType);
	}

	@Test
	void toggleTicketTypeActiveStatus_ShouldThrowInUseException_WhenDeactivatingWithActiveTickets() {
		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(ticketType));
		when(ticketRepository.existsByTicketTypeIdAndStatusIn(anyLong(), anyList())).thenReturn(true);
		when(ticketRepository.countByTicketTypeIdAndStatusIn(anyLong(), anyList())).thenReturn(3L);

		assertThatThrownBy(() -> ticketTypeService.toggleTicketTypeActiveStatus(1L))
				.isInstanceOf(TicketTypeInUseException.class).hasMessageContaining("used in 3 active ticket(s)");

		verify(ticketRepository).existsByTicketTypeIdAndStatusIn(eq(1L), anyList());
		verify(ticketRepository).countByTicketTypeIdAndStatusIn(eq(1L), anyList());
		verify(ticketTypeRepository, never()).save(any());
	}

	@ParameterizedTest
	@CsvSource({ "0, 12, 5, true", "0, 12, 0, true", "0, 12, 12, true", "18, null, 25, true", "null, 12, 5, true",
			"18, null, 17, false", "null, 12, 15, false", "18, 65, 70, false", "18, 65, 10, false" })
	void validateAgeForTicketType_ShouldReturnCorrectValidation(String minAgeStr, String maxAgeStr, Integer age,
			boolean expected) {
		Integer minAge = "null".equals(minAgeStr) ? null : Integer.parseInt(minAgeStr);
		Integer maxAge = "null".equals(maxAgeStr) ? null : Integer.parseInt(maxAgeStr);

		TicketType testType = TicketType.builder().id(1L).code("TEST").minAge(minAge).maxAge(maxAge).build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(testType));

		boolean result = ticketTypeService.validateAgeForTicketType(1L, age);

		assertThat(result).isEqualTo(expected);
		verify(ticketTypeRepository).findById(1L);
	}

	@Test
	void isAgeValidForTicketType_ShouldHandleNullAge() {
		TicketType noRestrictions = TicketType.builder().minAge(null).maxAge(null).build();

		TicketType withRestrictions = TicketType.builder().minAge(18).maxAge(65).build();

		assertThat(ticketTypeService.isAgeValidForTicketType(noRestrictions, null)).isTrue();
		assertThat(ticketTypeService.isAgeValidForTicketType(withRestrictions, null)).isFalse();
	}

	@Test
	void existsByCode_ShouldReturnTrue_WhenCodeExists() {
		when(ticketTypeRepository.existsByCode("EXISTING")).thenReturn(true);

		boolean result = ticketTypeService.existsByCode("EXISTING");

		assertThat(result).isTrue();
		verify(ticketTypeRepository).existsByCode("EXISTING");
	}

	@Test
	void existsByCode_ShouldReturnFalse_WhenCodeNotExists() {
		when(ticketTypeRepository.existsByCode("NONEXISTENT")).thenReturn(false);

		boolean result = ticketTypeService.existsByCode("NONEXISTENT");

		assertThat(result).isFalse();
		verify(ticketTypeRepository).existsByCode("NONEXISTENT");
	}

	@Test
	void getFormattedAgeRange_ShouldReturnCorrectFormat() {
		TicketType testType = TicketType.builder().id(1L).code("TEST").minAge(0).maxAge(12).build();

		when(ticketTypeRepository.findById(1L)).thenReturn(Optional.of(testType));

		String result = ticketTypeService.getFormattedAgeRange(1L);

		assertThat(result).isEqualTo("0-12 years");
		verify(ticketTypeRepository).findById(1L);
	}

	@Test
	void validateAgeRange_ShouldThrowException_WhenMinGreaterThanMax() {
		assertThatThrownBy(() -> ticketTypeService.createTicketType(
				TicketTypeCreateRequest.builder().code("TEST").displayName("Test").minAge(20).maxAge(10).build()))
				.isInstanceOf(TicketTypeValidationException.class).hasMessageContaining("Invalid age range");
	}

	@Test
	void validateAgeRange_ShouldThrowException_WhenMinAgeOutOfRange() {
		assertThatThrownBy(() -> ticketTypeService.createTicketType(
				TicketTypeCreateRequest.builder().code("TEST").displayName("Test").minAge(-5).build()))
				.isInstanceOf(TicketTypeValidationException.class)
				.hasMessageContaining("Invalid value for field 'minAge'");
	}

	@Test
	void validateAgeRange_ShouldThrowException_WhenMaxAgeOutOfRange() {
		assertThatThrownBy(() -> ticketTypeService.createTicketType(
				TicketTypeCreateRequest.builder().code("TEST").displayName("Test").maxAge(150).build()))
				.isInstanceOf(TicketTypeValidationException.class)
				.hasMessageContaining("Invalid value for field 'maxAge'");
	}
}