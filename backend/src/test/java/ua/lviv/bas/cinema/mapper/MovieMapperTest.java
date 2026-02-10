package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;

public class MovieMapperTest {

	private MovieMapper mapper = Mappers.getMapper(MovieMapper.class);

	@Test
	void toMovieCardResponseFromProjection() {
		var projection = Mockito.mock(ua.lviv.bas.cinema.domain.projection.MovieCardProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getTitle()).thenReturn("Test Movie");
		Mockito.when(projection.getDurationMinutes()).thenReturn(120);

		MovieCardResponse response = mapper.toMovieCardResponse(projection);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("Test Movie");
		assertThat(response.getDurationMinutes()).isEqualTo(120);
	}

	@Test
	void toMovieDetailResponseFromProjection() {
		var projection = Mockito.mock(ua.lviv.bas.cinema.domain.projection.MovieDetailProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getTitle()).thenReturn("Movie Title");
		Mockito.when(projection.getDescription()).thenReturn("Description");

		var response = mapper.toMovieDetailResponse(projection);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("Movie Title");
		assertThat(response.getDescription()).isEqualTo("Description");
	}

	@Test
	void toMovieDetailResponseFromMovie() {
		Movie movie = Movie.builder().id(1L).title("Test Movie").description("Description").build();

		var response = mapper.toMovieDetailResponse(movie);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("Test Movie");
		assertThat(response.getDescription()).isEqualTo("Description");
	}

	@Test
	void toMovieSessionSearchResponse() {
		var projection = Mockito.mock(ua.lviv.bas.cinema.domain.projection.MovieSessionSearchProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getTitle()).thenReturn("Search Movie");
		Mockito.when(projection.getReleaseDate()).thenReturn(LocalDate.of(2024, 1, 15));

		var response = mapper.toMovieSessionSearchResponse(projection);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("Search Movie");
		assertThat(response.getReleaseYear()).isEqualTo(2024);
	}

	@Test
	void toMovieSessionSearchResponseWithNullDate() {
		var projection = Mockito.mock(ua.lviv.bas.cinema.domain.projection.MovieSessionSearchProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getTitle()).thenReturn("Movie");
		Mockito.when(projection.getReleaseDate()).thenReturn(null);

		var response = mapper.toMovieSessionSearchResponse(projection);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTitle()).isEqualTo("Movie");
		assertThat(response.getReleaseYear()).isNull();
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

		MovieUpdateRequest request = MovieUpdateRequest.builder().title(null).build();

		mapper.updateMovieFromRequest(request, movie);

		assertThat(movie.getTitle()).isEqualTo("Old Title");
		assertThat(movie.getDescription()).isEqualTo("Old Description");
	}

	@Test
	void toMovieCardResponseFromProjectionWithNull() {
		MovieCardResponse response = mapper
				.toMovieCardResponse((ua.lviv.bas.cinema.domain.projection.MovieCardProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void toMovieDetailResponseFromMovieWithNull() {
		var response = mapper.toMovieDetailResponse((Movie) null);
		assertThat(response).isNull();
	}

	@Test
	void toMovieSessionSearchResponseWithNull() {
		var response = mapper.toMovieSessionSearchResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toMovieWithNull() {
		Movie movie = mapper.toMovie(null);
		assertThat(movie).isNull();
	}
}