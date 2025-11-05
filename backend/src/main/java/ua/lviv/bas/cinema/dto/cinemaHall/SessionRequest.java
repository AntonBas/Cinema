package ua.lviv.bas.cinema.dto.cinemaHall;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequest {

	@FutureOrPresent
	@NotNull(message = "Start time is required")
	private LocalDateTime startTime;

	@Positive(message = "Price must be positive")
	@DecimalMin(value = "10.0", message = "Price must be at least 10 UAH")
	@NotNull(message = "Price is required")
	private BigDecimal price;

	@NotNull(message = "Movie ID is required")
	private Long movieId;

	@NotNull(message = "Hall ID is required")
	private Long hallId;

	public boolean isStartTimeValid() {
		return startTime == null || startTime.isAfter(LocalDateTime.now().plusMinutes(30));
	}
}
