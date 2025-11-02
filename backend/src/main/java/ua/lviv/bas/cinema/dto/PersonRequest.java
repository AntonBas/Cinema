package ua.lviv.bas.cinema.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonRequest {

	@NotBlank(message = "Person name is required")
	private String name;

	@NotNull(message = "Person role is required")
	private PersonRole role;
}