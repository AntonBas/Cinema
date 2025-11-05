package ua.lviv.bas.cinema.domain;

import java.time.LocalDateTime;
import java.util.Objects;

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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.lviv.bas.cinema.domain.enums.TokenType;

@Entity

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "email_tokens", indexes = { @Index(name = "idx_email_token_user_id", columnList = "user_id"),
		@Index(name = "idx_email_token_expires", columnList = "expires_at"),
		@Index(name = "idx_email_token_type", columnList = "type") })
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

	@Column(name = "new_email")
	private String newEmail;

	public boolean isExpired() {
		return LocalDateTime.now().isAfter(expiresAt);
	}

	public boolean isValid() {
		return !confirmed && !isExpired();
	}

	@Override
	public int hashCode() {
		return Objects.hash(token);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EmailToken)) {
			return false;
		}
		EmailToken other = (EmailToken) obj;
		return Objects.equals(token, other.token);
	}

	@Override
	public String toString() {
		return "EmailToken [token=" + token + "]";
	}

}