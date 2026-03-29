package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;

public class MovieMapperTest {

	private MovieMapper mapper = Mappers.getMapper(MovieMapper.class);

	@Test
	void toMovieCardResponseFromMovie() {
		Movie movie = Movie.builder().id(1L).title("Test Movie").durationMinutes(120).build();

		MovieCardResponse response = mapper.toMovieCardResponse(movie);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Test Movie");
		assertThat(response.durationMinutes()).isEqualTo(120);
		assertThat(response.posterUrl()).isEqualTo("/api/movies/1/poster");
	}

	@Test
	void toMovieDetailResponseFromMovie() {
		Movie movie = Movie.builder().id(1L).title("Test Movie").description("Description").build();

		MovieDetailResponse response = mapper.toMovieDetailResponse(movie);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Test Movie");
		assertThat(response.description()).isEqualTo("Description");
		assertThat(response.posterUrl()).isEqualTo("/api/movies/1/poster");
	}

	@Test
	void toMovieSessionSearchResponse() {
		MovieSessionSearchResponse response = mapper.toMovieSessionSearchResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toMovie() {
		MovieCreateRequest request = MovieCreateRequest.builder().title("New Movie").description("Description")
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).build();

		Movie movie = mapper.toMovie(request);

		assertThat(movie.getTitle()).isEqualTo("New Movie");
		assertThat(movie.getDescription()).isEqualTo("Description");
		assertThat(movie.getDurationMinutes()).isEqualTo(120);
		assertThat(movie.getAgeRating()).isEqualTo(AgeRating.PEGI_12);
	}

	@Test
	void updateMovieFromRequest() {
		Movie movie = Movie.builder().id(1L).title("Old Title").description("Old Description").build();

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("New Title").description("New Description")
				.build();

		mapper.updateMovieFromRequest(request, movie);

		assertThat(movie.getTitle()).isEqualTo("New Title");
		assertThat(movie.getDescription()).isEqualTo("New Description");
	}

	@Test
	void updateMovieFromRequestWithNullFields() {
		Movie movie = Movie.builder().id(1L).title("Old Title").description("Old Description").build();

		MovieUpdateRequest request = MovieUpdateRequest.builder().title(null).description(null).build();

		mapper.updateMovieFromRequest(request, movie);

		assertThat(movie.getTitle()).isEqualTo("Old Title");
		assertThat(movie.getDescription()).isEqualTo("Old Description");
	}

	@Test
	void nullHandling() {
		assertThat(mapper.toMovieCardResponse((Movie) null)).isNull();
		assertThat(mapper.toMovieDetailResponse((Movie) null)).isNull();
		assertThat(mapper.toMovie(null)).isNull();
	}
}