package ua.lviv.bas.cinema.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.MovieDto;

class MovieMapperTest {

	private MovieMapper movieMapper;

	@BeforeEach
	void setUp() {
		movieMapper = new MovieMapperImpl();
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

		assertNotNull(result.getCastIds());
		assertEquals(2, result.getCastIds().size());
		assertTrue(result.getCastIds().containsAll(List.of(1L, 2L)));

		assertNotNull(result.getDirectorIds());
		assertEquals(1, result.getDirectorIds().size());
		assertTrue(result.getDirectorIds().contains(3L));

		assertNotNull(result.getScreenwriterIds());
		assertEquals(1, result.getScreenwriterIds().size());
		assertTrue(result.getScreenwriterIds().contains(4L));

		assertNotNull(result.getGenreIds());
		assertEquals(2, result.getGenreIds().size());
		assertTrue(result.getGenreIds().containsAll(List.of(1L, 2L)));
	}

	@Test
	void toEntity_FromMovieDto_ShouldMapCorrectly() {
		MovieDto movieDto = createTestMovieDto();

		Movie result = movieMapper.toEntity(movieDto);

		assertNotNull(result);
		assertEquals(movieDto.getId(), result.getId());
		assertEquals(movieDto.getTitle(), result.getTitle());
		assertEquals(movieDto.getSlug(), result.getSlug());
		assertEquals(movieDto.getTrailerUrl(), result.getTrailerUrl());
		assertEquals(movieDto.getDescription(), result.getDescription());
		assertEquals(movieDto.getDurationMinutes(), result.getDurationMinutes());
		assertEquals(movieDto.getReleaseDate(), result.getReleaseDate());
		assertEquals(movieDto.getEndShowingDate(), result.getEndShowingDate());
		assertEquals(movieDto.getStatus(), result.getStatus());
		assertEquals(movieDto.getAgeRating(), result.getAgeRating());

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

		assertNull(result.getPosterFileName());
	}

	@Test
	void toEntity_FromMovieCreateRequest_ShouldMapWithoutId() {
		MovieCreateRequest request = createTestMovieCreateRequest();

		Movie result = movieMapper.toEntity(request);

		assertNotNull(result);
		assertNull(result.getId());
		assertEquals(request.getTitle(), result.getTitle());
		assertEquals(request.getSlug(), result.getSlug());
		assertEquals(request.getTrailerUrl(), result.getTrailerUrl());
		assertEquals(request.getDescription(), result.getDescription());
		assertEquals(request.getDurationMinutes(), result.getDurationMinutes());
		assertEquals(request.getReleaseDate(), result.getReleaseDate());
		assertEquals(request.getEndShowingDate(), result.getEndShowingDate());
		assertEquals(request.getStatus(), result.getStatus());
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

		assertNull(result.getPosterFileName());
	}

	@Test
	void updateMovieFromDto_ShouldUpdateBasicFields() {
		Movie existingMovie = createTestMovie();
		MovieDto movieDto = createTestMovieDto();

		movieMapper.updateMovieFromDto(movieDto, existingMovie);

		assertEquals(movieDto.getTitle(), existingMovie.getTitle());
		assertEquals(movieDto.getSlug(), existingMovie.getSlug());
		assertEquals(movieDto.getDescription(), existingMovie.getDescription());
		assertEquals(movieDto.getTrailerUrl(), existingMovie.getTrailerUrl());
		assertEquals(movieDto.getDurationMinutes(), existingMovie.getDurationMinutes());
		assertEquals(movieDto.getReleaseDate(), existingMovie.getReleaseDate());
		assertEquals(movieDto.getEndShowingDate(), existingMovie.getEndShowingDate());
		assertEquals(movieDto.getStatus(), existingMovie.getStatus());
		assertEquals(movieDto.getAgeRating(), existingMovie.getAgeRating());

		assertNotEquals(movieDto.getId(), existingMovie.getId());

		assertNotNull(existingMovie.getCast());
		assertFalse(existingMovie.getCast().isEmpty());

		assertNotNull(existingMovie.getDirectors());
		assertFalse(existingMovie.getDirectors().isEmpty());

		assertNotNull(existingMovie.getScreenwriters());
		assertFalse(existingMovie.getScreenwriters().isEmpty());

		assertNotNull(existingMovie.getGenres());
		assertFalse(existingMovie.getGenres().isEmpty());
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

	private MovieDto createTestMovieDto() {
		return MovieDto.builder().id(2L).title("Test Movie DTO").slug("test-movie-dto")
				.trailerUrl("https://example.com/trailer-dto").description("Test Description DTO").durationMinutes(150)
				.releaseDate(LocalDate.now().plusDays(2)).endShowingDate(LocalDate.now().plusDays(45))
				.status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_16).castIds(List.of(10L, 11L))
				.directorIds(List.of(12L)).screenwriterIds(List.of(13L)).genreIds(List.of(3L, 4L)).build();
	}

	private MovieCreateRequest createTestMovieCreateRequest() {
		MovieCreateRequest request = new MovieCreateRequest();
		request.setTitle("New Movie");
		request.setSlug("new-movie");
		request.setTrailerUrl("https://example.com/new-trailer");
		request.setDescription("New Description");
		request.setDurationMinutes(130);
		request.setReleaseDate(LocalDate.now().plusDays(5));
		request.setEndShowingDate(LocalDate.now().plusDays(60));
		request.setStatus(MovieStatus.UPCOMING);
		request.setAgeRating(AgeRating.PEGI_7);
		request.setCastIds(List.of(20L, 21L));
		request.setDirectorIds(List.of(22L));
		request.setScreenwriterIds(List.of(23L));
		request.setGenreIds(List.of(5L, 6L));
		return request;
	}
}