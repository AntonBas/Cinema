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
@Schema(description = "Response DTO for movie session information in schedule view")
public class SessionScheduleResponse {

	@Schema(description = "Unique identifier of the session", example = "1")
	private Long id;

	@Schema(description = "Start time of the session", example = "2024-01-15T18:30:00", type = "string", format = "date-time")
	private LocalDateTime startTime;

	@Schema(description = "End time of the session (calculated based on movie duration)", example = "2024-01-15T21:00:00", type = "string", format = "date-time")
	private LocalDateTime endTime;

	@Schema(description = "Base price for a standard seat", example = "150.00")
	private BigDecimal basePrice;

	@Schema(description = "Current status of the session", example = "SCHEDULED")
	private CinemaSessionStatus status;

	@Schema(description = "Number of available seats for this session", example = "105")
	private Integer availableSeats;

	@Schema(description = "ID of the movie being shown", example = "5")
	private Long movieId;

	@Schema(description = "Title of the movie", example = "Inception")
	private String movieTitle;

	@Schema(description = "File name of the movie poster", example = "inception-poster.jpg")
	private String moviePosterFileName;

	@Schema(description = "Age rating of the movie", example = "PG_13")
	private String movieAgeRating;

	@Schema(description = "Duration of the movie in minutes", example = "148")
	private Integer movieDuration;

	@Schema(description = "ID of the cinema hall", example = "3")
	private Long hallId;

	@Schema(description = "Name of the cinema hall", example = "Hall A - Dolby Atmos")
	private String hallName;

	@Schema(description = "Capacity of the cinema hall", example = "150")
	private int hallCapacity;
}