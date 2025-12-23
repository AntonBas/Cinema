package ua.lviv.bas.cinema.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "booking", "session", "seat", "ticketType", "ticket" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "booked_seats", indexes = { @Index(name = "idx_booked_seat_booking", columnList = "booking_id"),
		@Index(name = "idx_booked_seat_session", columnList = "session_id"),
		@Index(name = "idx_booked_seat_seat", columnList = "seat_id"),
		@Index(name = "idx_booked_seat_session_seat", columnList = "session_id, seat_id") })
public class BookedSeat {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "booking_id", nullable = false)
	private Booking booking;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "session_id", nullable = false)
	private Session session;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "seat_id", nullable = false)
	private Seat seat;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ticket_type_id", nullable = false)
	private TicketType ticketType;

	@OneToOne(mappedBy = "bookedSeat", fetch = FetchType.LAZY)
	private Ticket ticket;
}