package ua.lviv.bas.cinema.domain;

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
@Table(name = "seats")
public class Seat {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false, name = "row_num")
	private int rowNumber;
	
	@Column(nullable = false, name = "seat_num")
	private int seatNumber;
	
	@Column (nullable = false, name = "is_vip")
	private boolean isVip;

	@ManyToOne
	@JoinColumn(name = "hall_id")
	private CinemaHall hall;

	@OneToMany(mappedBy = "seat")
	private List<Ticket> tickets;

	public Seat() {
	}

	public Seat(int rowNumber, int seatNumber, boolean isVip, CinemaHall hall, List<Ticket> tickets) {
		this.rowNumber = rowNumber;
		this.seatNumber = seatNumber;
		this.isVip = isVip;
		this.hall = hall;
		this.tickets = tickets;
	}

	public Seat(Long id, int rowNumber, int seatNumber, boolean isVip, CinemaHall hall, List<Ticket> tickets) {
		this.id = id;
		this.rowNumber = rowNumber;
		this.seatNumber = seatNumber;
		this.isVip = isVip;
		this.hall = hall;
		this.tickets = tickets;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public int getSeatNumber() {
		return seatNumber;
	}

	public void setSeatNumber(int seatNumber) {
		this.seatNumber = seatNumber;
	}

	public boolean isVip() {
		return isVip;
	}

	public void setVip(boolean isVip) {
		this.isVip = isVip;
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
		return Objects.hash(hall, id, isVip, rowNumber, seatNumber, tickets);
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
		Seat other = (Seat) obj;
		return Objects.equals(hall, other.hall) && Objects.equals(id, other.id) && isVip == other.isVip
				&& rowNumber == other.rowNumber && seatNumber == other.seatNumber
				&& Objects.equals(tickets, other.tickets);
	}

	@Override
	public String toString() {
		return "Seat [id=" + id + ", rowNumber=" + rowNumber + ", seatNumber=" + seatNumber + ", isVip=" + isVip
				+ ", hall=" + hall + ", tickets=" + tickets + "]";
	}

}