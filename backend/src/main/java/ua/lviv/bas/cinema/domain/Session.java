package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sessions")
public class Session {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@FutureOrPresent
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

	@OneToMany(mappedBy = "session", fetch = FetchType.LAZY)
	@Builder.Default
	private List<Ticket> tickets = new ArrayList<Ticket>();

	public LocalDateTime getEndTime() {
		if (movie == null || movie.getDurationMinutes() == null || startTime == null) {
			return null;
		}
		return startTime.plusMinutes(movie.getDurationMinutes());
	}

	public boolean isAvailable() {
		return startTime != null && startTime.isAfter(LocalDateTime.now());
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Session)) {
			return false;
		}
		Session other = (Session) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "Session [id=" + id + ", startTime=" + startTime + ", price=" + basePrice + "]";
	}

}
