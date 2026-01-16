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
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "movie", "hall", "bookings", "bookedSeats" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "sessions", indexes = { @Index(name = "idx_session_movie", columnList = "movie_id"),
		@Index(name = "idx_session_hall", columnList = "hall_id"),
		@Index(name = "idx_session_time", columnList = "start_time"),
		@Index(name = "idx_session_hall_time", columnList = "hall_id, start_time"),
		@Index(name = "idx_session_status", columnList = "status") })
public class Session {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(nullable = false, name = "start_time")
	private LocalDateTime startTime;

	@NotNull
	@Positive
	@Column(nullable = false, name = "base_price")
	private BigDecimal basePrice;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "movie_id", nullable = false)
	private Movie movie;

	@NotNull
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "hall_id", nullable = false)
	private CinemaHall hall;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	@Builder.Default
	private CinemaSessionStatus status = CinemaSessionStatus.SCHEDULED;

	@OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
	@Builder.Default
	private List<Booking> bookings = new ArrayList<>();

	@OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
	@Builder.Default
	private List<BookedSeat> bookedSeats = new ArrayList<>();
}