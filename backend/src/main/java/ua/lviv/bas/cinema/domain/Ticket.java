package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.BookingType;
import ua.lviv.bas.cinema.domain.enums.RefundStatus;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "session", "seat", "user", "ticketType", "promotion", "payment", "appliedDiscount", "refund" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "ticket")
public class Ticket {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "purchase_time")
	private LocalDateTime purchaseTime;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	private Session session;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_type_id", nullable = false)
	private TicketType ticketType;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "discount_id")
	private Discount appliedDiscount;

	@OneToOne(mappedBy = "ticket", fetch = FetchType.LAZY)
	private Refund refund;

	@Column(name = "calculated_price", precision = 10, scale = 2)
	private BigDecimal calculatedPrice;

	@Column(name = "final_price", precision = 10, scale = 2)
	private BigDecimal finalPrice;

	@Column(name = "base_price_at_purchase", precision = 10, scale = 2)
	private BigDecimal basePriceAtPurchase;

	@Column(name = "discount_amount", precision = 10, scale = 2)
	private BigDecimal discountAmount;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "promotion_id")
	private Promotion promotion;

	@Column(name = "unique_code", unique = true, length = 20)
	private String uniqueCode;

	@Enumerated(EnumType.STRING)
	@Column(name = "booking_type")
	private BookingType bookingType;

	@Column(nullable = false, name = "status")
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private TicketStatus ticketStatus = TicketStatus.ACTIVE;

	@PrePersist
	protected void onCreate() {
		if (purchaseTime == null) {
			purchaseTime = LocalDateTime.now();
		}
		if (uniqueCode == null) {
			uniqueCode = "TKT" + (System.currentTimeMillis() % 1000000000L);
		}
		if (session != null && basePriceAtPurchase == null) {
			basePriceAtPurchase = session.getBasePrice();
		}
	}

	public boolean isRefunded() {
		return refund != null && refund.getStatus() == RefundStatus.PROCESSED;
	}

	public boolean isRefundable() {
		return ticketStatus == TicketStatus.ACTIVE && session.getStartTime().isAfter(LocalDateTime.now().plusHours(2));
	}
}