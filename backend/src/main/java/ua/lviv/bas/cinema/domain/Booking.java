package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
import jakarta.persistence.OneToMany;
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
import ua.lviv.bas.cinema.domain.enums.BookingStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "user", "session", "seatReservations", "tickets", "bonusTransactions" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "bookings", indexes = { @Index(name = "idx_booking_user", columnList = "user_id"),
		@Index(name = "idx_booking_session", columnList = "session_id"),
		@Index(name = "idx_booking_status", columnList = "status"),
		@Index(name = "idx_booking_expires", columnList = "expires_at"),
		@Index(name = "idx_booking_created", columnList = "created_at"),
		@Index(name = "idx_booking_final_price", columnList = "final_price") })
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	private Session session;

	@OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, orphanRemoval = true)
	@Builder.Default
	private List<SeatReservation> seatReservations = new ArrayList<>();

	@OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, orphanRemoval = true)
	@Builder.Default
	private List<Ticket> tickets = new ArrayList<>();

	@OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, orphanRemoval = true)
	@Builder.Default
	private List<BonusTransaction> bonusTransactions = new ArrayList<>();

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private BookingStatus status = BookingStatus.PENDING;

	@NotNull
	@DecimalMin("0.01")
	@Column(name = "total_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalPrice;

	@Column(name = "bonus_points_used")
	@Builder.Default
	private Integer bonusPointsUsed = 0;

	@Column(name = "bonus_discount_amount", precision = 10, scale = 2)
	@Builder.Default
	private BigDecimal bonusDiscountAmount = BigDecimal.ZERO;

	@NotNull
	@DecimalMin("0.00")
	@Column(name = "final_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal finalPrice;

	@NotNull
	@Column(name = "expires_at", nullable = false)
	private LocalDateTime expiresAt;

	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@OneToOne(mappedBy = "booking", fetch = FetchType.LAZY)
	private Payment payment;
}