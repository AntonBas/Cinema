package ua.lviv.bas.cinema.dto.movie;

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
public class GenreRequest {

	@NotBlank(message = "Genre name is required")
	@Size(max = 30, min = 2, message = "Name must be between 2 and 30 characters")
	private String name;

}
