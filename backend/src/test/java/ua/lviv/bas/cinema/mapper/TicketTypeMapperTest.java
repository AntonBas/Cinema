package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.domain.projection.TicketTypeAdminProjection;
import ua.lviv.bas.cinema.domain.projection.TicketTypeUserProjection;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeUserResponse;

public class TicketTypeMapperTest {

	private TicketTypeMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new TicketTypeMapperImpl();
	}

	@Test
	void toTicketType_ShouldMapAllFieldsFromCreateRequest() {
		TicketTypeCreateRequest request = TicketTypeCreateRequest.builder().displayName("Child Ticket")
				.priceMultiplier(new BigDecimal("0.70")).minAge(0).maxAge(12).requiresDocument(true)
				.documentType("Birth Certificate").active(true).category(TicketTypeCategory.CHILD).build();

		TicketType entity = mapper.toTicketType(request);

		assertThat(entity.getId()).isNull();
		assertThat(entity.getDisplayName()).isEqualTo("Child Ticket");
		assertThat(entity.getPriceMultiplier()).isEqualByComparingTo("0.70");
		assertThat(entity.getMinAge()).isZero();
		assertThat(entity.getMaxAge()).isEqualTo(12);
		assertThat(entity.isRequiresDocument()).isTrue();
		assertThat(entity.getDocumentType()).isEqualTo("Birth Certificate");
		assertThat(entity.isActive()).isTrue();
		assertThat(entity.getCategory()).isEqualTo(TicketTypeCategory.CHILD);
	}

	@Test
	void toTicketType_ShouldReturnNull_WhenInputIsNull() {
		assertThat(mapper.toTicketType(null)).isNull();
	}

	@Test
	void updateTicketTypeFromRequest_ShouldUpdateAllFields() {
		TicketType existing = TicketType.builder().id(1L).displayName("Old Name")
				.priceMultiplier(new BigDecimal("1.00")).build();

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("New Name")
				.priceMultiplier(new BigDecimal("0.80")).minAge(18).maxAge(65).requiresDocument(true)
				.documentType("ID Card").active(false).category(TicketTypeCategory.SENIOR).build();

		mapper.updateTicketTypeFromRequest(existing, updateRequest);

		assertThat(existing.getId()).isEqualTo(1L);
		assertThat(existing.getDisplayName()).isEqualTo("New Name");
		assertThat(existing.getPriceMultiplier()).isEqualByComparingTo("0.80");
		assertThat(existing.getMinAge()).isEqualTo(18);
		assertThat(existing.getMaxAge()).isEqualTo(65);
		assertThat(existing.isRequiresDocument()).isTrue();
		assertThat(existing.getDocumentType()).isEqualTo("ID Card");
		assertThat(existing.isActive()).isFalse();
		assertThat(existing.getCategory()).isEqualTo(TicketTypeCategory.SENIOR);
	}

	@Test
	void updateTicketTypeFromRequest_ShouldIgnoreNullValues() {
		TicketType existing = TicketType.builder().id(1L).displayName("Original Name")
				.priceMultiplier(new BigDecimal("1.00")).minAge(10).active(true).build();

		TicketTypeUpdateRequest updateRequest = TicketTypeUpdateRequest.builder().displayName("Updated Name")
				.priceMultiplier(null).build();

		mapper.updateTicketTypeFromRequest(existing, updateRequest);

		assertThat(existing.getDisplayName()).isEqualTo("Updated Name");
		assertThat(existing.getPriceMultiplier()).isEqualByComparingTo("1.00");
		assertThat(existing.getMinAge()).isEqualTo(10);
		assertThat(existing.isActive()).isTrue();
	}

	@Test
	void toTicketTypeResponse_ShouldMapAllFieldsFromEntity() {
		TicketType entity = TicketType.builder().id(1L).displayName("Student Ticket")
				.priceMultiplier(new BigDecimal("0.50")).minAge(18).requiresDocument(true).documentType("Student ID")
				.active(true).category(TicketTypeCategory.STUDENT).build();

		TicketTypeResponse response = mapper.toTicketTypeResponse(entity);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getDisplayName()).isEqualTo("Student Ticket");
		assertThat(response.getPriceMultiplier()).isEqualByComparingTo("0.50");
		assertThat(response.getMinAge()).isEqualTo(18);
		assertThat(response.getMaxAge()).isNull();
		assertThat(response.isRequiresDocument()).isTrue();
		assertThat(response.getDocumentType()).isEqualTo("Student ID");
		assertThat(response.isActive()).isTrue();
		assertThat(response.getCategory()).isEqualTo(TicketTypeCategory.STUDENT);
	}

	@Test
	void toTicketTypeResponse_ShouldMapFromAdminProjection() {
		TicketTypeAdminProjection projection = mock(TicketTypeAdminProjection.class);

		when(projection.getId()).thenReturn(1L);
		when(projection.getDisplayName()).thenReturn("Admin Projection");
		when(projection.getPriceMultiplier()).thenReturn(new BigDecimal("0.75"));
		when(projection.getMinAge()).thenReturn(16);
		when(projection.getMaxAge()).thenReturn(60);
		when(projection.isRequiresDocument()).thenReturn(true);
		when(projection.getDocumentType()).thenReturn("Passport");
		when(projection.isActive()).thenReturn(true);
		when(projection.getCategory()).thenReturn(TicketTypeCategory.STANDARD);

		TicketTypeResponse response = mapper.toTicketTypeResponse(projection);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getDisplayName()).isEqualTo("Admin Projection");
		assertThat(response.getPriceMultiplier()).isEqualByComparingTo("0.75");
		assertThat(response.getMinAge()).isEqualTo(16);
		assertThat(response.getMaxAge()).isEqualTo(60);
		assertThat(response.isRequiresDocument()).isTrue();
		assertThat(response.getDocumentType()).isEqualTo("Passport");
		assertThat(response.isActive()).isTrue();
		assertThat(response.getCategory()).isEqualTo(TicketTypeCategory.STANDARD);
	}

	@Test
	void toTicketTypeUserResponse_ShouldMapFromEntity() {
		TicketType entity = TicketType.builder().id(1L).displayName("User Ticket")
				.priceMultiplier(new BigDecimal("0.60")).requiresDocument(true).documentType("Student Card").build();

		TicketTypeUserResponse response = mapper.toTicketTypeUserResponse(entity);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getDisplayName()).isEqualTo("User Ticket");
		assertThat(response.getPriceMultiplier()).isEqualByComparingTo("0.60");
		assertThat(response.isRequiresDocument()).isTrue();
		assertThat(response.getDocumentType()).isEqualTo("Student Card");
	}

	@Test
	void toTicketTypeUserResponse_ShouldMapFromUserProjection() {
		TicketTypeUserProjection projection = mock(TicketTypeUserProjection.class);

		when(projection.getId()).thenReturn(1L);
		when(projection.getDisplayName()).thenReturn("User Projection");
		when(projection.getPriceMultiplier()).thenReturn(new BigDecimal("0.85"));
		when(projection.isRequiresDocument()).thenReturn(true);
		when(projection.getDocumentType()).thenReturn("ID Card");

		TicketTypeUserResponse response = mapper.toTicketTypeUserResponse(projection);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getDisplayName()).isEqualTo("User Projection");
		assertThat(response.getPriceMultiplier()).isEqualByComparingTo("0.85");
		assertThat(response.isRequiresDocument()).isTrue();
		assertThat(response.getDocumentType()).isEqualTo("ID Card");
	}

	@Test
	void toTicketTypeResponse_ShouldReturnNull_WhenInputIsNull() {
		assertThat(mapper.toTicketTypeResponse((TicketType) null)).isNull();
		assertThat(mapper.toTicketTypeResponse((TicketTypeAdminProjection) null)).isNull();
	}

	@Test
	void toTicketTypeUserResponse_ShouldReturnNull_WhenInputIsNull() {
		assertThat(mapper.toTicketTypeUserResponse((TicketType) null)).isNull();
		assertThat(mapper.toTicketTypeUserResponse((TicketTypeUserProjection) null)).isNull();
	}
}