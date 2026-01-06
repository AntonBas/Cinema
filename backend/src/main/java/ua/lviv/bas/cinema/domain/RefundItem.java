package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.RefundItemStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "refund", "ticket" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "refund_items", indexes = { @Index(name = "idx_refund_item_refund", columnList = "refund_id"),
		@Index(name = "idx_refund_item_ticket", columnList = "ticket_id"),
		@Index(name = "idx_refund_item_status", columnList = "status") })
public class RefundItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "refund_id", nullable = false)
	private Refund refund;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_id", nullable = false)
	private Ticket ticket;

	@NotNull
	@Positive
	@Column(name = "ticket_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal ticketPrice;

	@Column(name = "refund_amount", precision = 10, scale = 2)
	private BigDecimal refundAmount;

	@Column(name = "bonus_points_to_deduct", nullable = false)
	@Builder.Default
	private Integer bonusPointsToDeduct = 0;

	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private RefundItemStatus status = RefundItemStatus.PENDING;

	@CreationTimestamp
	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;
}