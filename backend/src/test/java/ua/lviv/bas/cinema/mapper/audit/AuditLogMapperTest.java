package ua.lviv.bas.cinema.mapper.audit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.audit.AuditLog;
import ua.lviv.bas.cinema.domain.audit.AuditLogDetail;
import ua.lviv.bas.cinema.dto.audit.AuditLogResponse;

public class AuditLogMapperTest {

	private final AuditLogMapper mapper = Mappers.getMapper(AuditLogMapper.class);

	@Test
	void toResponse_ShouldMapAuditLog() {
		AuditLogDetail detail = AuditLogDetail.builder().id(1L).fieldName("points").oldValue("100").newValue("200")
				.build();

		AuditLog auditLog = AuditLog.builder().id(1L).entityType("BonusRules").entityId(10L).targetInfo("WELCOME_BONUS")
				.action(AuditAction.UPDATED).changedBy("admin@example.com")
				.changedAt(LocalDateTime.of(2024, 1, 15, 10, 30)).details(List.of(detail)).build();

		AuditLogResponse response = mapper.toResponse(auditLog);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.entityType()).isEqualTo("BonusRules");
		assertThat(response.entityId()).isEqualTo(10L);
		assertThat(response.targetInfo()).isEqualTo("WELCOME_BONUS");
		assertThat(response.action()).isEqualTo(AuditAction.UPDATED);
		assertThat(response.changedBy()).isEqualTo("admin@example.com");
		assertThat(response.changedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 10, 30));
		assertThat(response.details()).hasSize(1);
	}

	@Test
	void toResponse_WhenAuditLogHasNoDetails_ShouldReturnEmptyDetails() {
		AuditLog auditLog = AuditLog.builder().id(1L).entityType("User").entityId(5L).targetInfo("user@example.com")
				.action(AuditAction.CREATED).changedBy("system").changedAt(LocalDateTime.now()).details(List.of())
				.build();

		AuditLogResponse response = mapper.toResponse(auditLog);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.entityType()).isEqualTo("User");
		assertThat(response.details()).isEmpty();
	}

	@Test
	void toResponse_WhenAuditLogHasNullDetails_ShouldReturnNullDetails() {
		AuditLog auditLog = AuditLog.builder().id(1L).entityType("User").entityId(5L).targetInfo("user@example.com")
				.action(AuditAction.CREATED).changedBy("system").changedAt(LocalDateTime.now()).details(null).build();

		AuditLogResponse response = mapper.toResponse(auditLog);

		assertThat(response).isNotNull();
		assertThat(response.details()).isNull();
	}

	@Test
	void toResponse_WhenAuditLogHasMultipleDetails_ShouldMapAll() {
		AuditLogDetail detail1 = AuditLogDetail.builder().fieldName("points").oldValue("100").newValue("200").build();

		AuditLogDetail detail2 = AuditLogDetail.builder().fieldName("active").oldValue("true").newValue("false")
				.build();

		AuditLog auditLog = AuditLog.builder().id(1L).entityType("BonusRules").entityId(10L).targetInfo("WELCOME_BONUS")
				.action(AuditAction.UPDATED).changedBy("admin@example.com").changedAt(LocalDateTime.now())
				.details(List.of(detail1, detail2)).build();

		AuditLogResponse response = mapper.toResponse(auditLog);

		assertThat(response).isNotNull();
		assertThat(response.details()).hasSize(2);
		assertThat(response.details().get(0).fieldName()).isEqualTo("points");
		assertThat(response.details().get(1).fieldName()).isEqualTo("active");
	}

	@Test
	void toResponse_WhenAuditLogIsNull_ShouldReturnNull() {
		AuditLogResponse response = mapper.toResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toDetailResponse_ShouldMapAuditLogDetail() {
		AuditLogDetail detail = AuditLogDetail.builder().id(1L).fieldName("points").oldValue("100").newValue("200")
				.build();

		AuditLogResponse.AuditLogDetailResponse response = mapper.toDetailResponse(detail);

		assertThat(response).isNotNull();
		assertThat(response.fieldName()).isEqualTo("points");
		assertThat(response.oldValue()).isEqualTo("100");
		assertThat(response.newValue()).isEqualTo("200");
	}

	@Test
	void toDetailResponse_WhenDetailHasNullValues_ShouldMapCorrectly() {
		AuditLogDetail detail = AuditLogDetail.builder().id(1L).fieldName("active").oldValue(null).newValue("true")
				.build();

		AuditLogResponse.AuditLogDetailResponse response = mapper.toDetailResponse(detail);

		assertThat(response).isNotNull();
		assertThat(response.fieldName()).isEqualTo("active");
		assertThat(response.oldValue()).isNull();
		assertThat(response.newValue()).isEqualTo("true");
	}

	@Test
	void toDetailResponse_WhenDetailIsNull_ShouldReturnNull() {
		AuditLogResponse.AuditLogDetailResponse response = mapper.toDetailResponse(null);
		assertThat(response).isNull();
	}
}