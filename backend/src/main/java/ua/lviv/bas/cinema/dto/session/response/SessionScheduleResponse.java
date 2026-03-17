package ua.lviv.bas.cinema.dto.session.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Public schedule view of movie session")
public class SessionScheduleResponse {

	@Schema(description = "Unique identifier of the session", example = "1")
	private Long id;

	@Schema(description = "Start time of the session", example = "2024-01-15T18:30:00")
	private LocalDateTime startTime;

	@Schema(description = "End time of the session", example = "2024-01-15T21:00:00")
	private LocalDateTime endTime;

	@Schema(description = "Base price", example = "150.00")
	private BigDecimal basePrice;

	@Schema(description = "Available seats", example = "105")
	private Integer availableSeats;

	@Schema(description = "ID of the movie", example = "5")
	private Long movieId;

	@Schema(description = "Title of the movie", example = "Inception")
	private String movieTitle;

	@Schema(description = "Movie poster file name", example = "inception.jpg")
	private String moviePosterFileName;

	@Schema(description = "Age rating", example = "PG-13")
	private String movieAgeRating;

	@Schema(description = "Duration in minutes", example = "148")
	private Integer movieDuration;

	@Schema(description = "ID of the cinema hall", example = "3")
	private Long hallId;

	@Schema(description = "Name of the cinema hall", example = "Hall A")
	private String hallName;

	@Schema(description = "Hall capacity", example = "150")
	private Integer hallCapacity;
}