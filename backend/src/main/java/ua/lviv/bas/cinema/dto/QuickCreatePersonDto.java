package ua.lviv.bas.cinema.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

@Data
public class QuickCreatePersonDto {

	@NotNull(message = "Person name cannot be blank")
	private String name;

	@NotNull(message = "Person role is mandatory")
	private PersonRole role;
}
