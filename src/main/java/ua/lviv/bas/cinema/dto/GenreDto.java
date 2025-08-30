package ua.lviv.bas.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GenreDto {

	private Long id;

	@NotBlank(message = "Genre name is required")
	@Size(max = 30, min = 2, message = "Maximum 30 characters")
	private String name;

}