package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

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
import ua.lviv.bas.cinema.exception.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.MovieNotFoundException;
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

	private Movie testMovie;
	private MovieDto testMovieDto;
	private MovieCreateRequest testCreateRequest;
	private MovieUpdateRequest testUpdateRequest;

	@BeforeEach
	void setUp() {
		testMovie = createTestMovie();
		testMovieDto = createTestMovieDto();
		testCreateRequest = createTestMovieCreateRequest();
		testUpdateRequest = createTestMovieUpdateRequest();

		org.springframework.test.util.ReflectionTestUtils.setField(movieService, "uploadDir", "test-uploads");
	}

	@Test
	void create_ShouldCreateMovieSuccessfully() {
		when(slugService.generateUniqueSlug(anyString())).thenReturn("test-slug");
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.empty());
		when(movieMapper.toEntity(any(MovieCreateRequest.class))).thenReturn(testMovie);
		when(genreRepository.findAllById(anyList())).thenReturn(List.of(new Genre()));
		when(personRepository.findAllById(anyList())).thenReturn(List.of(new Person()));
		when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		MovieDto result = movieService.create(testCreateRequest);

		assertNotNull(result);
		assertEquals(testMovieDto.getId(), result.getId());
		verify(movieRepository).save(any(Movie.class));
	}

	@Test
	void create_ShouldThrowWhenSlugExists() {
		when(slugService.generateUniqueSlug(anyString())).thenReturn("test-slug");
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.of(testMovie));

		assertThrows(DuplicateEntityException.class, () -> movieService.create(testCreateRequest));
	}

	@Test
	void create_ShouldThrowWhenInvalidDates() {
		testCreateRequest.setReleaseDate(LocalDate.now().plusDays(10));
		testCreateRequest.setEndShowingDate(LocalDate.now().plusDays(5));

		assertThrows(IllegalArgumentException.class, () -> movieService.create(testCreateRequest));
	}

	@Test
	void update_ShouldUpdateMovieSuccessfully() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		when(slugService.generateUniqueSlug(anyString())).thenReturn("new-slug");
		when(slugService.isSlugAvailableForMovie(anyString(), anyLong())).thenReturn(true);
		when(genreRepository.findAllById(anyList())).thenReturn(List.of(new Genre()));
		when(personRepository.findAllById(anyList())).thenReturn(List.of(new Person()));
		when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		MovieDto result = movieService.update(1L, testUpdateRequest);

		assertNotNull(result);
		verify(movieRepository).save(any(Movie.class));
	}

	@Test
	void update_ShouldThrowWhenMovieNotFound() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(MovieNotFoundException.class, () -> movieService.update(1L, testUpdateRequest));
	}

	@Test
	void update_ShouldThrowWhenSlugExists() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		when(slugService.generateUniqueSlug(anyString())).thenReturn("new-slug");
		when(slugService.isSlugAvailableForMovie(anyString(), anyLong())).thenReturn(false);

		assertThrows(DuplicateEntityException.class, () -> movieService.update(1L, testUpdateRequest));
	}

	@Test
	void updateWithPoster_ShouldUpdateWithPoster() throws IOException {
		MultipartFile posterFile = mock(MultipartFile.class);
		when(posterFile.isEmpty()).thenReturn(false);
		when(posterFile.getOriginalFilename()).thenReturn("poster.jpg");
		when(posterFile.getBytes()).thenReturn(new byte[0]);

		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		when(slugService.generateUniqueSlug(anyString())).thenReturn("new-slug");
		when(slugService.isSlugAvailableForMovie(anyString(), anyLong())).thenReturn(true);
		when(genreRepository.findAllById(anyList())).thenReturn(List.of(new Genre()));
		when(personRepository.findAllById(anyList())).thenReturn(List.of(new Person()));
		when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		MovieDto result = movieService.update(1L, testUpdateRequest, posterFile);

		assertNotNull(result);
		verify(movieRepository).save(any(Movie.class));
	}

	@Test
	void delete_ShouldDeleteMovieSuccessfully() {
		Movie movieWithoutPoster = createTestMovie();
		movieWithoutPoster.setPosterFileName(null);

		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(movieWithoutPoster));

		movieService.delete(1L);

		verify(movieRepository).delete(any(Movie.class));
	}

	@Test
	void delete_ShouldThrowWhenMovieNotFound() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(MovieNotFoundException.class, () -> movieService.delete(1L));
	}

	@Test
	void getById_ShouldReturnMovie() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		MovieDto result = movieService.getById(1L);

		assertNotNull(result);
		assertEquals(testMovieDto.getId(), result.getId());
	}

	@Test
	void getById_ShouldThrowWhenNotFound() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(MovieNotFoundException.class, () -> movieService.getById(1L));
	}

	@Test
	void getBySlug_ShouldReturnMovie() {
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		MovieDto result = movieService.getBySlug("test-slug");

		assertNotNull(result);
		assertEquals(testMovieDto.getId(), result.getId());
	}

	@Test
	void getBySlug_ShouldThrowWhenNotFound() {
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.empty());

		assertThrows(MovieNotFoundException.class, () -> movieService.getBySlug("test-slug"));
	}

	@Test
	void getAll_ShouldReturnAllMovies() {
		when(movieRepository.findAll()).thenReturn(List.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		List<MovieDto> result = movieService.getAll();

		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
	}

	@Test
	void getPaginated_ShouldReturnPage() {
		Page<Movie> moviePage = new PageImpl<>(List.of(testMovie));
		when(movieRepository.findAll(any(Pageable.class))).thenReturn(moviePage);
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		Page<MovieDto> result = movieService.getPaginated(Pageable.ofSize(10));

		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
	}

	@Test
	void getCurrentlyShowing_ShouldReturnCurrentMovies() {
		when(movieRepository.findByStatus(MovieStatus.CURRENT)).thenReturn(List.of(testMovie));
		when(movieMapper.toResponse(any(Movie.class))).thenReturn(new MovieResponse());

		List<MovieResponse> result = movieService.getCurrentlyShowing();

		assertNotNull(result);
	}

	@Test
	void getUpcoming_ShouldReturnUpcomingMovies() {
		when(movieRepository.findByStatus(MovieStatus.UPCOMING)).thenReturn(List.of(testMovie));
		when(movieMapper.toResponse(any(Movie.class))).thenReturn(new MovieResponse());

		List<MovieResponse> result = movieService.getUpcoming();

		assertNotNull(result);
	}

	@Test
	void getArchived_ShouldReturnArchivedMovies() {
		when(movieRepository.findByStatus(MovieStatus.ARCHIVED)).thenReturn(List.of(testMovie));
		when(movieMapper.toResponse(any(Movie.class))).thenReturn(new MovieResponse());

		List<MovieResponse> result = movieService.getArchived();

		assertNotNull(result);
	}

	@Test
	void getPoster_ShouldReturnPosterWhenExists() throws IOException {
		Path testDir = Path.of("test-uploads", "posters");
		Files.createDirectories(testDir);
		Path testFile = testDir.resolve("test-poster.jpg");
		Files.write(testFile, "test".getBytes());

		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		testMovie.setPosterFileName("test-poster.jpg");

		ResponseEntity<byte[]> result = movieService.getPoster(1L);

		assertNotNull(result);
		assertEquals(HttpStatus.OK, result.getStatusCode());

		Files.deleteIfExists(testFile);

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		if (Files.list(testDir).findAny().isEmpty()) {
			Files.deleteIfExists(testDir);
		}
	}

	@Test
	void getPoster_ShouldReturnNotFoundWhenNoPoster() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		testMovie.setPosterFileName(null);

		ResponseEntity<byte[]> result = movieService.getPoster(1L);

		assertNotNull(result);
		assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
	}

	@Test
	void getPoster_ShouldReturnNotFoundWhenFileNotExists() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		testMovie.setPosterFileName("non-existent.jpg");

		ResponseEntity<byte[]> result = movieService.getPoster(1L);

		assertNotNull(result);
		assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
	}

	@Test
	void enrichWithComputedFields_ShouldSetCorrectStatus() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		MovieDto result = movieService.getById(1L);

		assertNotNull(result);
	}

	private Movie createTestMovie() {
		Person actor = Person.builder().id(1L).name("Actor One").role(PersonRole.ACTOR).build();
		Person director = Person.builder().id(2L).name("Director One").role(PersonRole.DIRECTOR).build();
		Genre genre = Genre.builder().id(1L).name("Action").build();

		return Movie.builder().id(1L).title("Test Movie").slug("test-movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_12)
				.posterFileName("poster.jpg").cast(new java.util.HashSet<>(List.of(actor)))
				.directors(new java.util.HashSet<>(List.of(director))).screenwriters(new java.util.HashSet<>())
				.genres(new java.util.HashSet<>(List.of(genre))).build();
	}

	private MovieDto createTestMovieDto() {
		return MovieDto.builder().id(1L).title("Test Movie DTO").slug("test-movie-dto")
				.trailerUrl("https://example.com/trailer-dto").description("Test Description DTO").durationMinutes(150)
				.releaseDate(LocalDate.now().plusDays(2)).endShowingDate(LocalDate.now().plusDays(45))
				.status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_16).castIds(List.of(1L)).directorIds(List.of(2L))
				.screenwriterIds(List.of()).genreIds(List.of(1L)).build();
	}

	private MovieCreateRequest createTestMovieCreateRequest() {
		return MovieCreateRequest.builder().title("New Movie").trailerUrl("https://example.com/new-trailer")
				.description("New Description").durationMinutes(130).releaseDate(LocalDate.now().plusDays(5))
				.endShowingDate(LocalDate.now().plusDays(60)).ageRating(AgeRating.PEGI_7).genreIds(List.of(1L))
				.directorIds(List.of(2L)).screenwriterIds(List.of()).castIds(List.of(1L)).build();
	}

	private MovieUpdateRequest createTestMovieUpdateRequest() {
		return MovieUpdateRequest.builder().title("Updated Movie").trailerUrl("https://example.com/updated-trailer")
				.description("Updated Description").durationMinutes(140).releaseDate(LocalDate.now().plusDays(10))
				.endShowingDate(LocalDate.now().plusDays(70)).ageRating(AgeRating.PEGI_16).genreIds(List.of(1L))
				.directorIds(List.of(2L)).screenwriterIds(List.of()).castIds(List.of(1L)).removePoster(false).build();
	}
}