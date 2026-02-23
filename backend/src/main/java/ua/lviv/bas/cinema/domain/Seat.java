package ua.lviv.bas.cinema.domain;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.SeatType;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "hall", "seatReservations" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "seats", indexes = { @Index(name = "idx_seat_hall", columnList = "hall_id"),
		@Index(name = "idx_seat_hall_type", columnList = "hall_id, seat_type"),
		@Index(name = "idx_seat_active", columnList = "active"),
		@Index(name = "idx_seat_hall_active", columnList = "hall_id, active"),
		@Index(name = "idx_seat_position", columnList = "hall_id, seat_row, number") }, uniqueConstraints = {
				@UniqueConstraint(columnNames = { "hall_id", "seat_row", "number" }) })
public class Seat {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Positive
	@NotNull
	@Column(name = "seat_row", nullable = false)
	private Integer row;

	@Positive
	@NotNull
	@Column(nullable = false)
	private Integer number;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private SeatType seatType = SeatType.STANDARD;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hall_id", nullable = false)
	private CinemaHall hall;

	@Column(name = "active", nullable = false)
	@Builder.Default
	private boolean active = true;

	@OneToMany(mappedBy = "seat", fetch = FetchType.LAZY)
	@Builder.Default
	private List<SeatReservation> seatReservations = new ArrayList<>();
}