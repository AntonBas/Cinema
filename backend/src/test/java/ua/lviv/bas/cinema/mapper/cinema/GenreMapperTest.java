package ua.lviv.bas.cinema.mapper.cinema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.cinema.Genre;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.GenreProjection;

public class GenreMapperTest {

	private final GenreMapper mapper = Mappers.getMapper(GenreMapper.class);

	@Test
	void toGenreResponseFromEntity_ShouldMapAllFields() {
		Genre genre = Genre.builder().id(1L).name("Action").movies(new HashSet<>()).build();

		GenreResponse response = mapper.toGenreResponse(genre);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Action");
		assertThat(response.movieCount()).isZero();
	}

	@Test
	void toGenreResponseFromEntity_WithMovies_ShouldCountMovies() {
		Genre genre = Genre.builder().id(1L).name("Action").movies(new HashSet<>()).build();

		genre.getMovies().add(Movie.builder().id(1L).build());
		genre.getMovies().add(Movie.builder().id(2L).build());

		GenreResponse response = mapper.toGenreResponse(genre);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Action");
		assertThat(response.movieCount()).isEqualTo(2);
	}

	@Test
	void toGenreResponseFromProjection_ShouldMapAllFields() {
		GenreProjection projection = new GenreProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getName() {
				return "Comedy";
			}

			@Override
			public Integer getMovieCount() {
				return 5;
			}
		};

		GenreResponse response = mapper.toGenreResponse(projection);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Comedy");
		assertThat(response.movieCount()).isEqualTo(5);
	}

	@Test
	void toGenreResponseFromProjection_WithNullMovieCount_ShouldMapNull() {
		GenreProjection projection = new GenreProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getName() {
				return "Comedy";
			}

			@Override
			public Integer getMovieCount() {
				return null;
			}
		};

		GenreResponse response = mapper.toGenreResponse(projection);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Comedy");
		assertThat(response.movieCount()).isNull();
	}

	@Test
	void toGenre_ShouldMapRequestToEntity() {
		GenreRequest request = new GenreRequest("Drama");
		Genre genre = mapper.toGenre(request);

		assertThat(genre).isNotNull();
		assertThat(genre.getId()).isNull();
		assertThat(genre.getName()).isEqualTo("Drama");
		assertThat(genre.getMovies()).isNotNull();
		assertThat(genre.getMovies()).isEmpty();
	}

	@Test
	void updateGenreFromRequest_ShouldUpdateOnlyNonNullFields() {
		Genre existing = Genre.builder().id(1L).name("Old").movies(new HashSet<>()).build();

		GenreRequest request = new GenreRequest("New");
		mapper.updateGenreFromRequest(request, existing);

		assertThat(existing.getId()).isEqualTo(1L);
		assertThat(existing.getName()).isEqualTo("New");
		assertThat(existing.getMovies()).isNotNull();
	}

	@Test
	void updateGenreFromRequest_WithSameName_ShouldNotChange() {
		Genre existing = Genre.builder().id(1L).name("Action").build();

		GenreRequest request = new GenreRequest("Action");
		mapper.updateGenreFromRequest(request, existing);

		assertThat(existing.getName()).isEqualTo("Action");
	}

	@Test
	void toGenreResponse_WithNullEntity_ShouldReturnNull() {
		GenreResponse response = mapper.toGenreResponse((Genre) null);
		assertThat(response).isNull();
	}

	@Test
	void toGenreResponse_WithNullProjection_ShouldReturnNull() {
		GenreResponse response = mapper.toGenreResponse((GenreProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void toGenre_WithNullRequest_ShouldReturnNull() {
		Genre genre = mapper.toGenre(null);
		assertThat(genre).isNull();
	}

	@Test
	void updateGenreFromRequest_WithNullRequest_ShouldNotChange() {
		Genre existing = Genre.builder().id(1L).name("Action").build();

		mapper.updateGenreFromRequest(null, existing);

		assertThat(existing.getName()).isEqualTo("Action");
	}
}