package ua.lviv.bas.cinema.domain.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.Formula;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "session_admin_projection")
@Immutable
public class SessionAdminProjection {

	@Id
	private Long id;

	@Column(name = "start_time", nullable = false)
	private LocalDateTime startTime;

	@Column(name = "base_price", nullable = false)
	private BigDecimal basePrice;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CinemaSessionStatus status;

	@Column(name = "movie_id", nullable = false)
	private Long movieId;

	@Column(name = "movie_title", nullable = false, length = 100)
	private String movieTitle;

	@Column(name = "movie_duration", nullable = false)
	private Integer movieDuration;

	@Column(name = "hall_id", nullable = false)
	private Long hallId;

	@Column(name = "hall_name", nullable = false, length = 50)
	private String hallName;

	@Formula("(SELECT COUNT(*) FROM seats s WHERE s.hall_id = hall_id AND s.active = true)")
	@Column(name = "hall_capacity", nullable = false)
	private Integer hallCapacity;

	@Formula("(SELECT COUNT(*) FROM tickets t WHERE t.booking_id IN (SELECT b.id FROM bookings b WHERE b.session_id = id AND b.status = 'CONFIRMED'))")
	@Column(name = "tickets_sold", nullable = false)
	private Integer ticketsSold;

	@Formula("(SELECT COALESCE(SUM(t.final_price), 0) FROM tickets t WHERE t.booking_id IN (SELECT b.id FROM bookings b WHERE b.session_id = id AND b.status = 'CONFIRMED'))")
	@Column(name = "total_revenue", nullable = false)
	private BigDecimal totalRevenue;

	@Formula("(SELECT TIMESTAMPADD(MINUTE, movie_duration, start_time))")
	@Column(name = "end_time", nullable = false)
	private LocalDateTime endTime;
}