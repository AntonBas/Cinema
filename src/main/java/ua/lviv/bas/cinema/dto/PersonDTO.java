package ua.lviv.bas.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

@Data
@Builder
public class PersonDto {

	private Long id;

	@NotBlank(message = "Person name cannot be blank")
	private String name;

	@NotNull(message = "Person role is mandatory")
	private PersonRole role;
}
