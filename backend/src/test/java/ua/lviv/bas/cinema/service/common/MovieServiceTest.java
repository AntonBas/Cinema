package ua.lviv.bas.cinema.service.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.filter.MovieFilter;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.mapper.MovieMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;
import ua.lviv.bas.cinema.scheduler.MovieScheduler;
import ua.lviv.bas.cinema.service.query.MovieQueryService;

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
	@Mock
	private MovieScheduler movieSchedule;
	@Mock
	private MovieQueryService movieQueryService;
	@Mock
	private PosterService posterService;

	@InjectMocks
	private MovieService movieService;

	private Movie movie;
	private MovieDetailResponse movieDetailResponse;
	private MovieCardResponse movieCardResponse;
	private MovieCreateRequest createRequest;
	private MovieUpdateRequest updateRequest;
	private Genre genre;
	private Person actor;
	private Person director;
	private Person screenwriter;

	@BeforeEach
	void setUp() {
		genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		actor = new Person();
		actor.setId(1L);
		actor.setName("Actor One");
		actor.setRole(PersonRole.ACTOR);

		director = new Person();
		director.setId(2L);
		director.setName("Director One");
		director.setRole(PersonRole.DIRECTOR);

		screenwriter = new Person();
		screenwriter.setId(3L);
		screenwriter.setName("Writer One");
		screenwriter.setRole(PersonRole.SCREENWRITER);

		movie = Movie.builder().id(1L).title("Test Movie").slug("test-movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_12)
				.posterFileName("poster.jpg").actors(new HashSet<>(List.of(actor)))
				.directors(new HashSet<>(List.of(director))).screenwriters(new HashSet<>(List.of(screenwriter)))
				.genres(new HashSet<>(List.of(genre))).build();

		movieDetailResponse = MovieDetailResponse.builder().id(1L).title("Test Movie").slug("test-movie")
				.trailerUrl("https://example.com/trailer").description("Test Description").durationMinutes(120)
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.status(MovieStatus.UPCOMING).ageRating(AgeRating.PEGI_12).posterFileName("poster.jpg")
				.posterUrl("/api/movies/1/poster").currentlyShowing(false).upcoming(true).archived(false).active(true)
				.build();

		movieCardResponse = MovieCardResponse.builder().id(1L).title("Test Movie").slug("test-movie")
				.durationMinutes(120).ageRating(AgeRating.PEGI_12).releaseDate(LocalDate.now().plusDays(1))
				.status(MovieStatus.UPCOMING).currentlyShowing(false).build();

		createRequest = MovieCreateRequest.builder().title("New Movie").trailerUrl("https://example.com/trailer")
				.description("Test Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).ageRating(AgeRating.PEGI_12).genreIds(List.of(1L))
				.actorIds(List.of(1L)).directorIds(List.of(2L)).screenwriterIds(List.of(3L))
				.posterFile(new MockMultipartFile("poster", "poster.jpg", "image/jpeg", new byte[10])).build();

		updateRequest = MovieUpdateRequest.builder().title("Updated Movie")
				.trailerUrl("https://example.com/updated-trailer").description("Updated Description")
				.durationMinutes(140).releaseDate(LocalDate.now().plusDays(10))
				.endShowingDate(LocalDate.now().plusDays(70)).ageRating(AgeRating.PEGI_16).genreIds(List.of(1L))
				.actorIds(List.of(1L)).directorIds(List.of(2L)).screenwriterIds(List.of(3L)).removePoster(false)
				.build();
	}

	@Test
	void createMovie_ShouldCreateMovieSuccessfully() {
		when(slugService.generateUniqueSlug("New Movie")).thenReturn("new-movie");
		when(movieRepository.findBySlug("new-movie")).thenReturn(Optional.empty());
		when(movieMapper.toEntity(createRequest)).thenReturn(movie);
		when(movieSchedule.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(director));
		when(personRepository.findAllById(List.of(3L))).thenReturn(List.of(screenwriter));
		when(posterService.uploadPoster(any())).thenReturn("poster.jpg");
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);
		when(posterService.getPosterUrl(eq(1L), eq("poster.jpg"))).thenReturn("/api/movies/1/poster");

		MovieDetailResponse result = movieService.createMovie(createRequest);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getTitle()).isEqualTo("Test Movie");
		verify(movieRepository).save(movie);
		verify(movieSchedule).calculateMovieStatus(movie, LocalDate.now());
		verify(posterService).uploadPoster(any());
	}

	@Test
	void createMovie_ShouldThrowException_WhenEndDateBeforeReleaseDate() {
		MovieCreateRequest invalidRequest = MovieCreateRequest.builder().title("Invalid Movie")
				.trailerUrl("https://example.com/trailer").description("Desc").durationMinutes(120)
				.releaseDate(LocalDate.now().plusDays(10)).endShowingDate(LocalDate.now().plusDays(5))
				.ageRating(AgeRating.PEGI_12).genreIds(List.of(1L)).actorIds(List.of(1L)).directorIds(List.of(1L))
				.screenwriterIds(List.of(1L)).build();

		assertThatThrownBy(() -> movieService.createMovie(invalidRequest)).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("End showing date cannot be before release date");
	}

	@Test
	void createMovie_ShouldThrowDuplicateEntityException_WhenSlugExists() {
		when(slugService.generateUniqueSlug("New Movie")).thenReturn("existing-movie");
		when(movieRepository.findBySlug("existing-movie")).thenReturn(Optional.of(movie));

		assertThatThrownBy(() -> movieService.createMovie(createRequest)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getMovieById_ShouldReturnMovie() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);
		when(posterService.getPosterUrl(eq(1L), eq("poster.jpg"))).thenReturn("/api/movies/1/poster");

		MovieDetailResponse result = movieService.getMovieById(1L);

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getTitle()).isEqualTo("Test Movie");
	}

	@Test
	void getMovieById_ShouldThrowMovieNotFoundException_WhenMovieNotExists() {
		when(movieRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovieById(999L)).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void getMovieBySlug_ShouldReturnMovie() {
		when(movieRepository.findBySlug("test-movie")).thenReturn(Optional.of(movie));
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);
		when(posterService.getPosterUrl(eq(1L), eq("poster.jpg"))).thenReturn("/api/movies/1/poster");

		MovieDetailResponse result = movieService.getMovieBySlug("test-movie");

		assertThat(result).isNotNull();
		assertThat(result.getSlug()).isEqualTo("test-movie");
	}

	@Test
	void getMovieBySlug_ShouldThrowMovieNotFoundException_WhenMovieNotExists() {
		when(movieRepository.findBySlug("nonexistent")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovieBySlug("nonexistent")).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void getMoviesPaginated_ShouldReturnPaginatedMovies() {
		Page<Movie> moviePage = new PageImpl<>(List.of(movie));
		when(movieRepository.findAll(any(Pageable.class))).thenReturn(moviePage);
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);
		when(posterService.getPosterUrl(eq(1L), eq("poster.jpg"))).thenReturn("/api/movies/1/poster");

		PageResponse<MovieDetailResponse> result = movieService.getMoviesPaginated(Pageable.unpaged());

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findFilteredMovies_ShouldReturnFilteredMovies() {
		MovieFilter filter = MovieFilter.builder().searchTerm("test").page(0).size(10).build();

		Page<Movie> moviePage = new PageImpl<>(List.of(movie));
		when(movieQueryService.findFilteredMovies(filter)).thenReturn(moviePage);
		when(movieMapper.toCardResponse(movie)).thenReturn(movieCardResponse);

		PageResponse<MovieCardResponse> result = movieService.findFilteredMovies(filter);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findCurrentlyShowingPaginated_ShouldReturnCurrentMovies() {
		Page<Movie> moviePage = new PageImpl<>(List.of(movie));
		when(movieQueryService.findCurrentlyShowing(any(Pageable.class), eq(false))).thenReturn(moviePage);
		when(movieMapper.toCardResponse(movie)).thenReturn(movieCardResponse);

		PageResponse<MovieCardResponse> result = movieService.findCurrentlyShowingPaginated(Pageable.unpaged(), false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void findUpcomingPaginated_ShouldReturnUpcomingMovies() {
		Page<Movie> moviePage = new PageImpl<>(List.of(movie));
		when(movieQueryService.findUpcoming(any(Pageable.class), eq(false))).thenReturn(moviePage);
		when(movieMapper.toCardResponse(movie)).thenReturn(movieCardResponse);

		PageResponse<MovieCardResponse> result = movieService.findUpcomingPaginated(Pageable.unpaged(), false);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getCurrentlyShowingMovies_ShouldReturnCurrentMoviesList() {
		when(movieQueryService.findCurrentlyShowingList()).thenReturn(List.of(movie));
		when(movieMapper.toCardResponseList(List.of(movie))).thenReturn(List.of(movieCardResponse));

		List<MovieCardResponse> result = movieService.getCurrentlyShowingMovies();

		assertThat(result).hasSize(1);
	}

	@Test
	void getUpcomingMovies_ShouldReturnUpcomingMoviesList() {
		when(movieQueryService.findUpcomingList()).thenReturn(List.of(movie));
		when(movieMapper.toCardResponseList(List.of(movie))).thenReturn(List.of(movieCardResponse));

		List<MovieCardResponse> result = movieService.getUpcomingMovies();

		assertThat(result).hasSize(1);
	}

	@Test
	void getNewReleases_ShouldReturnNewReleases() {
		when(movieQueryService.findNewReleases(5)).thenReturn(List.of(movie));
		when(movieMapper.toCardResponseList(List.of(movie))).thenReturn(List.of(movieCardResponse));

		List<MovieCardResponse> result = movieService.getNewReleases(5);

		assertThat(result).hasSize(1);
	}

	@Test
	void getEndingSoon_ShouldReturnEndingSoonMovies() {
		when(movieQueryService.findEndingSoon(5)).thenReturn(List.of(movie));
		when(movieMapper.toCardResponseList(List.of(movie))).thenReturn(List.of(movieCardResponse));

		List<MovieCardResponse> result = movieService.getEndingSoon(5);

		assertThat(result).hasSize(1);
	}

	@Test
	void deleteMovie_ShouldDeleteMovieSuccessfully() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

		movieService.deleteMovie(1L);

		verify(posterService).deletePoster("poster.jpg");
		verify(movieRepository).delete(movie);
	}

	@Test
	void deleteMovie_ShouldThrowMovieNotFoundException_WhenMovieNotExists() {
		when(movieRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.deleteMovie(999L)).isInstanceOf(MovieNotFoundException.class);

		verify(posterService, never()).deletePoster(any());
		verify(movieRepository, never()).delete(any());
	}

	@Test
	void updateMovie_ShouldUpdateMovieSuccessfully() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("Updated Movie")).thenReturn("updated-movie");
		when(slugService.isSlugAvailableForMovie("updated-movie", 1L)).thenReturn(true);
		when(movieSchedule.calculateMovieStatus(any(Movie.class), eq(LocalDate.now())))
				.thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(director));
		when(personRepository.findAllById(List.of(3L))).thenReturn(List.of(screenwriter));
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);
		when(posterService.getPosterUrl(eq(1L), eq("poster.jpg"))).thenReturn("/api/movies/1/poster");

		MovieDetailResponse result = movieService.updateMovie(1L, updateRequest);

		assertThat(result).isNotNull();
		verify(movieRepository).save(movie);
		verify(movieSchedule).calculateMovieStatus(any(Movie.class), eq(LocalDate.now()));
	}

	@Test
	void updateMovie_ShouldThrowException_WhenEndDateBeforeReleaseDate() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

		MovieUpdateRequest invalidRequest = MovieUpdateRequest.builder().title("Updated Movie")
				.trailerUrl("https://example.com/trailer").description("Desc").durationMinutes(120)
				.releaseDate(LocalDate.now().plusDays(10)).endShowingDate(LocalDate.now().plusDays(5))
				.ageRating(AgeRating.PEGI_12).genreIds(List.of(1L)).actorIds(List.of(1L)).directorIds(List.of(1L))
				.screenwriterIds(List.of(1L)).build();

		assertThatThrownBy(() -> movieService.updateMovie(1L, invalidRequest))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("End showing date cannot be before release date");
	}

	@Test
	void updateMovie_ShouldThrowDuplicateEntityException_WhenNewSlugExists() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("Updated Movie")).thenReturn("existing-slug");
		when(slugService.isSlugAvailableForMovie("existing-slug", 1L)).thenReturn(false);

		assertThatThrownBy(() -> movieService.updateMovie(1L, updateRequest))
				.isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void updateMovie_ShouldHandlePosterUpdate_WhenNewPosterProvided() {
		updateRequest.setPosterFile(new MockMultipartFile("poster", "new-poster.jpg", "image/jpeg", new byte[10]));

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("Updated Movie")).thenReturn("updated-movie");
		when(slugService.isSlugAvailableForMovie("updated-movie", 1L)).thenReturn(true);
		when(movieSchedule.calculateMovieStatus(any(Movie.class), eq(LocalDate.now())))
				.thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(director));
		when(personRepository.findAllById(List.of(3L))).thenReturn(List.of(screenwriter));
		when(posterService.uploadPoster(any())).thenReturn("new-poster.jpg");
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);
		when(posterService.getPosterUrl(eq(1L), eq("new-poster.jpg"))).thenReturn("/api/movies/1/poster");

		movieService.updateMovie(1L, updateRequest);

		verify(posterService).deletePoster("poster.jpg");
		verify(posterService).uploadPoster(any());
	}

	@Test
	void updateMovie_ShouldHandlePosterRemoval_WhenRemovePosterIsTrue() {
		updateRequest.setRemovePoster(true);

		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		when(slugService.generateUniqueSlug("Updated Movie")).thenReturn("updated-movie");
		when(slugService.isSlugAvailableForMovie("updated-movie", 1L)).thenReturn(true);
		when(movieSchedule.calculateMovieStatus(any(Movie.class), eq(LocalDate.now())))
				.thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(director));
		when(personRepository.findAllById(List.of(3L))).thenReturn(List.of(screenwriter));
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);
		when(posterService.getPosterUrl(eq(1L), eq(null))).thenReturn("/images/default-poster.jpg");

		movieService.updateMovie(1L, updateRequest);

		verify(posterService).deletePoster("poster.jpg");
	}

	@Test
	void searchMoviesForSessionCreation_ShouldReturnAvailableMovies() {
		LocalDate sessionDate = LocalDate.now().plusDays(5);
		when(movieQueryService.findMoviesForSessionCreation("test", sessionDate)).thenReturn(List.of(movie));

		List<MovieSessionSearchResponse> result = movieService.searchMoviesForSessionCreation("test", sessionDate);

		assertThat(result).hasSize(1);
	}

	@Test
	void getMoviePoster_ShouldReturnPosterResponse() {
		when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
		ResponseEntity<byte[]> posterResponse = ResponseEntity.ok().body(new byte[10]);
		when(posterService.getPosterResponse("poster.jpg")).thenReturn(posterResponse);

		ResponseEntity<byte[]> result = movieService.getMoviePoster(1L);

		assertThat(result).isEqualTo(posterResponse);
	}

	@Test
	void createMovie_ShouldHandleNullPosterFile() {
		createRequest.setPosterFile(null);

		when(slugService.generateUniqueSlug("New Movie")).thenReturn("new-movie");
		when(movieRepository.findBySlug("new-movie")).thenReturn(Optional.empty());
		when(movieMapper.toEntity(createRequest)).thenReturn(movie);
		when(movieSchedule.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(personRepository.findAllById(List.of(1L))).thenReturn(List.of(actor));
		when(personRepository.findAllById(List.of(2L))).thenReturn(List.of(director));
		when(personRepository.findAllById(List.of(3L))).thenReturn(List.of(screenwriter));
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toDetailResponse(movie)).thenReturn(movieDetailResponse);
		lenient().when(posterService.getPosterUrl(eq(1L), eq(null))).thenReturn("/images/default-poster.jpg");

		MovieDetailResponse result = movieService.createMovie(createRequest);

		assertThat(result).isNotNull();
		verify(posterService, never()).uploadPoster(any());
	}

	@Test
	void getMoviePoster_ShouldReturnNotFound_WhenMovieNotFound() {
		when(movieRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMoviePoster(1L)).isInstanceOf(MovieNotFoundException.class);
	}
}