package ua.lviv.bas.cinema.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@ToString(exclude = { "bonusCard", "payment" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "bonus_transaction")
public class BonusTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bonus_card_id", nullable = false)
	private BonusCard bonusCard;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 30)
	private BonusTransactionType type;

	@Column(name = "points_change", nullable = false)
	private Integer pointsChange;

	@Column(name = "description", length = 300)
	private String description;

	@Column(name = "reference_id", length = 50)
	private String referenceId;

	@Column(name = "created_at", nullable = false, updatable = false)
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();

	public boolean isCredit() {
		return pointsChange > 0;
	}

	public boolean isDebit() {
		return pointsChange < 0;
	}

	public Integer getAbsolutePoints() {
		return Math.abs(pointsChange);
	}
}