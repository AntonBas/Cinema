package ua.lviv.bas.cinema.dto.cinemaHall.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.validation.CoupleRowSeatsConstraint;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for creating or updating a cinema hall")
public class CinemaHallRequest {

	@Schema(description = "Name of the cinema hall", example = "Hall A - Dolby Atmos", requiredMode = Schema.RequiredMode.REQUIRED, minLength = 2, maxLength = 25)
	@NotBlank(message = "Hall name is required")
	@Size(min = 2, max = 25, message = "Name must be between 2-25 characters")
	private String name;

	@Schema(description = "Number of rows in the cinema hall", example = "10", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "20")
	@NotNull(message = "Number of rows is required")
	@Min(value = 1, message = "Minimum 1 row")
	@Max(value = 20, message = "Maximum 20 rows")
	private Integer rows;

	@Schema(description = "Number of seats per row", example = "10", requiredMode = Schema.RequiredMode.REQUIRED, minimum = "1", maximum = "20")
	@NotNull(message = "Seats per row is required")
	@Min(value = 1, message = "Minimum 1 seat in a row")
	@Max(value = 20, message = "Maximum 20 seats in a row")
	@CoupleRowSeatsConstraint
	private Integer seatsPerRow;

	@Schema(description = "Default seat type for the hall", example = "STANDARD", defaultValue = "STANDARD")
	@Builder.Default
	private SeatType defaultSeatType = SeatType.STANDARD;

	@Schema(description = "List of rows that should have COUPLE seats", example = "[4, 7]")
	private List<Integer> coupleRows;
}