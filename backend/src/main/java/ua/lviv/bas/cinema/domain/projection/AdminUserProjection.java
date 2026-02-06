package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDateTime;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "admin_user_projection")
@Immutable
public class AdminUserProjection {

	@Id
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String email;

	@Column(nullable = false, name = "first_name", length = 50)
	private String firstName;

	@Column(nullable = false, name = "last_name", length = 50)
	private String lastName;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "user_role", length = 50)
	private UserRole userRole;

	@Column(nullable = false)
	private boolean enabled;

	@Enumerated(EnumType.STRING)
	@Column(name = "verification_status", nullable = false)
	private VerificationStatus verificationStatus;

	@Column(name = "verified_at")
	private LocalDateTime verifiedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Formula("(SELECT COUNT(t.id) FROM tickets t WHERE t.user_id = id)")
	@Column(name = "tickets_count", nullable = false)
	private int ticketsCount;

	@Formula("updated_at")
	@Column(name = "last_activity")
	private LocalDateTime lastActivity;
}