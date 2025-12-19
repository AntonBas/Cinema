package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "bonus_rules")
public class BonusRules {

	@Id
	@EqualsAndHashCode.Include
	@Enumerated(EnumType.STRING)
	@Column(name = "bonus_type", length = 50)
	private BonusTransactionType bonusType;

	@Column(name = "points")
	private Integer points;

	@Column(name = "money_ratio", precision = 10, scale = 4)
	private BigDecimal moneyRatio;

	@Column(name = "point_value", precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal pointValue = BigDecimal.ONE;

	@Column(name = "min_points_per_transaction")
	@Builder.Default
	private Integer minPointsPerTransaction = 50;

	@Column(name = "max_points_per_transaction")
	@Builder.Default
	private Integer maxPointsPerTransaction = 300;

	@Column(name = "is_active", nullable = false)
	@Builder.Default
	private Boolean isActive = true;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}

	@Transient
	public BigDecimal calculateMoneyValue(Integer points) {
		if (points == null || pointValue == null) {
			return BigDecimal.ZERO;
		}
		return pointValue.multiply(BigDecimal.valueOf(points));
	}

	@Transient
	public boolean isValidForWriteOff(Integer pointsToUse) {
		if (!isActive || pointValue == null || bonusType != BonusTransactionType.PURCHASE_WRITE_OFF) {
			return false;
		}

		return pointsToUse != null && pointsToUse >= minPointsPerTransaction && pointsToUse <= maxPointsPerTransaction;
	}

	@Transient
	public boolean isValidForPurchaseBonus() {
		return isActive && moneyRatio != null && bonusType == BonusTransactionType.PURCHASE_BONUS;
	}
}