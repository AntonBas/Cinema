package ua.lviv.bas.cinema.dto;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MovieCreateRequest {

	@NotNull(message = "Movie data is required")
	@NotNull
	private MovieDto movie;

	private MultipartFile posterFile;
}
