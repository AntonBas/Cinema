package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.MovieDto;
import ua.lviv.bas.cinema.dto.MovieResponse;
import ua.lviv.bas.cinema.dto.MovieUpdateRequest;

class MovieMapperTest {

	private MovieMapper movieMapper;

	private Movie movie;
	private MovieCreateRequest createRequest;
	private MovieUpdateRequest updateRequest;

	@BeforeEach
	void setUp() {
		movieMapper = Mappers.getMapper(MovieMapper.class);

		movie = Movie.builder().id(1L).title("Test Movie").slug("test-movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(MovieStatus.UPCOMING).posterFileName("poster.jpg")
				.ageRating(AgeRating.PEGI_12).build();

		createRequest = MovieCreateRequest.builder().title("New Movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).ageRating(AgeRating.PEGI_12).genreIds(List.of(1L, 2L))
				.actorIds(List.of(1L, 2L)).directorIds(List.of(3L)).screenwriterIds(List.of(4L)).build();

		updateRequest = MovieUpdateRequest.builder().title("Updated Movie")
				.trailerUrl("https://example.com/trailer/updated").description("Updated Description")
				.durationMinutes(130).releaseDate(LocalDate.now().plusDays(2))
				.endShowingDate(LocalDate.now().plusDays(35)).ageRating(AgeRating.PEGI_16).genreIds(List.of(1L, 2L))
				.actorIds(List.of(1L, 2L)).directorIds(List.of(3L)).screenwriterIds(List.of(4L)).removePoster(false)
				.build();
	}

	@Test
	void toDto_ShouldMapAllFields() {
		MovieDto result = movieMapper.toDto(movie);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getTitle()).isEqualTo("Test Movie");
		assertThat(result.getSlug()).isEqualTo("test-movie");
		assertThat(result.getTrailerUrl()).isEqualTo("https://example.com/trailer");
		assertThat(result.getDescription()).isEqualTo("Test Description");
		assertThat(result.getDurationMinutes()).isEqualTo(120);
		assertThat(result.getReleaseDate()).isEqualTo(movie.getReleaseDate());
		assertThat(result.getEndShowingDate()).isEqualTo(movie.getEndShowingDate());
		assertThat(result.getStatus()).isEqualTo(MovieStatus.UPCOMING);
		assertThat(result.getAgeRating()).isEqualTo(AgeRating.PEGI_12);
		assertThat(result.getPosterFileName()).isEqualTo("poster.jpg");
	}

	@Test
	void toDtoList_ShouldMapList() {
		List<Movie> movies = List.of(movie);
		List<MovieDto> result = movieMapper.toDtoList(movies);
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
	}

	@Test
	void toResponse_ShouldMapResponseFields() {
		MovieResponse result = movieMapper.toResponse(movie);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getTitle()).isEqualTo("Test Movie");
		assertThat(result.getSlug()).isEqualTo("test-movie");
		assertThat(result.getDurationMinutes()).isEqualTo(120);
		assertThat(result.getAgeRating()).isEqualTo(AgeRating.PEGI_12);
		assertThat(result.getReleaseDate()).isEqualTo(movie.getReleaseDate());
		assertThat(result.getStatus()).isEqualTo(MovieStatus.UPCOMING);
	}

	@Test
	void toResponseList_ShouldMapList() {
		List<Movie> movies = List.of(movie);
		List<MovieResponse> result = movieMapper.toResponseList(movies);
		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTitle()).isEqualTo("Test Movie");
	}

	@Test
	void toEntity_ShouldMapFromCreateRequest() {
		Movie result = movieMapper.toEntity(createRequest);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isNull();
		assertThat(result.getTitle()).isEqualTo("New Movie");
		assertThat(result.getTrailerUrl()).isEqualTo("https://example.com/trailer");
		assertThat(result.getDescription()).isEqualTo("Test Description");
		assertThat(result.getDurationMinutes()).isEqualTo(120);
		assertThat(result.getReleaseDate()).isEqualTo(createRequest.getReleaseDate());
		assertThat(result.getEndShowingDate()).isEqualTo(createRequest.getEndShowingDate());
		assertThat(result.getAgeRating()).isEqualTo(AgeRating.PEGI_12);
		assertThat(result.getSlug()).isNull();
		assertThat(result.getStatus()).isNull();
		assertThat(result.getPosterFileName()).isNull();
	}

	@Test
	void updateEntityFromRequest_ShouldUpdateFields() {
		Movie existingMovie = Movie.builder().id(1L).title("Old Title").slug("old-slug")
				.trailerUrl("https://old.com/trailer").description("Old Description").durationMinutes(100)
				.releaseDate(LocalDate.now().minusDays(1)).endShowingDate(LocalDate.now().plusDays(10))
				.status(MovieStatus.CURRENT).posterFileName("old-poster.jpg").ageRating(AgeRating.PEGI_7).build();

		movieMapper.updateEntityFromRequest(updateRequest, existingMovie);

		assertThat(existingMovie.getTitle()).isEqualTo("Updated Movie");
		assertThat(existingMovie.getTrailerUrl()).isEqualTo("https://example.com/trailer/updated");
		assertThat(existingMovie.getDescription()).isEqualTo("Updated Description");
		assertThat(existingMovie.getDurationMinutes()).isEqualTo(130);
		assertThat(existingMovie.getReleaseDate()).isEqualTo(updateRequest.getReleaseDate());
		assertThat(existingMovie.getEndShowingDate()).isEqualTo(updateRequest.getEndShowingDate());
		assertThat(existingMovie.getAgeRating()).isEqualTo(AgeRating.PEGI_16);
		assertThat(existingMovie.getId()).isEqualTo(1L);
		assertThat(existingMovie.getSlug()).isEqualTo("old-slug");
		assertThat(existingMovie.getStatus()).isEqualTo(MovieStatus.CURRENT);
		assertThat(existingMovie.getPosterFileName()).isEqualTo("old-poster.jpg");
	}

	@Test
	void toEntity_WithNullRequest_ShouldReturnNull() {
		Movie result = movieMapper.toEntity(null);
		assertThat(result).isNull();
	}

	@Test
	void toDto_WithNullMovie_ShouldReturnNull() {
		MovieDto result = movieMapper.toDto(null);
		assertThat(result).isNull();
	}

	@Test
	void toResponse_WithNullMovie_ShouldReturnNull() {
		MovieResponse result = movieMapper.toResponse(null);
		assertThat(result).isNull();
	}
}