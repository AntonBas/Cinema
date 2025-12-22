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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@ToString(exclude = { "bookedSeat", "payment", "refund" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "tickets")
public class Ticket {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "purchase_time", nullable = false)
	private LocalDateTime purchaseTime;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booked_seat_id", nullable = false, unique = true)
	private BookedSeat bookedSeat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	@Column(name = "final_price", precision = 10, scale = 2, nullable = false)
	private BigDecimal finalPrice;

	@Column(name = "unique_code", unique = true, length = 20)
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