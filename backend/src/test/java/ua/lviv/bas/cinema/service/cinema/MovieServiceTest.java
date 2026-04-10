package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import ua.lviv.bas.cinema.domain.cinema.Genre;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Person;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieAdminResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.mapper.cinema.MovieMapper;
import ua.lviv.bas.cinema.repository.cinema.GenreRepository;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;
import ua.lviv.bas.cinema.repository.cinema.PersonRepository;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieCardProjection;
import ua.lviv.bas.cinema.scheduler.MovieScheduler;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.integration.file.PosterService;
import ua.lviv.bas.cinema.service.integration.slug.SlugService;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

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
	private MovieScheduler movieScheduler;
	@Mock
	private PosterService posterService;
	@Mock
	private AuditService auditService;

	@InjectMocks
	private MovieService movieService;

	private final Long MOVIE_ID = 1L;
	private final String MOVIE_TITLE = "Test Movie";
	private final String SLUG = "test-movie";
	private Movie movie;
	private MovieAdminResponse adminResponse;
	private MovieDetailResponse detailResponse;
	private MovieCardResponse cardResponse;
	private MovieCreateRequest createRequest;
	private MovieUpdateRequest updateRequest;

	@BeforeEach
	void setUp() {
		movie = new Movie();
		movie.setId(MOVIE_ID);
		movie.setTitle(MOVIE_TITLE);
		movie.setSlug(SLUG);
		movie.setTrailerUrl("trailer.mp4");
		movie.setDescription("Description");
		movie.setDurationMinutes(120);
		movie.setReleaseDate(LocalDate.now().plusDays(1));
		movie.setEndShowingDate(LocalDate.now().plusDays(30));
		movie.setAgeRating(AgeRating.PEGI_12);
		movie.setStatus(MovieStatus.UPCOMING);
		movie.setPosterFileName("poster.jpg");

		adminResponse = new MovieAdminResponse(MOVIE_ID, MOVIE_TITLE, "trailer.mp4", "Description", 120,
				LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), AgeRating.PEGI_12, MovieStatus.UPCOMING,
				"poster.jpg", "/api/movies/1/poster", List.of(), List.of(), List.of(), List.of());

		detailResponse = new MovieDetailResponse(MOVIE_ID, MOVIE_TITLE, SLUG, "trailer.mp4", "Description", 120,
				LocalDate.now().plusDays(1), LocalDate.now().plusDays(30), AgeRating.PEGI_12, MovieStatus.UPCOMING,
				"poster.jpg", "/api/movies/1/poster", List.of(), List.of(), List.of(), List.of(), List.of());

		cardResponse = new MovieCardResponse(MOVIE_ID, SLUG, MOVIE_TITLE, "/api/movies/1/poster", 120,
				AgeRating.PEGI_12, MovieStatus.UPCOMING);

		createRequest = MovieCreateRequest.builder().title(MOVIE_TITLE).trailerUrl("trailer.mp4")
				.description("Description").durationMinutes(120).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).ageRating(AgeRating.PEGI_12).genreIds(List.of(1L, 2L))
				.actorIds(List.of(3L, 4L)).directorIds(List.of(5L)).screenwriterIds(List.of(6L))
				.posterFile(mock(MultipartFile.class)).build();

		updateRequest = MovieUpdateRequest.builder().title("Updated Title").trailerUrl("new-trailer.mp4")
				.description("New Description").durationMinutes(130).releaseDate(LocalDate.now().plusDays(2))
				.endShowingDate(LocalDate.now().plusDays(40)).ageRating(AgeRating.PEGI_16).genreIds(List.of(1L))
				.actorIds(List.of(3L)).directorIds(List.of(5L, 7L)).screenwriterIds(List.of(6L, 8L)).removePoster(false)
				.build();
	}

	@Test
	void createMovieShouldSucceed() {
		when(slugService.generateUniqueSlug(MOVIE_TITLE)).thenReturn(SLUG);
		when(movieRepository.findBySlug(SLUG)).thenReturn(Optional.empty());
		when(movieMapper.toMovie(createRequest)).thenReturn(movie);
		when(movieScheduler.calculateMovieStatus(any(Movie.class), any(LocalDate.class)))
				.thenReturn(MovieStatus.UPCOMING);
		when(posterService.uploadPoster(any())).thenReturn("poster.jpg");
		doNothing().when(auditService).logChange(anyString(), any(), anyString(), any(), any(), any());

		List<Genre> genres = List.of(new Genre(), new Genre());
		List<Person> actors = List.of(new Person(), new Person());
		List<Person> directors = List.of(new Person());
		List<Person> screenwriters = List.of(new Person());

		when(genreRepository.findAllById(createRequest.getGenreIds())).thenReturn(genres);
		when(personRepository.findAllById(createRequest.getActorIds())).thenReturn(actors);
		when(personRepository.findAllById(createRequest.getDirectorIds())).thenReturn(directors);
		when(personRepository.findAllById(createRequest.getScreenwriterIds())).thenReturn(screenwriters);

		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieAdminResponse(movie)).thenReturn(adminResponse);

		MovieAdminResponse result = movieService.createMovie(createRequest);

		assertThat(result).isEqualTo(adminResponse);
		verify(movieRepository).save(movie);
	}

	@Test
	void createMovieWithDuplicateSlugShouldThrowException() {
		when(slugService.generateUniqueSlug(MOVIE_TITLE)).thenReturn(SLUG);
		when(movieRepository.findBySlug(SLUG)).thenReturn(Optional.of(new Movie()));

		assertThatThrownBy(() -> movieService.createMovie(createRequest)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getMovieShouldSucceed() {
		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));
		when(movieMapper.toMovieAdminResponse(movie)).thenReturn(adminResponse);

		MovieAdminResponse result = movieService.getMovie(MOVIE_ID);

		assertThat(result).isEqualTo(adminResponse);
	}

	@Test
	void getMovieWhenNotFoundShouldThrowException() {
		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovie(MOVIE_ID)).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void getMovieBySlugShouldSucceed() {
		when(movieRepository.findBySlugWithFutureSessions(SLUG)).thenReturn(Optional.of(movie));
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(detailResponse);

		MovieDetailResponse result = movieService.getMovieBySlug(SLUG);

		assertThat(result).isEqualTo(detailResponse);
	}

	@Test
	void getMovieBySlugWhenArchivedShouldThrowException() {
		movie.setStatus(MovieStatus.ARCHIVED);
		when(movieRepository.findBySlugWithFutureSessions(SLUG)).thenReturn(Optional.of(movie));

		assertThatThrownBy(() -> movieService.getMovieBySlug(SLUG)).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void getMoviesShouldReturnPage() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> moviePage = new PageImpl<>(List.of(movie));

		when(movieRepository.findMoviesByQueryAndStatus(MOVIE_TITLE, MovieStatus.UPCOMING, pageable))
				.thenReturn(moviePage);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(cardResponse);

		Page<MovieCardResponse> result = movieService.getMovies(MOVIE_TITLE, MovieStatus.UPCOMING, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(cardResponse);
	}

	@Test
	void updateMovieShouldSucceed() {
		Movie existingMovie = new Movie();
		existingMovie.setId(MOVIE_ID);
		existingMovie.setTitle("Old Title");
		existingMovie.setSlug("old-title");
		existingMovie.setReleaseDate(LocalDate.now().plusDays(1));
		existingMovie.setEndShowingDate(LocalDate.now().plusDays(30));

		when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(existingMovie));
		when(movieRepository.existsByTitle("Updated Title")).thenReturn(false);
		when(movieScheduler.calculateMovieStatus(any(Movie.class), any(LocalDate.class)))
				.thenReturn(MovieStatus.UPCOMING);
		doNothing().when(auditService).logChange(anyString(), any(), anyString(), any(), any(), any());

		List<Genre> genres = List.of(new Genre());
		List<Person> actors = List.of(new Person());
		List<Person> directors = List.of(new Person(), new Person());
		List<Person> screenwriters = List.of(new Person(), new Person());

		when(genreRepository.findAllById(updateRequest.getGenreIds())).thenReturn(genres);
		when(personRepository.findAllById(updateRequest.getActorIds())).thenReturn(actors);
		when(personRepository.findAllById(updateRequest.getDirectorIds())).thenReturn(directors);
		when(personRepository.findAllById(updateRequest.getScreenwriterIds())).thenReturn(screenwriters);

		when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
		when(movieMapper.toMovieAdminResponse(existingMovie)).thenReturn(adminResponse);

		MovieAdminResponse result = movieService.updateMovie(MOVIE_ID, updateRequest);

		assertThat(result).isEqualTo(adminResponse);
		verify(movieMapper).updateMovieFromRequest(updateRequest, existingMovie);
		verify(movieRepository).save(existingMovie);
	}

	@Test
	void updateMovieWithDuplicateTitleShouldThrowException() {
		Movie existingMovie = new Movie();
		existingMovie.setId(MOVIE_ID);
		existingMovie.setTitle("Old Title");

		when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(existingMovie));
		when(movieRepository.existsByTitle("Updated Title")).thenReturn(true);

		assertThatThrownBy(() -> movieService.updateMovie(MOVIE_ID, updateRequest))
				.isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void updateMovieWithSameTitleShouldNotChangeSlug() {
		Movie existingMovie = new Movie();
		existingMovie.setId(MOVIE_ID);
		existingMovie.setTitle("Same Title");
		existingMovie.setSlug("same-title");
		existingMovie.setReleaseDate(LocalDate.now().plusDays(1));
		existingMovie.setEndShowingDate(LocalDate.now().plusDays(30));

		MovieUpdateRequest sameTitleRequest = MovieUpdateRequest.builder().title("Same Title")
				.releaseDate(LocalDate.now().plusDays(2)).endShowingDate(LocalDate.now().plusDays(40))
				.genreIds(List.of(1L)).actorIds(List.of(1L)).directorIds(List.of(1L)).screenwriterIds(List.of(1L))
				.build();

		when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(existingMovie));
		when(movieScheduler.calculateMovieStatus(any(Movie.class), any(LocalDate.class)))
				.thenReturn(MovieStatus.UPCOMING);
		doNothing().when(auditService).logChange(anyString(), any(), anyString(), any(), any(), any());
		when(genreRepository.findAllById(anyList())).thenReturn(List.of());
		when(personRepository.findAllById(anyList())).thenReturn(List.of());
		when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
		when(movieMapper.toMovieAdminResponse(existingMovie)).thenReturn(adminResponse);

		movieService.updateMovie(MOVIE_ID, sameTitleRequest);

		verify(slugService, never()).generateUniqueSlug(anyString());
	}

	@Test
	void deleteMovieWithPosterShouldDeletePoster() {
		movie.setPosterFileName("poster.jpg");

		when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
		doNothing().when(auditService).logChange(anyString(), any(), anyString(), any(), any(), any());

		movieService.deleteMovie(MOVIE_ID);

		verify(posterService).deletePoster("poster.jpg");
		verify(movieRepository).delete(movie);
	}

	@Test
	void deleteMovieWithoutPosterShouldNotDeletePoster() {
		movie.setPosterFileName(null);

		when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
		doNothing().when(auditService).logChange(anyString(), any(), anyString(), any(), any(), any());

		movieService.deleteMovie(MOVIE_ID);

		verify(posterService, never()).deletePoster(anyString());
		verify(movieRepository).delete(movie);
	}

	@Test
	void deleteMovieWhenNotFoundShouldThrowException() {
		when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.deleteMovie(MOVIE_ID)).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void searchMoviesByTitleShouldReturnList() {
		MovieCardProjection projection = createMovieCardProjection();
		when(movieRepository.findMoviesForSessionSearch("test")).thenReturn(List.of(projection));
		when(movieMapper.toMovieSessionSearchResponse(projection))
				.thenReturn(new MovieSessionSearchResponse(MOVIE_ID, MOVIE_TITLE, 120));

		List<MovieSessionSearchResponse> result = movieService.searchMovies("test");

		assertThat(result).hasSize(1);
		assertThat(result.get(0).id()).isEqualTo(MOVIE_ID);
		assertThat(result.get(0).title()).isEqualTo(MOVIE_TITLE);
		assertThat(result.get(0).durationMinutes()).isEqualTo(120);
	}

	@Test
	void searchMoviesByDateShouldReturnList() {
		LocalDate date = LocalDate.now();
		MovieCardProjection projection = createMovieCardProjection();
		when(movieRepository.findMoviesByDate(date)).thenReturn(List.of(projection));
		when(movieMapper.toMovieSessionSearchResponse(projection))
				.thenReturn(new MovieSessionSearchResponse(MOVIE_ID, MOVIE_TITLE, 120));

		List<MovieSessionSearchResponse> result = movieService.searchMovies(date.toString());

		assertThat(result).hasSize(1);
		assertThat(result.get(0).id()).isEqualTo(MOVIE_ID);
	}

	@Test
	void searchMoviesWithNullQueryShouldReturnEmptyList() {
		List<MovieSessionSearchResponse> result = movieService.searchMovies(null);
		assertThat(result).isEmpty();
	}

	@Test
	void searchMoviesWithBlankQueryShouldReturnEmptyList() {
		List<MovieSessionSearchResponse> result = movieService.searchMovies("   ");
		assertThat(result).isEmpty();
	}

	@Test
	void getPosterShouldReturnResponse() {
		when(movieRepository.findPosterFileNameById(MOVIE_ID)).thenReturn(Optional.of("poster.jpg"));
		when(posterService.getPosterResponse("poster.jpg")).thenReturn(ResponseEntity.ok().body(new byte[0]));

		ResponseEntity<byte[]> result = movieService.getPoster(MOVIE_ID);

		assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
	}

	@Test
	void getPosterWhenNotFoundShouldReturnNotFound() {
		when(movieRepository.findPosterFileNameById(MOVIE_ID)).thenReturn(Optional.empty());

		ResponseEntity<byte[]> result = movieService.getPoster(MOVIE_ID);

		assertThat(result.getStatusCode().is4xxClientError()).isTrue();
	}

	private MovieCardProjection createMovieCardProjection() {
		return new MovieCardProjection() {
			@Override
			public Long getId() {
				return MOVIE_ID;
			}

			@Override
			public String getSlug() {
				return SLUG;
			}

			@Override
			public String getTitle() {
				return MOVIE_TITLE;
			}

			@Override
			public String getPosterFileName() {
				return "poster.jpg";
			}

			@Override
			public Integer getDurationMinutes() {
				return 120;
			}

			@Override
			public AgeRating getAgeRating() {
				return AgeRating.PEGI_12;
			}

			@Override
			public MovieStatus getStatus() {
				return MovieStatus.UPCOMING;
			}

			@Override
			public LocalDate getReleaseDate() {
				return LocalDate.now().plusDays(1);
			}

			@Override
			public LocalDate getEndShowingDate() {
				return LocalDate.now().plusDays(30);
			}
		};
	}
}