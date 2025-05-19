package ua.lviv.bas.cinema.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "sessions")
public class Session {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "start_time")
	private LocalDateTime startTime;

	@Column(nullable = false, name = "end_time")
	private LocalDateTime endTime;
	
	@Column(nullable = false)
	private BigDecimal price;

	@ManyToOne
	@JoinColumn(name = "movie_id")
	private Movie movie;

	@ManyToOne
	@JoinColumn(name = "hall_id")
	private CinemaHall hall;

	@OneToMany(mappedBy = "session")
	private List<Ticket> tickets;

	public Session() {
	}

	public Session(LocalDateTime startTime, LocalDateTime endTime, BigDecimal price, Movie movie, CinemaHall hall,
			List<Ticket> tickets) {
		this.startTime = startTime;
		this.endTime = endTime;
		this.price = price;
		this.movie = movie;
		this.hall = hall;
		this.tickets = tickets;
	}

	public Session(Long id, LocalDateTime startTime, LocalDateTime endTime, BigDecimal price, Movie movie,
			CinemaHall hall, List<Ticket> tickets) {
		this.id = id;
		this.startTime = startTime;
		this.endTime = endTime;
		this.price = price;
		this.movie = movie;
		this.hall = hall;
		this.tickets = tickets;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;
	}

	public LocalDateTime getEndTime() {
		return endTime;
	}

	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public Movie getMovie() {
		return movie;
	}

	public void setMovie(Movie movie) {
		this.movie = movie;
	}

	public CinemaHall getHall() {
		return hall;
	}

	public void setHall(CinemaHall hall) {
		this.hall = hall;
	}

	public List<Ticket> getTickets() {
		return tickets;
	}

	public void setTickets(List<Ticket> tickets) {
		this.tickets = tickets;
	}

	@Override
	public int hashCode() {
		return Objects.hash(endTime, hall, id, movie, price, startTime, tickets);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Session other = (Session) obj;
		return Objects.equals(endTime, other.endTime) && Objects.equals(hall, other.hall)
				&& Objects.equals(id, other.id) && Objects.equals(movie, other.movie)
				&& Objects.equals(price, other.price) && Objects.equals(startTime, other.startTime)
				&& Objects.equals(tickets, other.tickets);
	}

	@Override
	public String toString() {
		return "Session [id=" + id + ", startTime=" + startTime + ", endTime=" + endTime + ", price=" + price
				+ ", movie=" + movie + ", hall=" + hall + ", tickets=" + tickets + "]";
	}

}