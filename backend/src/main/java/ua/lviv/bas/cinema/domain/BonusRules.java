package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "bonus_rules", indexes = { @Index(name = "idx_bonus_rules_active", columnList = "is_active"),
		@Index(name = "idx_bonus_rules_type", columnList = "bonus_type") })
public class BonusRules {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "bonus_type", unique = true, nullable = false, length = 50)
	private BonusTransactionType bonusType;

	@Column(name = "points")
	@Min(0)
	private Integer points;

	@Column(name = "money_ratio", precision = 10, scale = 4)
	@DecimalMin(value = "0.00", inclusive = true)
	private BigDecimal moneyRatio;

	@Column(name = "point_value", precision = 10, scale = 2)
	@Builder.Default
	@DecimalMin(value = "0.01", inclusive = true)
	private BigDecimal pointValue = BigDecimal.ONE;

	@Column(name = "min_points_per_transaction")
	@Builder.Default
	@Min(1)
	private Integer minPointsPerTransaction = 50;

	@Column(name = "max_points_per_transaction")
	@Builder.Default
	@Min(1)
	private Integer maxPointsPerTransaction = 300;

	@Column(name = "active", nullable = false)
	@Builder.Default
	private boolean active = true;

	@UpdateTimestamp
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}