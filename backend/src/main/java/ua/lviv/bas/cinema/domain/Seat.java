package ua.lviv.bas.cinema.domain;

import java.util.List;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.lviv.bas.cinema.domain.enums.SeatType;

@Entity
@Table(name = "seats", uniqueConstraints = { @UniqueConstraint(columnNames = { "hall_id", "seat_row", "number" }) })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Min(value = 1, message = "Row must be at least 1")
	@Column(name = "seat-row", nullable = false)
	private int row;

	@Min(value = 1, message = "Seat must be at least 1")
	@Column(nullable = false)
	private int number;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	@Builder.Default
	private SeatType seatType = SeatType.STANDARD;

	@ManyToOne
	@JoinColumn(name = "hall_id")
	private CinemaHall hall;

	@OneToMany(mappedBy = "seat", fetch = FetchType.LAZY)
	private List<Ticket> tickets;
}