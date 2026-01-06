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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "booking", "seat", "session", "ticketType" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "booked_seats", indexes = { @Index(name = "idx_booked_seat_booking", columnList = "booking_id"),
		@Index(name = "idx_booked_seat_seat_session", columnList = "seat_id, session_id"),
		@Index(name = "idx_booked_seat_booking_session", columnList = "booking_id, session_id"),
		@Index(name = "idx_booked_seat_status", columnList = "status"),
		@Index(name = "idx_booked_seat_ticket_type", columnList = "ticket_type_id") }, uniqueConstraints = @UniqueConstraint(columnNames = {
				"session_id", "seat_id" }))
public class BookedSeat {

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

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private BookedSeatStatus status = BookedSeatStatus.PENDING;

	@Column(name = "booked_at", nullable = false)
	@Builder.Default
	private LocalDateTime bookedAt = LocalDateTime.now();
}