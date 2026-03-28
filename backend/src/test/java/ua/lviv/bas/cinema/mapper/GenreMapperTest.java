package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.projection.cinema.GenreProjection;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;

public class GenreMapperTest {

	private GenreMapper mapper = Mappers.getMapper(GenreMapper.class);

	@Test
	void toGenreResponseFromEntity() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreResponse response = mapper.toGenreResponse(genre);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Action");
		assertThat(response.movieCount()).isEqualTo(0);
	}

	@Test
	void toGenreResponseFromProjection() {
		GenreProjection projection = new GenreProjection(1L, "Comedy", 5);
		GenreResponse response = mapper.toGenreResponse(projection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Comedy");
		assertThat(response.movieCount()).isEqualTo(5);
	}

	@Test
	void toGenreResponseList() {
		List<Genre> genres = List.of(Genre.builder().id(1L).name("Action").build(),
				Genre.builder().id(2L).name("Comedy").build());

		List<GenreResponse> responses = mapper.toGenreResponseList(genres);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).name()).isEqualTo("Action");
		assertThat(responses.get(1).name()).isEqualTo("Comedy");
	}

	@Test
	void toGenre() {
		GenreRequest request = new GenreRequest("Drama");
		Genre genre = mapper.toGenre(request);

		assertThat(genre.getName()).isEqualTo("Drama");
	}

	@Test
	void updateGenreFromRequest() {
		Genre existing = Genre.builder().id(1L).name("Old").build();
		GenreRequest request = new GenreRequest("New");

		mapper.updateGenreFromRequest(request, existing);

		assertThat(existing.getName()).isEqualTo("New");
	}

	@Test
	void nullHandling() {
		assertThat(mapper.toGenreResponse((Genre) null)).isNull();
		assertThat(mapper.toGenreResponse((GenreProjection) null)).isNull();
		assertThat(mapper.toGenre(null)).isNull();
		assertThat(mapper.toGenreResponseList(null)).isNull();
	}
}