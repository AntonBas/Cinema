package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;

class TicketTypeMapperTest {

	private TicketTypeMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(TicketTypeMapper.class);
	}

	@Test
	void toEntity_ShouldMapAllFieldsFromCreateRequest() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("CHILD").displayName("Child Ticket")
				.priceMultiplier(new BigDecimal("0.70")).minAge(0).maxAge(12).requiresDocument(true)
				.documentType("Birth Certificate").active(true).category(TicketTypeCategory.CHILD).build();

		TicketType entity = mapper.toEntity(request);

		assertThat(entity).isNotNull();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getCode()).isEqualTo("CHILD");
		assertThat(entity.getDisplayName()).isEqualTo("Child Ticket");
		assertThat(entity.getPriceMultiplier()).isEqualTo(new BigDecimal("0.70"));
		assertThat(entity.getMinAge()).isEqualTo(0);
		assertThat(entity.getMaxAge()).isEqualTo(12);
		assertThat(entity.isRequiresDocument()).isTrue();
		assertThat(entity.getDocumentType()).isEqualTo("Birth Certificate");
		assertThat(entity.isActive()).isTrue();
		assertThat(entity.getCategory()).isEqualTo(TicketTypeCategory.CHILD);
		assertThat(entity.getCreatedAt()).isNull();
		assertThat(entity.getUpdatedAt()).isNull();
	}

	@Test
	void toEntity_ShouldSetDefaultValues() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("STANDARD")
				.displayName("Standard Ticket").build();

		TicketType entity = mapper.toEntity(request);

		assertThat(entity).isNotNull();
		assertThat(entity.getCode()).isEqualTo("STANDARD");
		assertThat(entity.getDisplayName()).isEqualTo("Standard Ticket");
		assertThat(entity.getPriceMultiplier()).isNull();
		assertThat(entity.getMinAge()).isNull();
		assertThat(entity.getMaxAge()).isNull();
		assertThat(entity.isRequiresDocument()).isFalse();
		assertThat(entity.getDocumentType()).isNull();
		assertThat(entity.isActive()).isTrue();
		assertThat(entity.getCategory()).isNull();
	}

	@Test
	void toEntity_ShouldReturnNull_WhenInputIsNull() {
		TicketType entity = mapper.toEntity(null);

		assertThat(entity).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "\t", "\n" })
	void toEntity_ShouldHandleBlankCode(String code) {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code(code).displayName("Test Ticket")
				.build();

		TicketType entity = mapper.toEntity(request);

		assertThat(entity.getCode()).isEqualTo(code);
	}

	@Test
	void toEntity_ShouldHandleMaxLengthFields() {
		String maxCode = "A".repeat(20);
		String maxDisplayName = "B".repeat(50);
		String maxDocumentType = "C".repeat(100);

		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code(maxCode).displayName(maxDisplayName)
				.documentType(maxDocumentType).build();

		TicketType entity = mapper.toEntity(request);

		assertThat(entity.getCode()).hasSize(20);
		assertThat(entity.getDisplayName()).hasSize(50);
		assertThat(entity.getDocumentType()).hasSize(100);
	}

	@Test
	void updateEntity_ShouldUpdateFieldsFromUpdateRequest() {
		TicketType existing = TicketType.builder().id(1L).code("OLD_CODE").displayName("Old Name")
				.priceMultiplier(new BigDecimal("1.00")).minAge(null).maxAge(null).requiresDocument(false)
				.documentType(null).active(true).category(null).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("New Name")
				.priceModifier(new BigDecimal("0.80")) // Змінено на priceModifier
				.minAge(18).maxAge(65).requiresDocument(true).documentType("ID Card").active(false)
				.category(TicketTypeCategory.SENIOR).build();

		mapper.updateEntity(existing, updateRequest);

		assertThat(existing.getId()).isEqualTo(1L);
		assertThat(existing.getCode()).isEqualTo("OLD_CODE");
		assertThat(existing.getDisplayName()).isEqualTo("New Name");
		assertThat(existing.getPriceMultiplier()).isEqualTo(new BigDecimal("0.80"));
		assertThat(existing.getMinAge()).isEqualTo(18);
		assertThat(existing.getMaxAge()).isEqualTo(65);
		assertThat(existing.isRequiresDocument()).isTrue();
		assertThat(existing.getDocumentType()).isEqualTo("ID Card");
		assertThat(existing.isActive()).isFalse();
		assertThat(existing.getCategory()).isEqualTo(TicketTypeCategory.SENIOR);
		assertThat(existing.getCreatedAt()).isNotNull();
		assertThat(existing.getUpdatedAt()).isNotNull();
	}

	@Test
	void updateEntity_ShouldIgnoreNullValues() {
		TicketType existing = TicketType.builder().id(1L).code("CODE").displayName("Original Name")
				.priceMultiplier(new BigDecimal("1.00")).minAge(10).maxAge(20).requiresDocument(true)
				.documentType("Original Doc").active(true).category(TicketTypeCategory.STUDENT).build();

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated Name").build();

		mapper.updateEntity(existing, updateRequest);

		assertThat(existing).isNotNull();
		assertThat(existing.getDisplayName()).isEqualTo("Updated Name");
		assertThat(existing.getPriceMultiplier()).isEqualTo(new BigDecimal("1.00"));
		assertThat(existing.getMinAge()).isEqualTo(10);
		assertThat(existing.getMaxAge()).isEqualTo(20);
		assertThat(existing.isRequiresDocument()).isTrue();
		assertThat(existing.getDocumentType()).isEqualTo("Original Doc");
		assertThat(existing.isActive()).isTrue();
		assertThat(existing.getCategory()).isEqualTo(TicketTypeCategory.STUDENT);
	}

	@Test
	void updateEntity_ShouldNotUpdateCode() {
		TicketType existing = TicketType.builder().id(1L).code("ORIGINAL").displayName("Original").build();

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated").build();

		mapper.updateEntity(existing, updateRequest);

		assertThat(existing.getCode()).isEqualTo("ORIGINAL");
		assertThat(existing.getDisplayName()).isEqualTo("Updated");
	}

	@Test
	void updateEntity_ShouldThrowException_WhenTargetIsNull() {
		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Test").build();

		assertThatThrownBy(() -> mapper.updateEntity(null, updateRequest)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void updateEntity_ShouldHandleEmptyRequest() {
		TicketType existing = TicketType.builder().id(1L).code("CODE").displayName("Original")
				.priceMultiplier(new BigDecimal("1.00")).active(true).build();

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().build();

		mapper.updateEntity(existing, updateRequest);

		assertThat(existing).isNotNull();
		assertThat(existing.getCode()).isEqualTo("CODE");
		assertThat(existing.getDisplayName()).isEqualTo("Original");
		assertThat(existing.getPriceMultiplier()).isEqualTo(new BigDecimal("1.00"));
		assertThat(existing.isActive()).isTrue();
	}

	@Test
	void toResponseDto_ShouldMapAllFields() {
		LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
		LocalDateTime updatedAt = LocalDateTime.now();

		TicketType entity = TicketType.builder().id(1L).code("STUDENT").displayName("Student Ticket")
				.priceMultiplier(new BigDecimal("0.50")).minAge(18).maxAge(null).requiresDocument(true)
				.documentType("Student ID").active(true).category(TicketTypeCategory.STUDENT).createdAt(createdAt)
				.updatedAt(updatedAt).build();

		TicketTypeResponse dto = mapper.toResponseDto(entity);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getCode()).isEqualTo("STUDENT");
		assertThat(dto.getDisplayName()).isEqualTo("Student Ticket");
		assertThat(dto.getPriceMultiplier().toString()).isEqualTo("0.50");
		assertThat(dto.getMinAge()).isEqualTo(18);
		assertThat(dto.getMaxAge()).isNull();
		assertThat(dto.isRequiresDocument()).isTrue();
		assertThat(dto.getDocumentType()).isEqualTo("Student ID");
		assertThat(dto.isActive()).isTrue();
		assertThat(dto.getCategory()).isEqualTo(TicketTypeCategory.STUDENT);
		assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
		assertThat(dto.getUpdatedAt()).isEqualTo(updatedAt);
	}

	@Test
	void toResponseDto_ShouldReturnNull_WhenInputIsNull() {
		TicketTypeResponse dto = mapper.toResponseDto(null);

		assertThat(dto).isNull();
	}

	@Test
	void toResponseDto_ShouldHandleEntityWithoutDates() {
		TicketType entity = TicketType.builder().id(1L).code("TEST").displayName("Test").build();

		TicketTypeResponse dto = mapper.toResponseDto(entity);

		assertThat(dto).isNotNull();
		assertThat(dto.getCreatedAt()).isNull();
		assertThat(dto.getUpdatedAt()).isNull();
	}

	@ParameterizedTest
	@CsvSource({ "0.50, STUDENT", "1.00, STANDARD", "0.70, CHILD", "0.80, SENIOR" })
	void toResponseDto_ShouldMapPriceMultiplierAndCategory(String multiplier, TicketTypeCategory category) {
		TicketType entity = TicketType.builder().id(1L).code("TEST").displayName("Test")
				.priceMultiplier(new BigDecimal(multiplier)).category(category).build();

		TicketTypeResponse dto = mapper.toResponseDto(entity);

		assertThat(dto.getPriceMultiplier().toString()).isEqualTo(multiplier);
		assertThat(dto.getCategory()).isEqualTo(category);
	}

	@Test
	void toSimpleDto_ShouldMapOnlyBasicFields() {
		TicketType entity = TicketType.builder().id(1L).code("SIMPLE").displayName("Simple Ticket")
				.priceMultiplier(new BigDecimal("0.90")).active(true).build();

		TicketTypeSimpleResponse dto = mapper.toSimpleDto(entity);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getCode()).isEqualTo("SIMPLE");
		assertThat(dto.getDisplayName()).isEqualTo("Simple Ticket");
		assertThat(dto.getPriceMultiplier().toString()).isEqualTo("0.90");
		assertThat(dto.isActive()).isTrue();
	}

	@Test
	void toSimpleDto_ShouldReturnNull_WhenInputIsNull() {
		TicketTypeSimpleResponse dto = mapper.toSimpleDto(null);

		assertThat(dto).isNull();
	}

	@Test
	void toSimpleDto_ShouldHandleInactiveTicketType() {
		TicketType entity = TicketType.builder().id(1L).code("INACTIVE").displayName("Inactive Ticket")
				.priceMultiplier(new BigDecimal("1.00")).active(false).build();

		TicketTypeSimpleResponse dto = mapper.toSimpleDto(entity);

		assertThat(dto.isActive()).isFalse();
	}

	@Test
	void toResponseDtoList_ShouldMapListOfEntities() {
		List<TicketType> entities = Arrays.asList(
				TicketType.builder().id(1L).code("TYPE1").displayName("Type 1").build(),
				TicketType.builder().id(2L).code("TYPE2").displayName("Type 2").build(),
				TicketType.builder().id(3L).code("TYPE3").displayName("Type 3").build());

		List<TicketTypeResponse> dtos = mapper.toResponseDtoList(entities);

		assertThat(dtos).hasSize(3);
		assertThat(dtos.get(0).getCode()).isEqualTo("TYPE1");
		assertThat(dtos.get(1).getCode()).isEqualTo("TYPE2");
		assertThat(dtos.get(2).getCode()).isEqualTo("TYPE3");
	}

	@Test
	void toResponseDtoList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<TicketTypeResponse> dtos = mapper.toResponseDtoList(Collections.emptyList());

		assertThat(dtos).isNotNull().isEmpty();
	}

	@Test
	void toResponseDtoList_ShouldReturnNull_WhenInputIsNull() {
		List<TicketTypeResponse> dtos = mapper.toResponseDtoList(null);

		assertThat(dtos).isNull();
	}

	@Test
	void toResponseDtoList_ShouldHandleListWithNullElements() {
		List<TicketType> entities = Arrays.asList(TicketType.builder().id(1L).code("TYPE1").build(), null,
				TicketType.builder().id(2L).code("TYPE2").build());

		List<TicketTypeResponse> dtos = mapper.toResponseDtoList(entities);

		assertThat(dtos).hasSize(3);
		assertThat(dtos.get(0)).isNotNull();
		assertThat(dtos.get(1)).isNull();
		assertThat(dtos.get(2)).isNotNull();
	}

	@Test
	void toSimpleDtoList_ShouldMapListOfEntities() {
		List<TicketType> entities = Arrays.asList(
				TicketType.builder().id(1L).code("SIMPLE1").displayName("Simple 1").build(),
				TicketType.builder().id(2L).code("SIMPLE2").displayName("Simple 2").build());

		List<TicketTypeSimpleResponse> dtos = mapper.toSimpleDtoList(entities);

		assertThat(dtos).hasSize(2);
		assertThat(dtos.get(0).getCode()).isEqualTo("SIMPLE1");
		assertThat(dtos.get(1).getCode()).isEqualTo("SIMPLE2");
	}

	@Test
	void toSimpleDtoList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<TicketTypeSimpleResponse> dtos = mapper.toSimpleDtoList(Collections.emptyList());

		assertThat(dtos).isNotNull().isEmpty();
	}

	@Test
	void toSimpleDtoList_ShouldReturnNull_WhenInputIsNull() {
		List<TicketTypeSimpleResponse> dtos = mapper.toSimpleDtoList(null);

		assertThat(dtos).isNull();
	}

	@Test
	void consistencyCheck_CreateThenToResponse_ShouldReturnSameValues() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("CONSISTENCY")
				.displayName("Consistency Test").priceMultiplier(new BigDecimal("0.75")).minAge(10).maxAge(20)
				.requiresDocument(true).documentType("Test Doc").active(false).category(TicketTypeCategory.STUDENT)
				.build();

		TicketType entity = mapper.toEntity(request);
		TicketTypeResponse dto = mapper.toResponseDto(entity);

		assertThat(dto.getCode()).isEqualTo("CONSISTENCY");
		assertThat(dto.getDisplayName()).isEqualTo("Consistency Test");
		assertThat(dto.getPriceMultiplier().toString()).isEqualTo("0.75");
		assertThat(dto.getMinAge()).isEqualTo(10);
		assertThat(dto.getMaxAge()).isEqualTo(20);
		assertThat(dto.isRequiresDocument()).isTrue();
		assertThat(dto.getDocumentType()).isEqualTo("Test Doc");
		assertThat(dto.isActive()).isFalse();
		assertThat(dto.getCategory()).isEqualTo(TicketTypeCategory.STUDENT);
	}

	@Test
	void updateThenToResponse_ShouldReflectChanges() {
		TicketType entity = TicketType.builder().id(1L).code("ORIGINAL").displayName("Original")
				.priceMultiplier(new BigDecimal("1.00")).active(true).build();

		TicketTypeUpdateRequest update = TicketTypeUpdateRequest.builder().displayName("Updated").active(false).build();

		mapper.updateEntity(entity, update);
		TicketTypeResponse dto = mapper.toResponseDto(entity);

		assertThat(dto.getDisplayName()).isEqualTo("Updated");
		assertThat(dto.isActive()).isFalse();
	}

	@Test
	void shouldHandleAllTicketTypeCategories() {
		for (TicketTypeCategory category : TicketTypeCategory.values()) {
			TicketType entity = TicketType.builder().id(1L).code("TEST_" + category).displayName("Test " + category)
					.category(category).build();

			TicketTypeResponse dto = mapper.toResponseDto(entity);

			assertThat(dto.getCategory()).isEqualTo(category);
		}
	}

	@Test
	void shouldHandleExtremePriceMultipliers() {
		TicketType entityLow = TicketType.builder().id(1L).code("LOW").displayName("Low Multiplier")
				.priceMultiplier(new BigDecimal("0.01")).build();

		TicketType entityHigh = TicketType.builder().id(2L).code("HIGH").displayName("High Multiplier")
				.priceMultiplier(new BigDecimal("9.99")).build();

		TicketTypeResponse dtoLow = mapper.toResponseDto(entityLow);
		TicketTypeResponse dtoHigh = mapper.toResponseDto(entityHigh);

		assertThat(dtoLow.getPriceMultiplier().toString()).isEqualTo("0.01");
		assertThat(dtoHigh.getPriceMultiplier().toString()).isEqualTo("9.99");
	}

	@Test
	void shouldMapEntityWithAgeRangeAndDocumentRequirement() {
		TicketType entity = TicketType.builder().id(1L).code("AGE_DOC").displayName("Age with Document").minAge(18)
				.maxAge(65).requiresDocument(true).documentType("Passport or ID").build();

		TicketTypeResponse dto = mapper.toResponseDto(entity);

		assertThat(dto.getMinAge()).isEqualTo(18);
		assertThat(dto.getMaxAge()).isEqualTo(65);
		assertThat(dto.isRequiresDocument()).isTrue();
		assertThat(dto.getDocumentType()).isEqualTo("Passport or ID");
	}

	@Test
	void shouldHandleEntityWithoutAgeRestrictions() {
		TicketType entity = TicketType.builder().id(1L).code("NO_AGE").displayName("No Age Restrictions").minAge(null)
				.maxAge(null).build();

		TicketTypeResponse dto = mapper.toResponseDto(entity);

		assertThat(dto.getMinAge()).isNull();
		assertThat(dto.getMaxAge()).isNull();
	}
}