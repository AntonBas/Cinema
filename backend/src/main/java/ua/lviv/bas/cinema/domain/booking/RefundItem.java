package ua.lviv.bas.cinema.domain.booking;

import java.math.BigDecimal;

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
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.lviv.bas.cinema.domain.audit.AuditableEntity;
import ua.lviv.bas.cinema.domain.booking.status.RefundItemStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "refund_items", indexes = { @Index(name = "idx_refund_item_refund", columnList = "refund_id"),
		@Index(name = "idx_refund_item_ticket", columnList = "ticket_id"),
		@Index(name = "idx_refund_item_status", columnList = "status") })
public class RefundItem extends AuditableEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
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

	@DecimalMin("0.00")
	@DecimalMax("100.00")
	@Column(name = "refund_percentage", precision = 10, scale = 2, nullable = false)
	@Builder.Default
	private BigDecimal refundPercentage = BigDecimal.ZERO;

	@Column(name = "refund_amount", precision = 10, scale = 2)
	private BigDecimal refundAmount;

	@Column(name = "bonus_points_to_deduct", nullable = false)
	@Builder.Default
	private Integer bonusPointsToDeduct = 0;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private RefundItemStatus status = RefundItemStatus.PENDING;
}