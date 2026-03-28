package ua.lviv.bas.cinema.domain.projection.cinema;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenreProjection {
	private Long id;
	private String name;
	private Integer movieCount;
}