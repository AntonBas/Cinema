package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import ua.lviv.bas.cinema.mapper.MovieMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;

@ExtendWith(MockitoExtension.class)
class MovieServiceTest {

	@Mock
	private MovieRepository movieRepository;
	@Mock
	private GenreRepository genreRepository;
	@Mock
	private PersonRepository personRepository;
	@Mock
	private MovieMapper movieMapper;
	@Mock
	private SlugService slugService;
	@InjectMocks
	private MovieService movieService;

	private Movie movie;
	private MovieDto movieDto;
	private MovieResponse movieResponse;
	private MovieCreateRequest createRequest;
	private MovieUpdateRequest updateRequest;
	private Genre genre;
	private Person actor;
	private Person director;

	@BeforeEach
	void setUp() {
		genre = Genre.builder().id(1L).name("Action").build();
		actor = Person.builder().id(1L).name("Actor One").role(PersonRole.ACTOR).build();
		director = Person.builder().id(2L).name("Director One").role(PersonRole.DIRECTOR).build();

		movie = Movie.builder().id(1L).title("Test Movie").slug("test-movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_12)
				.posterFileName("poster.jpg").cast(new HashSet<>(List.of(actor)))
				.directors(new HashSet<>(List.of(director))).screenwriters(new HashSet<>())
				.genres(new HashSet<>(List.of(genre))).build();

		movieDto = MovieDto.builder().id(1L).title("Test Movie").slug("test-movie")
				.trailerUrl("https://example.com/trailer").description("Test Description").durationMinutes(120)
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_12).posterFileName("poster.jpg")
				.castIds(List.of(1L)).directorIds(List.of(2L)).screenwriterIds(List.of()).genreIds(List.of(1L)).build();

		movieResponse = MovieResponse.builder().id(1L).title("Test Movie").slug("test-movie").durationMinutes(120)
				.ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1)).status(MovieStatus.UPCOMING)
				.currentlyShowing(false).build();

		createRequest = MovieCreateRequest.builder().title("New Movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).ageRating(AgeRating.PEGI_12).genreIds(List.of(1L))
				.castIds(List.of(1L)).directorIds(List.of(2L)).screenwriterIds(List.of()).build();

		updateRequest = MovieUpdateRequest.builder().title("Updated Movie")
				.trailerUrl("https://example.com/updated-trailer").description("Updated Description")
				.durationMinutes(140).releaseDate(LocalDate.now().plusDays(10))
				.endShowingDate(LocalDate.now().plusDays(70)).ageRating(AgeRating.PEGI_16).genreIds(List.of(1L))
				.castIds(List.of(1L)).directorIds(List.of(2L)).screenwriterIds(List.of()).removePoster(false).build();

		org.springframework.test.util.ReflectionTestUtils.setField(movieService, "uploadDir", "test-uploads");
	}

	@Test
	void create_ShouldCreateMovieSuccessfully() {
		when(slugService.generateUniqueSlug("New Movie")).thenReturn("new-movie");
		when(movieRepository.findBySlug("new-movie")).thenReturn(Optional.empty());
		when(movieMapper.toEntity(createRequest)).thenReturn(movie);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(director));
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toDto(movie)).thenReturn(movieDto);

		MovieDto result = movieService.create(createRequest);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Test Movie", result.getTitle());
		verify(movieRepository).save(movie);
	}

	@Test
	void getById_ShouldReturnMovie() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(movieMapper.toDto(movie)).thenReturn(movieDto);

		MovieDto result = movieService.getById(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Test Movie", result.getTitle());
	}

	@Test
	void getBySlug_ShouldReturnMovie() {
		when(movieRepository.findBySlug("test-movie")).thenReturn(Optional.of(movie));
		when(movieMapper.toDto(movie)).thenReturn(movieDto);

		MovieDto result = movieService.getBySlug("test-movie");

		assertNotNull(result);
		assertEquals("test-movie", result.getSlug());
	}

	@Test
	void getAll_ShouldReturnAllMovies() {
		when(movieRepository.findAll()).thenReturn(List.of(movie));
		when(movieMapper.toDto(movie)).thenReturn(movieDto);

		List<MovieDto> result = movieService.getAll();

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("Test Movie", result.get(0).getTitle());
	}

	@Test
	void getCurrentlyShowing_ShouldReturnCurrentMovies() {
		movie.setStatus(MovieStatus.CURRENT);
		when(movieRepository.findByStatus(MovieStatus.CURRENT)).thenReturn(List.of(movie));
		when(movieMapper.toResponse(movie)).thenReturn(movieResponse);

		List<MovieResponse> result = movieService.getCurrentlyShowing();

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getUpcoming_ShouldReturnUpcomingMovies() {
		when(movieRepository.findByStatus(MovieStatus.UPCOMING)).thenReturn(List.of(movie));
		when(movieMapper.toResponse(movie)).thenReturn(movieResponse);

		List<MovieResponse> result = movieService.getUpcoming();

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void getArchived_ShouldReturnArchivedMovies() {
		movie.setStatus(MovieStatus.ARCHIVED);
		when(movieRepository.findByStatus(MovieStatus.ARCHIVED)).thenReturn(List.of(movie));
		when(movieMapper.toResponse(movie)).thenReturn(movieResponse);

		List<MovieResponse> result = movieService.getArchived();

		assertNotNull(result);
		assertEquals(1, result.size());
	}

	@Test
	void delete_ShouldDeleteMovieSuccessfully() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

		movieService.delete(1L);

		verify(movieRepository).delete(movie);
	}

	@Test
	void update_ShouldUpdateMovieSuccessfully() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("Updated Movie")).thenReturn("updated-movie");
		when(slugService.isSlugAvailableForMovie("updated-movie", 1L)).thenReturn(true);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(director));
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toDto(movie)).thenReturn(movieDto);

		MovieDto result = movieService.update(1L, updateRequest);

		assertNotNull(result);
		verify(movieRepository).save(movie);
	}
}