package ua.lviv.bas.cinema.dto.session.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO for movie session information (admin view with statistics)")
public class SessionAdminResponse {

	@Schema(description = "Unique identifier of the session", example = "1")
	private Long id;

	@Schema(description = "Start time of the session", example = "2024-01-15T18:30:00", type = "string", format = "date-time")
	private LocalDateTime startTime;

	@Schema(description = "End time of the session (calculated based on movie duration)", example = "2024-01-15T21:00:00", type = "string", format = "date-time")
	private LocalDateTime endTime;

	@Schema(description = "Base price for a standard seat", example = "150.00")
	private BigDecimal basePrice;

	@Schema(description = "Indicates if the session is available for booking", example = "true")
	private boolean available;

	@Schema(description = "ID of the movie being shown", example = "5")
	private Long movieId;

	@Schema(description = "Title of the movie", example = "Inception")
	private String movieTitle;

	@Schema(description = "Duration of the movie in minutes", example = "148")
	private Integer movieDuration;

	@Schema(description = "ID of the cinema hall", example = "3")
	private Long hallId;

	@Schema(description = "Name of the cinema hall", example = "Hall A - Dolby Atmos")
	private String hallName;

	@Schema(description = "Total capacity of the cinema hall", example = "150")
	private Integer hallCapacity;

	@Schema(description = "Number of tickets sold for this session", example = "45")
	private Integer ticketsSold;

	@Schema(description = "Total revenue generated from this session", example = "6750.00")
	private BigDecimal totalRevenue;
}