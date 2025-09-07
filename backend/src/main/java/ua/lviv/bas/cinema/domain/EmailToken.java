package ua.lviv.bas.cinema.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.TokenType;

@Entity
@Table(name = "email_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailToken {

	@Id
	private String token;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TokenType type;

	@Builder.Default
	@Column(nullable = false)
	private boolean confirmed = false;

	@Column(name = "confirmed_at")
	private LocalDateTime confirmedAt;
}
