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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
@ToString(exclude = { "bookedSeat", "payment", "refund", "user" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "tickets", indexes = { @Index(name = "idx_ticket_payment", columnList = "payment_id"),
		@Index(name = "idx_ticket_status", columnList = "status"),
		@Index(name = "idx_ticket_purchase_time", columnList = "purchase_time"),
		@Index(name = "idx_ticket_booked_seat", columnList = "booked_seat_id"),
		@Index(name = "idx_ticket_user", columnList = "user_id") })
public class Ticket {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(name = "purchase_time", nullable = false)
	private LocalDateTime purchaseTime;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booked_seat_id", nullable = false, unique = true)
	private BookedSeat bookedSeat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id")
	private Payment payment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@NotNull
	@Positive
	@Column(name = "final_price", precision = 10, scale = 2, nullable = false)
	private BigDecimal finalPrice;

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