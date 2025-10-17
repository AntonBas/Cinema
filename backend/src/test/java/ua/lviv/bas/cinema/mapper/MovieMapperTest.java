package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.MovieDto;
import ua.lviv.bas.cinema.dto.MovieResponse;
import ua.lviv.bas.cinema.dto.MovieUpdateRequest;

class MovieMapperTest {

	private MovieMapper movieMapper;

	@BeforeEach
	void setUp() {
		movieMapper = Mappers.getMapper(MovieMapper.class);
	}

	@Test
	void toDto_ShouldMapEntityToDtoCorrectly() {
		Movie movie = createTestMovie();

		MovieDto result = movieMapper.toDto(movie);

		assertNotNull(result);
		assertEquals(movie.getId(), result.getId());
		assertEquals(movie.getTitle(), result.getTitle());
		assertEquals(movie.getSlug(), result.getSlug());
		assertEquals(movie.getTrailerUrl(), result.getTrailerUrl());
		assertEquals(movie.getDescription(), result.getDescription());
		assertEquals(movie.getDurationMinutes(), result.getDurationMinutes());
		assertEquals(movie.getReleaseDate(), result.getReleaseDate());
		assertEquals(movie.getEndShowingDate(), result.getEndShowingDate());
		assertEquals(movie.getStatus(), result.getStatus());
		assertEquals(movie.getAgeRating(), result.getAgeRating());
		assertEquals(movie.getPosterFileName(), result.getPosterFileName());
	}

	@Test
	void toResponse_ShouldMapEntityToResponseCorrectly() {
		Movie movie = createTestMovie();

		MovieResponse result = movieMapper.toResponse(movie);

		assertNotNull(result);
		assertEquals(movie.getId(), result.getId());
		assertEquals(movie.getTitle(), result.getTitle());
		assertEquals(movie.getSlug(), result.getSlug());
		assertEquals(movie.getDurationMinutes(), result.getDurationMinutes());
		assertEquals(movie.getAgeRating(), result.getAgeRating());
		assertEquals(movie.getReleaseDate(), result.getReleaseDate());
		assertEquals(movie.getStatus(), result.getStatus());
	}

	@Test
	void toDtoList_ShouldMapListCorrectly() {
		List<Movie> movies = List.of(createTestMovie(), createTestMovie());

		List<MovieDto> result = movieMapper.toDtoList(movies);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(movies.get(0).getTitle(), result.get(0).getTitle());
		assertEquals(movies.get(1).getTitle(), result.get(1).getTitle());
	}

	@Test
	void toResponseList_ShouldMapListCorrectly() {
		List<Movie> movies = List.of(createTestMovie(), createTestMovie());

		List<MovieResponse> result = movieMapper.toResponseList(movies);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals(movies.get(0).getTitle(), result.get(0).getTitle());
		assertEquals(movies.get(1).getTitle(), result.get(1).getTitle());
	}

	@Test
	void toEntity_FromMovieCreateRequest_ShouldMapWithoutIgnoredFields() {
		MovieCreateRequest request = createTestMovieCreateRequest();

		Movie result = movieMapper.toEntity(request);

		assertNotNull(result);
		assertNull(result.getId());
		assertNull(result.getSlug());
		assertNull(result.getStatus());
		assertNull(result.getPosterFileName());

		assertEquals(request.getTitle(), result.getTitle());
		assertEquals(request.getTrailerUrl(), result.getTrailerUrl());
		assertEquals(request.getDescription(), result.getDescription());
		assertEquals(request.getDurationMinutes(), result.getDurationMinutes());
		assertEquals(request.getReleaseDate(), result.getReleaseDate());
		assertEquals(request.getEndShowingDate(), result.getEndShowingDate());
		assertEquals(request.getAgeRating(), result.getAgeRating());

		assertNotNull(result.getCast());
		assertTrue(result.getCast().isEmpty());
		assertNotNull(result.getDirectors());
		assertTrue(result.getDirectors().isEmpty());
		assertNotNull(result.getScreenwriters());
		assertTrue(result.getScreenwriters().isEmpty());
		assertNotNull(result.getGenres());
		assertTrue(result.getGenres().isEmpty());
		assertNotNull(result.getSessions());
		assertTrue(result.getSessions().isEmpty());
	}

	@Test
	void updateEntityFromRequest_ShouldUpdateOnlyAllowedFields() {
		Movie existingMovie = createTestMovie();
		MovieUpdateRequest request = createTestMovieUpdateRequest();

		movieMapper.updateEntityFromRequest(request, existingMovie);

		assertEquals(request.getTitle(), existingMovie.getTitle());
		assertEquals(request.getTrailerUrl(), existingMovie.getTrailerUrl());
		assertEquals(request.getDescription(), existingMovie.getDescription());
		assertEquals(request.getDurationMinutes(), existingMovie.getDurationMinutes());
		assertEquals(request.getReleaseDate(), existingMovie.getReleaseDate());
		assertEquals(request.getEndShowingDate(), existingMovie.getEndShowingDate());
		assertEquals(request.getAgeRating(), existingMovie.getAgeRating());

		assertNotNull(existingMovie.getId());
		assertNotNull(existingMovie.getSlug());
		assertNotNull(existingMovie.getStatus());
		assertNotNull(existingMovie.getPosterFileName());

		assertNotNull(existingMovie.getCast());
		assertFalse(existingMovie.getCast().isEmpty());
		assertNotNull(existingMovie.getDirectors());
		assertFalse(existingMovie.getDirectors().isEmpty());
		assertNotNull(existingMovie.getScreenwriters());
		assertFalse(existingMovie.getScreenwriters().isEmpty());
		assertNotNull(existingMovie.getGenres());
		assertFalse(existingMovie.getGenres().isEmpty());
	}

	@Test
	void toEntity_WithNullRequest_ShouldReturnNull() {
		assertNull(movieMapper.toEntity((MovieCreateRequest) null));
	}

	@Test
	void toDto_WithNullEntity_ShouldReturnNull() {
		assertNull(movieMapper.toDto((Movie) null));
	}

	@Test
	void toResponse_WithNullEntity_ShouldReturnNull() {
		assertNull(movieMapper.toResponse((Movie) null));
	}

	@Test
	void updateEntityFromRequest_WithNullRequest_ShouldDoNothing() {
		Movie existingMovie = createTestMovie();
		String originalTitle = existingMovie.getTitle();

		movieMapper.updateEntityFromRequest(null, existingMovie);

		assertEquals(originalTitle, existingMovie.getTitle());
	}

	private Movie createTestMovie() {
		Person actor1 = Person.builder().id(1L).name("Actor One").role(PersonRole.ACTOR).build();
		Person actor2 = Person.builder().id(2L).name("Actor Two").role(PersonRole.ACTOR).build();
		Person director = Person.builder().id(3L).name("Director One").role(PersonRole.DIRECTOR).build();
		Person screenwriter = Person.builder().id(4L).name("Writer One").role(PersonRole.SCREENWRITER).build();

		Genre action = Genre.builder().id(1L).name("Action").build();
		Genre drama = Genre.builder().id(2L).name("Drama").build();

		return Movie.builder().id(1L).title("Test Movie").slug("test-movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_12)
				.posterFileName("poster.jpg").cast(Set.of(actor1, actor2)).directors(Set.of(director))
				.screenwriters(Set.of(screenwriter)).genres(Set.of(action, drama)).build();
	}

	private MovieCreateRequest createTestMovieCreateRequest() {
		return MovieCreateRequest.builder().title("New Movie").trailerUrl("https://example.com/new-trailer")
				.description("New Description").durationMinutes(130).releaseDate(LocalDate.now().plusDays(5))
				.endShowingDate(LocalDate.now().plusDays(60)).ageRating(AgeRating.PEGI_7).genreIds(List.of(5L, 6L))
				.directorIds(List.of(22L)).screenwriterIds(List.of(23L)).castIds(List.of(20L, 21L)).build();
	}

	private MovieUpdateRequest createTestMovieUpdateRequest() {
		return MovieUpdateRequest.builder().title("Updated Movie").trailerUrl("https://example.com/updated-trailer")
				.description("Updated Description").durationMinutes(140).releaseDate(LocalDate.now().plusDays(10))
				.endShowingDate(LocalDate.now().plusDays(70)).ageRating(AgeRating.PEGI_16).genreIds(List.of(7L, 8L))
				.directorIds(List.of(24L)).screenwriterIds(List.of(25L)).castIds(List.of(26L, 27L)).removePoster(false)
				.build();
	}
}