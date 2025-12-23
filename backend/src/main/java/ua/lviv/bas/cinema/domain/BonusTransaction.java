package ua.lviv.bas.cinema.domain;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@ToString(exclude = { "bonusCard", "payment", "refund" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "bonus_transactions", indexes = { @Index(name = "idx_bonus_trans_card", columnList = "bonus_card_id"),
		@Index(name = "idx_bonus_trans_created", columnList = "created_at"),
		@Index(name = "idx_bonus_trans_type", columnList = "type"),
		@Index(name = "idx_bonus_trans_payment", columnList = "payment_id"),
		@Index(name = "idx_bonus_trans_refund", columnList = "refund_id") })
public class BonusTransaction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bonus_card_id", nullable = false)
	private BonusCard bonusCard;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "refund_id")
	private Refund refund;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 30)
	private BonusTransactionType type;

	@NotNull
	@Column(name = "points_change", nullable = false)
	private Integer pointsChange;

	@Size(max = 50)
	@Column(name = "reference_id", length = 50)
	private String referenceId;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}