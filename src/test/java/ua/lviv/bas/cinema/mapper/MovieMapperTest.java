package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.dto.MovieDto;

public class MovieMapperTest {

	private final MovieMapper mapper = Mappers.getMapper(MovieMapper.class);

	@Test
	void toDto_ShouldMapAllFields() {
		Movie movie = Movie.builder().id(1L).title("Test Movie").slug("test_movie").build();

		MovieDto dto = mapper.toDto(movie);

		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getTitle()).isEqualTo("Test Movie");
		assertThat(dto.getSlug()).isEqualTo("test_movie");
	}
}
