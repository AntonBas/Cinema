package ua.lviv.bas.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.PersonRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonDto {

	private Long id;

	private String name;

	private PersonRole role;
}
