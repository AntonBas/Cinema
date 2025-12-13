package ua.lviv.bas.cinema.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.TokenType;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "user" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "email_token", indexes = { @Index(name = "idx_email_token_user_id", columnList = "user_id"),
		@Index(name = "idx_email_token_expires", columnList = "expires_at"),
		@Index(name = "idx_email_token_type", columnList = "type") })
public class EmailToken {

	@Id
	@Column(length = 255)
	@EqualsAndHashCode.Include
	private String token;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@NotNull
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@NotNull
	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private TokenType type;

	@Builder.Default
	@Column(nullable = false)
	private boolean confirmed = false;

	@Column(name = "confirmed_at")
	private LocalDateTime confirmedAt;

	@Column(name = "new_email")
	private String newEmail;

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}

	public boolean isValid() {
		return !confirmed && !isExpired();
	}
}