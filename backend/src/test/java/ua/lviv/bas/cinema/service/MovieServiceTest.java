package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import ua.lviv.bas.cinema.dao.GenreRepository;
import ua.lviv.bas.cinema.dao.MovieRepository;
import ua.lviv.bas.cinema.dao.PersonRepository;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.MovieDto;
import ua.lviv.bas.cinema.mapper.MovieMapper;

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

	@InjectMocks
	private MovieService movieService;

	private Movie testMovie;
	private MovieDto testMovieDto;
	private MovieCreateRequest testCreateRequest;

	@BeforeEach
	void setUp() {
		testMovie = createTestMovie();
		testMovieDto = createTestMovieDto();
		testCreateRequest = createTestMovieCreateRequest();
	}

	@Test
	void createMovie_ShouldCreateMovieSuccessfully() {
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.empty());
		when(movieMapper.toEntity(any(MovieCreateRequest.class))).thenReturn(testMovie);
		when(genreRepository.findAllById(anyList())).thenReturn(List.of(new Genre()));
		when(personRepository.findAllById(anyList())).thenReturn(List.of(new Person()));
		when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		MovieDto result = movieService.createMovie(testCreateRequest);

		assertNotNull(result);
		assertEquals(testMovieDto.getId(), result.getId());
		verify(movieRepository).save(any(Movie.class));
	}

	@Test
	void createMovie_ShouldThrowWhenSlugExists() {
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.of(testMovie));

		assertThrows(RuntimeException.class, () -> movieService.createMovie(testCreateRequest));
	}

	@Test
	void createMovie_ShouldThrowWhenInvalidDates() {
		testCreateRequest.setReleaseDate(LocalDate.now().plusDays(10));
		testCreateRequest.setEndShowingDate(LocalDate.now().plusDays(5));

		assertThrows(RuntimeException.class, () -> movieService.createMovie(testCreateRequest));
	}

	@Test
	void updateMovie_ShouldUpdateMovieSuccessfully() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.empty());
		when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		MovieDto result = movieService.updateMovie(1L, testMovieDto);

		assertNotNull(result);
		verify(movieMapper).updateMovieFromDto(any(MovieDto.class), any(Movie.class));
		verify(movieRepository).save(any(Movie.class));
	}

	@Test
	void updateMovie_ShouldThrowWhenMovieNotFound() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> movieService.updateMovie(1L, testMovieDto));
	}

	@Test
	void updateMovie_ShouldThrowWhenSlugExists() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.of(testMovie));

		testMovieDto.setSlug("different-slug");

		assertThrows(RuntimeException.class, () -> movieService.updateMovie(1L, testMovieDto));
	}

	@Test
	void updateMovieWithPoster_ShouldUpdateWithPoster() throws IOException {
		MultipartFile posterFile = mock(MultipartFile.class);
		when(posterFile.isEmpty()).thenReturn(false);
		when(posterFile.getOriginalFilename()).thenReturn("poster.jpg");
		when(posterFile.getBytes()).thenReturn(new byte[0]);

		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.empty());
		when(movieRepository.save(any(Movie.class))).thenReturn(testMovie);
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		org.springframework.test.util.ReflectionTestUtils.setField(movieService, "uploadDir", "test-uploads");

		MovieDto result = movieService.updateMovieWithPoster(1L, testMovieDto, posterFile);

		assertNotNull(result);
		verify(movieRepository).save(any(Movie.class));

		try {
			Files.deleteIfExists(Paths.get("test-uploads", "posters"));
		} catch (IOException e) {
		}
	}

	@Test
	void deleteMovie_ShouldDeleteMovieSuccessfully() {
		Movie movie = createTestMovie();
		movie.setPosterFileName(null);

		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(movie));

		movieService.deleteMovie(1L);

		verify(movieRepository).deleteById(1L);
	}

	@Test
	void deleteMovie_ShouldThrowWhenMovieNotFound() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> movieService.deleteMovie(1L));
	}

	@Test
	void getMovieById_ShouldReturnMovie() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		MovieDto result = movieService.getMovieById(1L);

		assertNotNull(result);
		assertEquals(testMovieDto.getId(), result.getId());
	}

	@Test
	void getMovieById_ShouldThrowWhenNotFound() {
		when(movieRepository.findById(anyLong())).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> movieService.getMovieById(1L));
	}

	@Test
	void getMovieBySlug_ShouldReturnMovie() {
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		MovieDto result = movieService.getMovieBySlug("test-slug");

		assertNotNull(result);
		assertEquals(testMovieDto.getId(), result.getId());
	}

	@Test
	void getMovieBySlug_ShouldThrowWhenNotFound() {
		when(movieRepository.findBySlug(anyString())).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> movieService.getMovieBySlug("test-slug"));
	}

	@Test
	void getAllMovies_ShouldReturnAllMovies() {
		when(movieRepository.findAll()).thenReturn(List.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		List<MovieDto> result = movieService.getAllMovies();

		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
	}

	@Test
	void getPaginatedMovies_ShouldReturnPage() {
		Page<Movie> moviePage = new PageImpl<>(List.of(testMovie));
		when(movieRepository.findAll(any(Pageable.class))).thenReturn(moviePage);
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		Page<MovieDto> result = movieService.getPaginatedMovies(Pageable.ofSize(10));

		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
	}

	@Test
	void getMoviesByStatus_ShouldReturnMoviesByStatus() {
		when(movieRepository.findByStatus(any(MovieStatus.class))).thenReturn(List.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		List<MovieDto> result = movieService.getMoviesByStatus("UPCOMING");

		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	void getMoviesByStatus_ShouldThrowWhenInvalidStatus() {
		assertThrows(RuntimeException.class, () -> movieService.getMoviesByStatus("INVALID_STATUS"));
	}

	@Test
	void getCurrentlyShowingMovies_ShouldReturnCurrentMovies() {
		when(movieRepository.findByReleaseDateBeforeAndEndShowingDateAfter(any(LocalDate.class), any(LocalDate.class)))
				.thenReturn(List.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		List<MovieDto> result = movieService.getCurrentlyShowingMovies();

		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	void getUpcomingMovies_ShouldReturnUpcomingMovies() {
		when(movieRepository.findByReleaseDateAfter(any(LocalDate.class))).thenReturn(List.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		List<MovieDto> result = movieService.getUpcomingMovies();

		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	void getMoviesByGenre_ShouldReturnMoviesByGenre() {
		when(genreRepository.existsById(anyLong())).thenReturn(true);
		when(movieRepository.findByGenresContaining(anyLong())).thenReturn(List.of(testMovie));
		when(movieMapper.toDto(any(Movie.class))).thenReturn(testMovieDto);

		List<MovieDto> result = movieService.getMoviesByGenre(1L);

		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	void getMoviesByGenre_ShouldThrowWhenGenreNotFound() {
		when(genreRepository.existsById(anyLong())).thenReturn(false);

		assertThrows(RuntimeException.class, () -> movieService.getMoviesByGenre(1L));
	}

	private Movie createTestMovie() {
		Person actor = Person.builder().id(1L).name("Actor One").role(PersonRole.ACTOR).build();
		Person director = Person.builder().id(2L).name("Director One").role(PersonRole.DIRECTOR).build();
		Genre genre = Genre.builder().id(1L).name("Action").build();

		// Використовуємо new HashSet<>() замість Set.of() для mutable колекцій
		Set<Person> cast = new HashSet<>();
		cast.add(actor);

		Set<Person> directors = new HashSet<>();
		directors.add(director);

		Set<Genre> genres = new HashSet<>();
		genres.add(genre);

		return Movie.builder().id(1L).title("Test Movie").slug("test-movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_12)
				.posterFileName("poster.jpg").cast(cast).directors(directors).screenwriters(new HashSet<>())
				.genres(genres).build();
	}

	private MovieDto createTestMovieDto() {
		return MovieDto.builder().id(1L).title("Test Movie DTO").slug("test-movie-dto")
				.trailerUrl("https://example.com/trailer-dto").description("Test Description DTO").durationMinutes(150)
				.releaseDate(LocalDate.now().plusDays(2)).endShowingDate(LocalDate.now().plusDays(45))
				.status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_16).castIds(List.of(1L)).directorIds(List.of(2L))
				.screenwriterIds(List.of()).genreIds(List.of(1L)).build();
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
		request.setCastIds(List.of(1L));
		request.setDirectorIds(List.of(2L));
		request.setScreenwriterIds(List.of());
		request.setGenreIds(List.of(1L));
		return request;
	}
}