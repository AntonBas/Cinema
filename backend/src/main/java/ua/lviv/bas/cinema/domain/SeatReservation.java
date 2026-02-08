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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "booking", "seat", "session", "ticketType", "reservedByUser" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "seat_reservations", indexes = { @Index(name = "idx_seat_reservation_booking", columnList = "booking_id"),
		@Index(name = "idx_seat_reservation_session", columnList = "session_id"),
		@Index(name = "idx_seat_reservation_seat", columnList = "seat_id"),
		@Index(name = "idx_seat_reservation_status", columnList = "status"),
		@Index(name = "idx_seat_reservation_reserved_until", columnList = "reserved_until"),
		@Index(name = "idx_seat_reservation_composite", columnList = "session_id, seat_id, status"),
		@Index(name = "idx_seat_reservation_active", columnList = "status, reserved_until"),
		@Index(name = "idx_seat_reservation_created", columnList = "reserved_at"),
		@Index(name = "idx_seat_reservation_user", columnList = "user_id") })
public class SeatReservation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id")
	private Booking booking;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	private Session session;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_type_id", nullable = false)
	private TicketType ticketType;

	@NotNull
	@DecimalMin("0.00")
	@Column(name = "seat_price", nullable = false, precision = 10, scale = 2)
	private BigDecimal seatPrice;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private ReservationStatus status = ReservationStatus.PENDING;

	@Column(name = "reserved_at", nullable = false)
	@Builder.Default
	private LocalDateTime reservedAt = LocalDateTime.now();

	@NotNull
	@Column(name = "reserved_until", nullable = false)
	private LocalDateTime reservedUntil;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User reservedByUser;
}