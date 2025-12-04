package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "bonus_cards")
public class BonusCard {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "card_number", unique = true, nullable = false, length = 20)
	private String cardNumber;

	@Column(name = "bonus_points")
	@Builder.Default
	private Integer bonusPoints = 0;

	@Column(name = "total_spent", precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal totalSpent = BigDecimal.ZERO;

	@Column(name = "discount_percentage")
	@Builder.Default
	private Integer discountPercentage = 0;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "expires_at")
	private LocalDateTime expiresAt;

	@Column(name = "is_active")
	@Builder.Default
	private Boolean isActive = true;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", unique = true, nullable = false)
	private User user;

	@PrePersist
	protected void onCreate() {
		if (createdAt == null) {
			createdAt = LocalDateTime.now();
		}
		if (expiresAt == null) {
			expiresAt = createdAt.plusYears(1);
		}
		if (cardNumber == null) {
			cardNumber = "BC" + (1000000 + (int) (Math.random() * 9000000));
		}
	}
}