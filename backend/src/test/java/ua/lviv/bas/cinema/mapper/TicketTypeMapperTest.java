package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

public class TicketTypeMapperTest {

	private TicketTypeMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(TicketTypeMapper.class);
	}

	@Test
	void toTicketType_ShouldMapAllFieldsFromCreateRequest() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("CHILD").displayName("Child Ticket")
				.priceMultiplier(new BigDecimal("0.70")).minAge(0).maxAge(12).requiresDocument(true)
				.documentType("Birth Certificate").active(true).category(TicketTypeCategory.CHILD).build();

		TicketType entity = mapper.toTicketType(request);

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
	void toTicketType_ShouldSetDefaultValues() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("STANDARD")
				.displayName("Standard Ticket").build();

		TicketType entity = mapper.toTicketType(request);

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
	void toTicketType_ShouldReturnNull_WhenInputIsNull() {
		TicketType entity = mapper.toTicketType(null);

		assertThat(entity).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "\t", "\n" })
	void toTicketType_ShouldHandleBlankCode(String code) {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code(code).displayName("Test Ticket")
				.build();

		TicketType entity = mapper.toTicketType(request);

		assertThat(entity.getCode()).isEqualTo(code);
	}

	@Test
	void toTicketType_ShouldHandleMaxLengthFields() {
		String maxCode = "A".repeat(20);
		String maxDisplayName = "B".repeat(50);
		String maxDocumentType = "C".repeat(100);

		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code(maxCode).displayName(maxDisplayName)
				.documentType(maxDocumentType).build();

		TicketType entity = mapper.toTicketType(request);

		assertThat(entity.getCode()).hasSize(20);
		assertThat(entity.getDisplayName()).hasSize(50);
		assertThat(entity.getDocumentType()).hasSize(100);
	}

	@Test
	void updateTicketTypeFromRequest_ShouldUpdateFieldsFromUpdateRequest() {
		TicketType existing = TicketType.builder().id(1L).code("OLD_CODE").displayName("Old Name")
				.priceMultiplier(new BigDecimal("1.00")).minAge(null).maxAge(null).requiresDocument(false)
				.documentType(null).active(true).category(null).createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now()).build();

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("New Name")
				.priceModifier(new BigDecimal("0.80")).minAge(18).maxAge(65).requiresDocument(true)
				.documentType("ID Card").active(false).category(TicketTypeCategory.SENIOR).build();

		mapper.updateTicketTypeFromRequest(existing, updateRequest);

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
	void updateTicketTypeFromRequest_ShouldIgnoreNullValues() {
		TicketType existing = TicketType.builder().id(1L).code("CODE").displayName("Original Name")
				.priceMultiplier(new BigDecimal("1.00")).minAge(10).maxAge(20).requiresDocument(true)
				.documentType("Original Doc").active(true).category(TicketTypeCategory.STUDENT).build();

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated Name").build();

		mapper.updateTicketTypeFromRequest(existing, updateRequest);

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
	void updateTicketTypeFromRequest_ShouldNotUpdateCode() {
		TicketType existing = TicketType.builder().id(1L).code("ORIGINAL").displayName("Original").build();

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated").build();

		mapper.updateTicketTypeFromRequest(existing, updateRequest);

		assertThat(existing.getCode()).isEqualTo("ORIGINAL");
		assertThat(existing.getDisplayName()).isEqualTo("Updated");
	}

	@Test
	void updateTicketTypeFromRequest_ShouldThrowException_WhenTargetIsNull() {
		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Test").build();

		assertThatThrownBy(() -> mapper.updateTicketTypeFromRequest(null, updateRequest))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void updateTicketTypeFromRequest_ShouldHandleEmptyRequest() {
		TicketType existing = TicketType.builder().id(1L).code("CODE").displayName("Original")
				.priceMultiplier(new BigDecimal("1.00")).active(true).build();

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().build();

		mapper.updateTicketTypeFromRequest(existing, updateRequest);

		assertThat(existing).isNotNull();
		assertThat(existing.getCode()).isEqualTo("CODE");
		assertThat(existing.getDisplayName()).isEqualTo("Original");
		assertThat(existing.getPriceMultiplier()).isEqualTo(new BigDecimal("1.00"));
		assertThat(existing.isActive()).isTrue();
	}

	@Test
	void toTicketTypeResponse_ShouldMapAllFields() {
		LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
		LocalDateTime updatedAt = LocalDateTime.now();

		TicketType entity = TicketType.builder().id(1L).code("STUDENT").displayName("Student Ticket")
				.priceMultiplier(new BigDecimal("0.50")).minAge(18).maxAge(null).requiresDocument(true)
				.documentType("Student ID").active(true).category(TicketTypeCategory.STUDENT).createdAt(createdAt)
				.updatedAt(updatedAt).build();

		TicketTypeResponse response = mapper.toTicketTypeResponse(entity);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getCode()).isEqualTo("STUDENT");
		assertThat(response.getDisplayName()).isEqualTo("Student Ticket");
		assertThat(response.getPriceMultiplier().toString()).isEqualTo("0.50");
		assertThat(response.getMinAge()).isEqualTo(18);
		assertThat(response.getMaxAge()).isNull();
		assertThat(response.isRequiresDocument()).isTrue();
		assertThat(response.getDocumentType()).isEqualTo("Student ID");
		assertThat(response.isActive()).isTrue();
		assertThat(response.getCategory()).isEqualTo(TicketTypeCategory.STUDENT);
		assertThat(response.getCreatedAt()).isEqualTo(createdAt);
		assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
	}

	@Test
	void toTicketTypeResponse_ShouldReturnNull_WhenInputIsNull() {
		TicketTypeResponse response = mapper.toTicketTypeResponse(null);

		assertThat(response).isNull();
	}

	@Test
	void toTicketTypeResponse_ShouldHandleEntityWithoutDates() {
		TicketType entity = TicketType.builder().id(1L).code("TEST").displayName("Test").build();

		TicketTypeResponse response = mapper.toTicketTypeResponse(entity);

		assertThat(response).isNotNull();
		assertThat(response.getCreatedAt()).isNull();
		assertThat(response.getUpdatedAt()).isNull();
	}

	@ParameterizedTest
	@CsvSource({ "0.50, STUDENT", "1.00, STANDARD", "0.70, CHILD", "0.80, SENIOR" })
	void toTicketTypeResponse_ShouldMapPriceMultiplierAndCategory(String multiplier, TicketTypeCategory category) {
		TicketType entity = TicketType.builder().id(1L).code("TEST_" + category).displayName("Test " + category)
				.priceMultiplier(new BigDecimal(multiplier)).category(category).build();

		TicketTypeResponse response = mapper.toTicketTypeResponse(entity);

		assertThat(response.getPriceMultiplier().toString()).isEqualTo(multiplier);
		assertThat(response.getCategory()).isEqualTo(category);
	}

	@Test
	void toTicketTypeSimpleResponse_ShouldMapOnlyBasicFields() {
		TicketType entity = TicketType.builder().id(1L).code("SIMPLE").displayName("Simple Ticket")
				.priceMultiplier(new BigDecimal("0.90")).active(true).build();

		TicketTypeSimpleResponse response = mapper.toTicketTypeSimpleResponse(entity);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getCode()).isEqualTo("SIMPLE");
		assertThat(response.getDisplayName()).isEqualTo("Simple Ticket");
		assertThat(response.getPriceMultiplier().toString()).isEqualTo("0.90");
		assertThat(response.isActive()).isTrue();
	}

	@Test
	void toTicketTypeSimpleResponse_ShouldReturnNull_WhenInputIsNull() {
		TicketTypeSimpleResponse response = mapper.toTicketTypeSimpleResponse(null);

		assertThat(response).isNull();
	}

	@Test
	void toTicketTypeSimpleResponse_ShouldHandleInactiveTicketType() {
		TicketType entity = TicketType.builder().id(1L).code("INACTIVE").displayName("Inactive Ticket")
				.priceMultiplier(new BigDecimal("1.00")).active(false).build();

		TicketTypeSimpleResponse response = mapper.toTicketTypeSimpleResponse(entity);

		assertThat(response.isActive()).isFalse();
	}

	@Test
	void consistencyCheck_CreateThenToTicketTypeResponse_ShouldReturnSameValues() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().code("CONSISTENCY")
				.displayName("Consistency Test").priceMultiplier(new BigDecimal("0.75")).minAge(10).maxAge(20)
				.requiresDocument(true).documentType("Test Doc").active(false).category(TicketTypeCategory.STUDENT)
				.build();

		TicketType entity = mapper.toTicketType(request);
		TicketTypeResponse response = mapper.toTicketTypeResponse(entity);

		assertThat(response.getCode()).isEqualTo("CONSISTENCY");
		assertThat(response.getDisplayName()).isEqualTo("Consistency Test");
		assertThat(response.getPriceMultiplier().toString()).isEqualTo("0.75");
		assertThat(response.getMinAge()).isEqualTo(10);
		assertThat(response.getMaxAge()).isEqualTo(20);
		assertThat(response.isRequiresDocument()).isTrue();
		assertThat(response.getDocumentType()).isEqualTo("Test Doc");
		assertThat(response.isActive()).isFalse();
		assertThat(response.getCategory()).isEqualTo(TicketTypeCategory.STUDENT);
	}

	@Test
	void updateTicketTypeFromRequestThenToTicketTypeResponse_ShouldReflectChanges() {
		TicketType entity = TicketType.builder().id(1L).code("ORIGINAL").displayName("Original")
				.priceMultiplier(new BigDecimal("1.00")).active(true).build();

		TicketTypeUpdateRequest update = TicketTypeUpdateRequest.builder().displayName("Updated").active(false).build();

		mapper.updateTicketTypeFromRequest(entity, update);
		TicketTypeResponse response = mapper.toTicketTypeResponse(entity);

		assertThat(response.getDisplayName()).isEqualTo("Updated");
		assertThat(response.isActive()).isFalse();
	}

	@Test
	void toTicketTypeResponse_ShouldHandleAllTicketTypeCategories() {
		for (TicketTypeCategory category : TicketTypeCategory.values()) {
			TicketType entity = TicketType.builder().id(1L).code("TEST_" + category).displayName("Test " + category)
					.category(category).build();

			TicketTypeResponse response = mapper.toTicketTypeResponse(entity);

			assertThat(response.getCategory()).isEqualTo(category);
		}
	}

	@Test
	void toTicketTypeResponse_ShouldHandleExtremePriceMultipliers() {
		TicketType entityLow = TicketType.builder().id(1L).code("LOW").displayName("Low Multiplier")
				.priceMultiplier(new BigDecimal("0.01")).build();

		TicketType entityHigh = TicketType.builder().id(2L).code("HIGH").displayName("High Multiplier")
				.priceMultiplier(new BigDecimal("9.99")).build();

		TicketTypeResponse responseLow = mapper.toTicketTypeResponse(entityLow);
		TicketTypeResponse responseHigh = mapper.toTicketTypeResponse(entityHigh);

		assertThat(responseLow.getPriceMultiplier().toString()).isEqualTo("0.01");
		assertThat(responseHigh.getPriceMultiplier().toString()).isEqualTo("9.99");
	}

	@Test
	void toTicketTypeResponse_ShouldMapEntityWithAgeRangeAndDocumentRequirement() {
		TicketType entity = TicketType.builder().id(1L).code("AGE_DOC").displayName("Age with Document").minAge(18)
				.maxAge(65).requiresDocument(true).documentType("Passport or ID").build();

		TicketTypeResponse response = mapper.toTicketTypeResponse(entity);

		assertThat(response.getMinAge()).isEqualTo(18);
		assertThat(response.getMaxAge()).isEqualTo(65);
		assertThat(response.isRequiresDocument()).isTrue();
		assertThat(response.getDocumentType()).isEqualTo("Passport or ID");
	}

	@Test
	void toTicketTypeResponse_ShouldHandleEntityWithoutAgeRestrictions() {
		TicketType entity = TicketType.builder().id(1L).code("NO_AGE").displayName("No Age Restrictions").minAge(null)
				.maxAge(null).build();

		TicketTypeResponse response = mapper.toTicketTypeResponse(entity);

		assertThat(response.getMinAge()).isNull();
		assertThat(response.getMaxAge()).isNull();
	}
}