package ua.lviv.bas.cinema.repository.cinema.projection;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenreProjection {
	private Long id;
	private String name;
	private Integer movieCount;
}