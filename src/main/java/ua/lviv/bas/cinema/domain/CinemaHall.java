package ua.lviv.bas.cinema.domain;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "cinema_halls")
public class CinemaHall {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String name;
	private int capacity;

	@OneToMany(mappedBy = "hall")
	private List<Session> sessions;

	@OneToMany(mappedBy = "hall")
	private List<Seat> seats;

	public CinemaHall() {
	}

	public CinemaHall(String name, int capacity, List<Session> sessions, List<Seat> seats) {
		this.name = name;
		this.capacity = capacity;
		this.sessions = sessions;
		this.seats = seats;
	}

	public CinemaHall(Integer id, String name, int capacity, List<Session> sessions, List<Seat> seats) {
		this.id = id;
		this.name = name;
		this.capacity = capacity;
		this.sessions = sessions;
		this.seats = seats;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}

	public List<Seat> getSeats() {
		return seats;
	}

	public void setSeats(List<Seat> seats) {
		this.seats = seats;
	}

	@Override
	public int hashCode() {
		return Objects.hash(capacity, id, name, seats, sessions);
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
		CinemaHall other = (CinemaHall) obj;
		return capacity == other.capacity && Objects.equals(id, other.id) && Objects.equals(name, other.name)
				&& Objects.equals(seats, other.seats) && Objects.equals(sessions, other.sessions);
	}

	@Override
	public String toString() {
		return "CinemaHall [id=" + id + ", name=" + name + ", capacity=" + capacity + ", sessions=" + sessions
				+ ", seats=" + seats + "]";
	}

}
