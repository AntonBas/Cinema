package ua.lviv.bas.cinema.dto.session.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionAdminResponse {

	private Long id;
	private LocalDateTime startTime;
	private LocalDateTime endTime;
	private BigDecimal basePrice;
	private boolean available;

	private Long movieId;
	private String movieTitle;
	private Integer movieDuration;

	private Long hallId;
	private String hallName;
	private Integer hallCapacity;

	private Integer ticketsSold;
	private BigDecimal totalRevenue;
}