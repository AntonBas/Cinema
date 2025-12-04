package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
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
import jakarta.validation.constraints.NotNull;
import ua.lviv.bas.cinema.domain.enums.BookingType;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Entity
@Table(name = "tickets")
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(nullable = false, name = "purchase_time")
	private LocalDateTime purchaseTime;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	private Session session;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_type_id", nullable = false)
	private TicketType ticketType;

	@Column(name = "calculated_price", precision = 10, scale = 2)
	private BigDecimal calculatedPrice;

	@Column(name = "base_price_at_purchase", precision = 10, scale = 2)
	private BigDecimal basePriceAtPurchase;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "promotion_id")
	private Promotion promotion;

	@Column(name = "unique_code", unique = true, length = 20)
	private String uniqueCode;

	@Enumerated(EnumType.STRING)
	@Column(name = "booking_type")
	private BookingType bookingType;

	@NotNull
	@Column(nullable = false, name = "status")
	@Enumerated(EnumType.STRING)
	private TicketStatus ticketStatus;

	@OneToOne(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Payment payment;

	@PrePersist
	protected void onCreate() {
		if (purchaseTime == null) {
			purchaseTime = LocalDateTime.now();
		}
		if (uniqueCode == null) {
			uniqueCode = "TKT" + (System.currentTimeMillis() % 1000000000L);
		}
		if (session != null) {
			basePriceAtPurchase = session.getBasePrice();
		}
	}
}