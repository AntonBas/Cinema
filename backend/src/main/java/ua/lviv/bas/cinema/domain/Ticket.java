package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "booking", "ticketType", "payment", "refund" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "tickets", indexes = { @Index(name = "idx_ticket_booking", columnList = "booking_id"),
		@Index(name = "idx_ticket_payment", columnList = "payment_id"),
		@Index(name = "idx_ticket_status", columnList = "status"),
		@Index(name = "idx_ticket_purchase_time", columnList = "purchase_time"),
		@Index(name = "idx_ticket_ticket_type", columnList = "ticket_type_id"),
		@Index(name = "idx_ticket_unique_code", columnList = "unique_code", unique = true) })
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id", nullable = false)
	private Booking booking;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_type_id", nullable = false)
	private TicketType ticketType;

	@NotNull
	@Column(name = "purchase_time", nullable = false)
	@Builder.Default
	private LocalDateTime purchaseTime = LocalDateTime.now();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@NotNull
	@DecimalMin("0.01")
	@Column(name = "original_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal originalPrice;

	@NotNull
	@DecimalMin("0.00")
	@Column(name = "final_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal finalPrice;

	@Column(name = "discount_amount", precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal discountAmount = BigDecimal.ZERO;

	@NotNull
	@Column(name = "unique_code", unique = true, nullable = false, length = 20)
	private String uniqueCode;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private TicketStatus status = TicketStatus.ACTIVE;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@OneToOne(mappedBy = "ticket", fetch = FetchType.LAZY)
	private Refund refund;
}