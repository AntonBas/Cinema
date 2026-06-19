package ua.lviv.bas.cinema.domain.audit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "details")
@Table(name = "audit_log")
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String entityType;

	@Column(nullable = false)
	private Long entityId;

	@Column(name = "target_info")
	private String targetInfo;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private AuditAction action;

	@Column(nullable = false)
	private String changedBy;

	@Column(nullable = false)
	private LocalDateTime changedAt;

	@OneToMany(mappedBy = "auditLog", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<AuditLogDetail> details = new ArrayList<>();
}