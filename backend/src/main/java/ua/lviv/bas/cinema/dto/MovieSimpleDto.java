package ua.lviv.bas.cinema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MovieSimpleDto {

	private Long id;
	private String title;
	private Integer durationMinutes;
	private String posterFileName;
}
