package ua.lviv.bas.cinema.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.TokenType;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_tokens", indexes = { @Index(name = "idx_email_token_user_id", columnList = "user_id"),
		@Index(name = "idx_email_token_expires", columnList = "expires_at"),
		@Index(name = "idx_email_token_type", columnList = "type") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailToken {

	@Id
	@Column(length = 255)
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

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}

	public boolean isValid() {
		return !confirmed && !isExpired();
	}
}