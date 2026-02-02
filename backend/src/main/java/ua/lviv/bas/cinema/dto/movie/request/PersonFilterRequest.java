package ua.lviv.bas.cinema.dto.movie.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for filtering and searching persons")
public class PersonFilterRequest {

	@Schema(description = "Search term for person name", example = "Leo")
	private String name;

	@Schema(description = "Filter by person role", example = "ACTOR")
	private PersonRole role;
}