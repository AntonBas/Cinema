package ua.lviv.bas.cinema.mapper.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.ticket.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.ticketType.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticketType.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticketType.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticketType.response.TicketTypeUserResponse;
import ua.lviv.bas.cinema.repository.ticket.projection.TicketTypeAdminProjection;
import ua.lviv.bas.cinema.repository.ticket.projection.TicketTypeUserProjection;

public class TicketTypeMapperTest {

	private TicketTypeMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new TicketTypeMapperImpl();
	}

	@Test
	void toTicketType_ShouldMapAllFieldsFromCreateRequest() {
		TicketTypeCreateRequest request = new TicketTypeCreateRequest("Child Ticket", new BigDecimal("0.70"), 0, 12,
				true, "Birth Certificate", true, TicketTypeCategory.CHILD);

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

		TicketTypeUpdateRequest updateRequest = new TicketTypeUpdateRequest("New Name", new BigDecimal("0.80"), 18, 65,
				true, "ID Card", false, TicketTypeCategory.SENIOR);

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

		TicketTypeUpdateRequest updateRequest = new TicketTypeUpdateRequest("Updated Name", null, null, null, null,
				null, null, null);

		mapper.updateTicketTypeFromRequest(existing, updateRequest);

		assertThat(existing.getDisplayName()).isEqualTo("Updated Name");
		assertThat(existing.getPriceMultiplier()).isEqualByComparingTo("1.00");
		assertThat(existing.getMinAge()).isEqualTo(10);
		assertThat(existing.isActive()).isTrue();
	}

	@Test
	void toTicketTypeResponse_ShouldMapAllFieldsFromEntity() {
		TicketType entity = TicketType.builder().id(1L).displayName("Student Ticket")
				.priceMultiplier(new BigDecimal("0.50")).minAge(18).maxAge(25).requiresDocument(true)
				.documentType("Student ID").active(true).category(TicketTypeCategory.STUDENT).build();

		TicketTypeResponse response = mapper.toTicketTypeResponse(entity);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.displayName()).isEqualTo("Student Ticket");
		assertThat(response.priceMultiplier()).isEqualByComparingTo("0.50");
		assertThat(response.minAge()).isEqualTo(18);
		assertThat(response.maxAge()).isEqualTo(25);
		assertThat(response.requiresDocument()).isTrue();
		assertThat(response.documentType()).isEqualTo("Student ID");
		assertThat(response.active()).isTrue();
		assertThat(response.category()).isEqualTo(TicketTypeCategory.STUDENT);
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

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.displayName()).isEqualTo("Admin Projection");
		assertThat(response.priceMultiplier()).isEqualByComparingTo("0.75");
		assertThat(response.minAge()).isEqualTo(16);
		assertThat(response.maxAge()).isEqualTo(60);
		assertThat(response.requiresDocument()).isTrue();
		assertThat(response.documentType()).isEqualTo("Passport");
		assertThat(response.active()).isTrue();
		assertThat(response.category()).isEqualTo(TicketTypeCategory.STANDARD);
	}

	@Test
	void toTicketTypeUserResponse_ShouldMapFromUserProjection() {
		TicketTypeUserProjection projection = mock(TicketTypeUserProjection.class);

		when(projection.getId()).thenReturn(1L);
		when(projection.getDisplayName()).thenReturn("User Projection");
		when(projection.getPriceMultiplier()).thenReturn(new BigDecimal("0.85"));
		when(projection.isRequiresDocument()).thenReturn(true);
		when(projection.getDocumentType()).thenReturn("ID Card");
		when(projection.getCategory()).thenReturn(TicketTypeCategory.STANDARD);

		TicketTypeUserResponse response = mapper.toTicketTypeUserResponse(projection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.displayName()).isEqualTo("User Projection");
		assertThat(response.priceMultiplier()).isEqualByComparingTo("0.85");
		assertThat(response.requiresDocument()).isTrue();
		assertThat(response.documentType()).isEqualTo("ID Card");
	}

	@Test
	void toTicketTypeResponse_ShouldReturnNull_WhenEntityIsNull() {
		assertThat(mapper.toTicketTypeResponse((TicketType) null)).isNull();
	}

	@Test
	void toTicketTypeResponse_ShouldReturnNull_WhenProjectionIsNull() {
		assertThat(mapper.toTicketTypeResponse((TicketTypeAdminProjection) null)).isNull();
	}

	@Test
	void toTicketTypeUserResponse_ShouldReturnNull_WhenProjectionIsNull() {
		assertThat(mapper.toTicketTypeUserResponse((TicketTypeUserProjection) null)).isNull();
	}

	@Test
	void updateTicketTypeFromRequest_WithNullRequest_ShouldNotChange() {
		TicketType existing = TicketType.builder().id(1L).displayName("Original Name")
				.priceMultiplier(new BigDecimal("1.00")).build();

		mapper.updateTicketTypeFromRequest(existing, null);

		assertThat(existing.getDisplayName()).isEqualTo("Original Name");
		assertThat(existing.getPriceMultiplier()).isEqualByComparingTo("1.00");
	}
}