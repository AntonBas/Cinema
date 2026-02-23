package ua.lviv.bas.cinema.dto.session.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Admin view of movie session with statistics")
public class SessionAdminResponse {

	@Schema(description = "Unique identifier of the session", example = "1")
	private Long id;

	@Schema(description = "Start time of the session", example = "2024-01-15T18:30:00")
	private LocalDateTime startTime;

	@Schema(description = "End time of the session", example = "2024-01-15T21:00:00")
	private LocalDateTime endTime;

	@Schema(description = "Base price", example = "150.00")
	private BigDecimal basePrice;

	@Schema(description = "Current status", example = "SCHEDULED")
	private CinemaSessionStatus status;

	@Schema(description = "ID of the movie", example = "5")
	private Long movieId;

	@Schema(description = "Title of the movie", example = "Inception")
	private String movieTitle;

	@Schema(description = "Duration in minutes", example = "148")
	private Integer movieDuration;

	@Schema(description = "ID of the cinema hall", example = "3")
	private Long hallId;

	@Schema(description = "Name of the cinema hall", example = "Hall A")
	private String hallName;

	@Schema(description = "Hall capacity", example = "150")
	private Integer hallCapacity;

	@Schema(description = "Tickets sold", example = "45")
	private Integer ticketsSold;

	@Schema(description = "Total revenue", example = "6750.00")
	private BigDecimal totalRevenue;
}