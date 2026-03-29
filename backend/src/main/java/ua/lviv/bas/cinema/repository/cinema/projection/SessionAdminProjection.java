package ua.lviv.bas.cinema.repository.cinema.projection;

import java.math.BigDecimal;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SessionAdminProjection {
	private Long id;
	private Timestamp startTime;
	private Timestamp endTime;
	private BigDecimal basePrice;
	private String status;
	private Long movieId;
	private String movieTitle;
	private Integer movieDuration;
	private Long hallId;
	private String hallName;
	private Long hallCapacity;
	private Long ticketsSold;
	private BigDecimal totalRevenue;
}