package ua.lviv.bas.cinema.dto.cinemaHall;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaHallRequest {

	@NotBlank(message = "Hall name is required")
	@Size(min = 2, max = 25, message = "Name must be between 2-25 characters")
	private String name;
}
