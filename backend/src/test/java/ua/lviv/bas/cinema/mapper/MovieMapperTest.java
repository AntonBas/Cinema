package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;

public class MovieMapperTest {

	private MovieMapper movieMapper;

	@BeforeEach
	void setUp() {
		movieMapper = Mappers.getMapper(MovieMapper.class);
	}

	@Test
	void toMovieCardResponse_ShouldMapResponseFields() {
		Movie movie = createTestMovie();

		MovieCardResponse result = movieMapper.toMovieCardResponse(movie);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getTitle()).isEqualTo("Test Movie");
		assertThat(result.getSlug()).isEqualTo("test-movie");
		assertThat(result.getPosterUrl()).isNull();
		assertThat(result.getDurationMinutes()).isEqualTo(120);
		assertThat(result.getAgeRating()).isEqualTo(AgeRating.PEGI_12);
		assertThat(result.getStatus()).isEqualTo(MovieStatus.UPCOMING);
	}

	@Test
	void toMovieCardResponse_ShouldCalculateCurrentlyShowing_WhenMovieIsCurrent() {
		Movie currentMovie = Movie.builder().id(5L).title("Current Movie").slug("current-movie")
				.releaseDate(LocalDate.now().minusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.status(MovieStatus.CURRENT).build();

		MovieCardResponse result = movieMapper.toMovieCardResponse(currentMovie);

		assertThat(result.getStatus()).isEqualTo(MovieStatus.CURRENT);
	}

	@Test
	void toMovieCardResponse_ShouldNotBeCurrentlyShowing_WhenMovieIsArchived() {
		Movie archivedMovie = Movie.builder().id(6L).title("Archived Movie").slug("archived-movie")
				.releaseDate(LocalDate.now().minusDays(10)).endShowingDate(LocalDate.now().minusDays(1))
				.status(MovieStatus.ARCHIVED).build();

		MovieCardResponse result = movieMapper.toMovieCardResponse(archivedMovie);

		assertThat(result.getStatus()).isEqualTo(MovieStatus.ARCHIVED);
	}

	@Test
	void toMovieCardResponse_ShouldHandleNullPosterFileName() {
		Movie movieWithoutPoster = Movie.builder().id(7L).title("No Poster").slug("no-poster").posterFileName(null)
				.build();

		MovieCardResponse result = movieMapper.toMovieCardResponse(movieWithoutPoster);

		assertThat(result).isNotNull();
		assertThat(result.getPosterUrl()).isNull();
	}

	@Test
	void toMovieCardResponse_ShouldHandleEmptyPosterFileName() {
		Movie movieWithEmptyPoster = Movie.builder().id(8L).title("Empty Poster").slug("empty-poster")
				.posterFileName("").build();

		MovieCardResponse result = movieMapper.toMovieCardResponse(movieWithEmptyPoster);

		assertThat(result).isNotNull();
		assertThat(result.getPosterUrl()).isNull();
	}

	@ParameterizedTest
	@EnumSource(AgeRating.class)
	void toMovieCardResponse_ShouldMapAllAgeRatings(AgeRating ageRating) {
		Movie movieWithRating = Movie.builder().id(9L).title("Rating Test").slug("rating-test").ageRating(ageRating)
				.build();

		MovieCardResponse result = movieMapper.toMovieCardResponse(movieWithRating);

		assertThat(result.getAgeRating()).isEqualTo(ageRating);
	}

	@ParameterizedTest
	@EnumSource(MovieStatus.class)
	void toMovieCardResponse_ShouldMapAllStatuses(MovieStatus status) {
		Movie movieWithStatus = Movie.builder().id(10L).title("Status Test").slug("status-test").status(status).build();

		MovieCardResponse result = movieMapper.toMovieCardResponse(movieWithStatus);

		assertThat(result.getStatus()).isEqualTo(status);
	}

	@Test
	void toMovieCardResponse_ShouldReturnNull_WhenInputIsNull() {
		MovieCardResponse result = movieMapper.toMovieCardResponse(null);

		assertThat(result).isNull();
	}

	@Test
	void toMovieCardResponse_ShouldMapMovieWithoutBuilder() {
		Movie movie = new Movie();
		movie.setId(11L);
		movie.setTitle("No Builder Movie");
		movie.setSlug("no-builder");
		movie.setPosterFileName("poster.jpg");

		MovieCardResponse result = movieMapper.toMovieCardResponse(movie);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(11L);
		assertThat(result.getTitle()).isEqualTo("No Builder Movie");
		assertThat(result.getSlug()).isEqualTo("no-builder");
		assertThat(result.getPosterUrl()).isNull();
	}

	@Test
	void toMovieCardResponse_ShouldHandleSpecialCharactersInTitle() {
		Movie movie = Movie.builder().id(12L).title("Movie: The Sequel & Remastered (2024)").slug("special-chars")
				.build();

		MovieCardResponse result = movieMapper.toMovieCardResponse(movie);

		assertThat(result.getTitle()).isEqualTo("Movie: The Sequel & Remastered (2024)");
	}

	@Test
	void toMovieCardResponseList_ShouldMapList() {
		Movie movie1 = createTestMovie();
		Movie movie2 = createAnotherMovie();

		List<Movie> movies = Arrays.asList(movie1, movie2);
		List<MovieCardResponse> result = movieMapper.toMovieCardResponseList(movies);

		assertThat(result).hasSize(2).extracting(MovieCardResponse::getTitle).containsExactly("Test Movie",
				"Another Movie");
	}

	@Test
	void toMovieCardResponseList_ShouldHandleEmptyList() {
		List<MovieCardResponse> result = movieMapper.toMovieCardResponseList(Collections.emptyList());

		assertThat(result).isEmpty();
	}

	@Test
	void toMovieCardResponseList_ShouldHandleNullList() {
		List<MovieCardResponse> result = movieMapper.toMovieCardResponseList(null);

		assertThat(result).isNull();
	}

	@Test
	void toMovieCardResponseList_ShouldHandleListWithNullElements() {
		Movie movie1 = createTestMovie();
		Movie movie2 = createAnotherMovie();

		List<Movie> movies = Arrays.asList(movie1, null, movie2);
		List<MovieCardResponse> result = movieMapper.toMovieCardResponseList(movies);

		assertThat(result).hasSize(3);
		assertThat(result.get(0)).isNotNull();
		assertThat(result.get(1)).isNull();
		assertThat(result.get(2)).isNotNull();
	}

	@Test
	void toMovie_ShouldMapFromCreateRequest() {
		MovieCreateRequest createRequest = MovieCreateRequest.builder().title("New Movie")
				.trailerUrl("https://example.com/trailer").description("Test Description").durationMinutes(120)
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.ageRating(AgeRating.PEGI_12).build();

		Movie result = movieMapper.toMovie(createRequest);

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
	void toMovie_ShouldHandleNullIdsInRequest() {
		MovieCreateRequest request = MovieCreateRequest.builder().title("Movie Without IDs").build();

		Movie result = movieMapper.toMovie(request);

		assertThat(result).isNotNull();
		assertThat(result.getTitle()).isEqualTo("Movie Without IDs");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "\t", "\n" })
	void toMovie_ShouldHandleEmptyOrBlankTitle(String title) {
		MovieCreateRequest request = MovieCreateRequest.builder().title(title).build();

		Movie result = movieMapper.toMovie(request);

		assertThat(result.getTitle()).isEqualTo(title);
	}

	@Test
	void toMovie_ShouldReturnNull_WhenRequestIsNull() {
		Movie result = movieMapper.toMovie(null);

		assertThat(result).isNull();
	}

	@Test
	void updateMovieFromRequest_ShouldUpdateFields() {
		Movie existingMovie = Movie.builder().id(1L).title("Old Title").slug("old-slug")
				.trailerUrl("https://old.com/trailer").description("Old Description").durationMinutes(100)
				.releaseDate(LocalDate.now().minusDays(1)).endShowingDate(LocalDate.now().plusDays(10))
				.status(MovieStatus.CURRENT).posterFileName("old-poster.jpg").ageRating(AgeRating.PEGI_7).build();

		MovieUpdateRequest updateRequest = MovieUpdateRequest.builder().title("Updated Movie")
				.trailerUrl("https://example.com/trailer/updated").description("Updated Description")
				.durationMinutes(130).releaseDate(LocalDate.now().plusDays(2))
				.endShowingDate(LocalDate.now().plusDays(35)).ageRating(AgeRating.PEGI_16).removePoster(false).build();

		movieMapper.updateMovieFromRequest(updateRequest, existingMovie);

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
	void updateMovieFromRequest_ShouldNotUpdateIgnoredFields() {
		Movie existingMovie = Movie.builder().id(999L).slug("existing-slug").status(MovieStatus.ARCHIVED)
				.posterFileName("existing-poster.jpg").build();

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("New Title").build();

		movieMapper.updateMovieFromRequest(request, existingMovie);

		assertThat(existingMovie.getId()).isEqualTo(999L);
		assertThat(existingMovie.getSlug()).isEqualTo("existing-slug");
		assertThat(existingMovie.getStatus()).isEqualTo(MovieStatus.ARCHIVED);
		assertThat(existingMovie.getPosterFileName()).isEqualTo("existing-poster.jpg");
	}

	@Test
	void updateMovieFromRequest_ShouldHandleNullRequest() {
		Movie existingMovie = Movie.builder().id(1L).title("Original").description("Original Description").build();

		movieMapper.updateMovieFromRequest(null, existingMovie);

		assertThat(existingMovie.getTitle()).isEqualTo("Original");
		assertThat(existingMovie.getDescription()).isEqualTo("Original Description");
	}

	@Test
	void updateMovieFromRequest_ShouldHandlePartialUpdate() {
		Movie existingMovie = Movie.builder().id(1L).title("Original Title").description(null).build();

		MovieUpdateRequest partialRequest = MovieUpdateRequest.builder().title("Updated Title").build();

		movieMapper.updateMovieFromRequest(partialRequest, existingMovie);

		assertThat(existingMovie.getTitle()).isEqualTo("Updated Title");
		assertThat(existingMovie.getDescription()).isNull();
	}

	@Test
	void updateMovieFromRequest_ShouldHandleEmptyStringFields() {
		Movie existingMovie = Movie.builder().id(1L).title("Original Title").description("Original Description")
				.build();

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("").description("").build();

		movieMapper.updateMovieFromRequest(request, existingMovie);

		assertThat(existingMovie.getTitle()).isEmpty();
		assertThat(existingMovie.getDescription()).isEmpty();
	}

	@Test
	void updateMovieFromRequest_ShouldThrowException_WhenTargetIsNull() {
		MovieUpdateRequest request = MovieUpdateRequest.builder().title("Test").build();

		assertThatThrownBy(() -> movieMapper.updateMovieFromRequest(request, null))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void updateMovieFromRequest_ShouldUpdateTrailerUrl_WhenProvided() {
		Movie existingMovie = Movie.builder().id(1L).trailerUrl("original-trailer").build();

		MovieUpdateRequest request = MovieUpdateRequest.builder().trailerUrl("updated-trailer").build();

		movieMapper.updateMovieFromRequest(request, existingMovie);

		assertThat(existingMovie.getTrailerUrl()).isEqualTo("updated-trailer");
	}

	private Movie createTestMovie() {
		return Movie.builder().id(1L).title("Test Movie").slug("test-movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(MovieStatus.UPCOMING).posterFileName("poster.jpg")
				.ageRating(AgeRating.PEGI_12).build();
	}

	private Movie createAnotherMovie() {
		return Movie.builder().id(2L).title("Another Movie").slug("another-movie")
				.trailerUrl("https://example.com/trailer2").description("Another Description").durationMinutes(90)
				.releaseDate(LocalDate.now().plusDays(5)).endShowingDate(LocalDate.now().plusDays(20))
				.status(MovieStatus.CURRENT).posterFileName("poster2.jpg").ageRating(AgeRating.PEGI_16).build();
	}
}