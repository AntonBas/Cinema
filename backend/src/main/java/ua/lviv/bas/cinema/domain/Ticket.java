package ua.lviv.bas.cinema.domain;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Entity
@Table(name = "tickets")
public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, name = "purchase_time")
	private LocalDateTime purchaseTime;

	@ManyToOne
	@JoinColumn(name = "session_id")
	private Session session;

	@ManyToOne
	@JoinColumn(name = "seat_id")
	private Seat seat;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false, name = "status")
	@Enumerated(EnumType.STRING)
	private TicketStatus ticketStatus;

	public Ticket() {
		super();
	}

	public Ticket(LocalDateTime purchaseTime, Session session, Seat seat, User user) {
		super();
		this.purchaseTime = purchaseTime;
		this.session = session;
		this.seat = seat;
		this.user = user;
	}

	public Ticket(Long id, LocalDateTime purchaseTime, Session session, Seat seat, User user) {
		super();
		this.id = id;
		this.purchaseTime = purchaseTime;
		this.session = session;
		this.seat = seat;
		this.user = user;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public LocalDateTime getPurchaseTime() {
		return purchaseTime;
	}

	public void setPurchaseTime(LocalDateTime purchaseTime) {
		this.purchaseTime = purchaseTime;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public Seat getSeat() {
		return seat;
	}

	public void setSeat(Seat seat) {
		this.seat = seat;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, purchaseTime, seat, session, user);
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
		Ticket other = (Ticket) obj;
		return Objects.equals(id, other.id) && Objects.equals(purchaseTime, other.purchaseTime)
				&& Objects.equals(seat, other.seat) && Objects.equals(session, other.session)
				&& Objects.equals(user, other.user);
	}

	@Override
	public String toString() {
		return "Ticket [id=" + id + ", purchaseTime=" + purchaseTime + ", session=" + session + ", seat=" + seat
				+ ", user=" + user + "]";
	}

}