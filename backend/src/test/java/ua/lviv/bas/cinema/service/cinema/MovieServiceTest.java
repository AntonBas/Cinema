package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
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

import ua.lviv.bas.cinema.domain.cinema.Genre;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Person;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieValidationException;
import ua.lviv.bas.cinema.mapper.cinema.MovieMapper;
import ua.lviv.bas.cinema.repository.cinema.GenreRepository;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;
import ua.lviv.bas.cinema.repository.cinema.PersonRepository;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieCardProjection;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieDetailProjection;
import ua.lviv.bas.cinema.scheduler.MovieScheduler;
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

	@InjectMocks
	private MovieService movieService;

	private final Long MOVIE_ID = 1L;
	private final String MOVIE_TITLE = "Test Movie";
	private final String SLUG = "test-movie";
	private Movie movie;
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
		movie.setReleaseDate(LocalDate.now().plusDays(1));
		movie.setEndShowingDate(LocalDate.now().plusDays(30));
		movie.setStatus(MovieStatus.UPCOMING);

		detailResponse = new MovieDetailResponse(MOVIE_ID, MOVIE_TITLE, SLUG, "trailer.mp4", "Description", 120,
				LocalDate.now(), LocalDate.now().plusDays(30), AgeRating.PEGI_12, MovieStatus.UPCOMING, "poster.jpg",
				"/api/movies/1/poster", List.of(), List.of(), List.of(), List.of());

		cardResponse = new MovieCardResponse(MOVIE_ID, SLUG, MOVIE_TITLE, "/api/movies/1/poster", 120,
				AgeRating.PEGI_12, MovieStatus.UPCOMING);

		createRequest = MovieCreateRequest.builder().title(MOVIE_TITLE).releaseDate(LocalDate.now().plusDays(1))
				.endShowingDate(LocalDate.now().plusDays(30)).genreIds(List.of(1L, 2L)).actorIds(List.of(3L, 4L))
				.directorIds(List.of(5L)).screenwriterIds(List.of(6L)).build();

		updateRequest = MovieUpdateRequest.builder().title("Updated Title").releaseDate(LocalDate.now().plusDays(2))
				.endShowingDate(LocalDate.now().plusDays(40)).genreIds(List.of(1L)).actorIds(List.of(3L))
				.directorIds(List.of(5L, 7L)).screenwriterIds(List.of(6L, 8L)).build();
	}

	@Test
	void createMovie_Success() {
		when(slugService.generateUniqueSlug(MOVIE_TITLE)).thenReturn(SLUG);
		when(movieRepository.findBySlug(SLUG)).thenReturn(Optional.empty());
		when(movieMapper.toMovie(createRequest)).thenReturn(movie);
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);

		List<Genre> genres = List.of(new Genre(), new Genre());
		List<Person> actors = List.of(new Person(), new Person());
		List<Person> directors = List.of(new Person());
		List<Person> screenwriters = List.of(new Person());

		when(genreRepository.findAllById(createRequest.getGenreIds())).thenReturn(genres);
		when(personRepository.findAllById(createRequest.getActorIds())).thenReturn(actors);
		when(personRepository.findAllById(createRequest.getDirectorIds())).thenReturn(directors);
		when(personRepository.findAllById(createRequest.getScreenwriterIds())).thenReturn(screenwriters);

		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(detailResponse);

		MovieDetailResponse result = movieService.createMovie(createRequest);

		assertThat(result).isEqualTo(detailResponse);
		verify(movieRepository).save(movie);
	}

	@Test
	void createMovie_InvalidDates_ThrowsException() {
		MovieCreateRequest invalidRequest = MovieCreateRequest.builder().title(MOVIE_TITLE)
				.releaseDate(LocalDate.now().plusDays(10)).endShowingDate(LocalDate.now().plusDays(5))
				.genreIds(List.of(1L)).actorIds(List.of(1L)).directorIds(List.of(1L)).screenwriterIds(List.of(1L))
				.build();

		assertThatThrownBy(() -> movieService.createMovie(invalidRequest)).isInstanceOf(MovieValidationException.class);
	}

	@Test
	void createMovie_DuplicateSlug_ThrowsException() {
		when(slugService.generateUniqueSlug(MOVIE_TITLE)).thenReturn(SLUG);
		when(movieRepository.findBySlug(SLUG)).thenReturn(Optional.of(new Movie()));

		assertThatThrownBy(() -> movieService.createMovie(createRequest)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getMovieById_Success() {
		MovieDetailProjection projection = createMovieDetailProjection();
		when(movieRepository.findDetailProjectionById(MOVIE_ID)).thenReturn(Optional.of(projection));
		when(movieMapper.toMovieDetailResponse(projection)).thenReturn(detailResponse);

		MovieDetailResponse result = movieService.getMovieById(MOVIE_ID);

		assertThat(result).isEqualTo(detailResponse);
	}

	@Test
	void getMovieById_NotFound_ThrowsException() {
		when(movieRepository.findDetailProjectionById(MOVIE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovieById(MOVIE_ID)).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void getAdminMovieById_Success() {
		when(movieRepository.findAdminMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(detailResponse);

		MovieDetailResponse result = movieService.getAdminMovieById(MOVIE_ID);

		assertThat(result).isEqualTo(detailResponse);
	}

	@Test
	void getMovieBySlug_Success() {
		when(movieRepository.findBySlug(SLUG)).thenReturn(Optional.of(movie));
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(detailResponse);

		MovieDetailResponse result = movieService.getMovieBySlug(SLUG);

		assertThat(result).isEqualTo(detailResponse);
	}

	@Test
	void updateMovie_Success() {
		Movie existingMovie = new Movie();
		existingMovie.setId(MOVIE_ID);
		existingMovie.setTitle("Old Title");
		existingMovie.setReleaseDate(LocalDate.now().plusDays(1));
		existingMovie.setEndShowingDate(LocalDate.now().plusDays(30));

		when(movieRepository.findAdminMovieById(MOVIE_ID)).thenReturn(Optional.of(existingMovie));

		when(slugService.generateUniqueSlug("Updated Title")).thenReturn("updated-title");
		when(slugService.isSlugAvailableForMovie("updated-title", MOVIE_ID)).thenReturn(true);

		when(movieScheduler.calculateMovieStatus(existingMovie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);

		List<Genre> genres = List.of(new Genre());
		List<Person> actors = List.of(new Person());
		List<Person> directors = List.of(new Person(), new Person());
		List<Person> screenwriters = List.of(new Person(), new Person());

		when(genreRepository.findAllById(updateRequest.getGenreIds())).thenReturn(genres);
		when(personRepository.findAllById(updateRequest.getActorIds())).thenReturn(actors);
		when(personRepository.findAllById(updateRequest.getDirectorIds())).thenReturn(directors);
		when(personRepository.findAllById(updateRequest.getScreenwriterIds())).thenReturn(screenwriters);

		when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
		when(movieMapper.toMovieDetailResponse(existingMovie)).thenReturn(detailResponse);

		MovieDetailResponse result = movieService.updateMovie(MOVIE_ID, updateRequest);

		assertThat(result).isEqualTo(detailResponse);
		verify(movieMapper).updateMovieFromRequest(updateRequest, existingMovie);
		verify(movieRepository).save(existingMovie);
	}

	@Test
	void updateMovie_SameTitle_DoesNotChangeSlug() {
		Movie existingMovie = new Movie();
		existingMovie.setId(MOVIE_ID);
		existingMovie.setTitle("Same Title");
		existingMovie.setReleaseDate(LocalDate.now().plusDays(1));
		existingMovie.setEndShowingDate(LocalDate.now().plusDays(30));

		MovieUpdateRequest sameTitleRequest = MovieUpdateRequest.builder().title("Same Title")
				.releaseDate(LocalDate.now().plusDays(2)).endShowingDate(LocalDate.now().plusDays(40))
				.genreIds(List.of(1L)).actorIds(List.of(1L)).directorIds(List.of(1L)).screenwriterIds(List.of(1L))
				.build();

		when(movieRepository.findAdminMovieById(MOVIE_ID)).thenReturn(Optional.of(existingMovie));
		when(movieScheduler.calculateMovieStatus(existingMovie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(anyList())).thenReturn(List.of());
		when(personRepository.findAllById(anyList())).thenReturn(List.of());
		when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
		when(movieMapper.toMovieDetailResponse(existingMovie)).thenReturn(detailResponse);

		MovieDetailResponse result = movieService.updateMovie(MOVIE_ID, sameTitleRequest);

		assertThat(result).isEqualTo(detailResponse);
		verify(slugService, never()).generateUniqueSlug(anyString());
	}

	@Test
	void deleteMovie_WithPoster_DeletesPoster() {
		movie.setPosterFileName("poster.jpg");

		when(movieRepository.findAdminMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));

		movieService.deleteMovie(MOVIE_ID);

		verify(posterService).deletePoster("poster.jpg");
		verify(movieRepository).delete(movie);
	}

	@Test
	void deleteMovie_WithoutPoster_DoesNotDeletePoster() {
		when(movieRepository.findAdminMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));

		movieService.deleteMovie(MOVIE_ID);

		verify(posterService, never()).deletePoster(anyString());
		verify(movieRepository).delete(movie);
	}

	@Test
	void getFilteredMovies_ReturnsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<Movie> moviePage = new PageImpl<>(List.of(movie));

		when(movieRepository.findMoviesByFilters(MOVIE_TITLE, MovieStatus.UPCOMING, pageable)).thenReturn(moviePage);
		when(movieMapper.toMovieCardResponse(movie)).thenReturn(cardResponse);

		Page<MovieCardResponse> result = movieService.getFilteredMovies(MOVIE_TITLE, MovieStatus.UPCOMING, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(cardResponse);
	}

	@Test
	void searchMoviesForSession_ByTitle_ReturnsList() {
		MovieCardProjection projection = createMovieCardProjection();
		when(movieRepository.findMoviesForSession("test")).thenReturn(List.of(projection));

		var result = movieService.searchMoviesForSession("test");

		assertThat(result).hasSize(1);
		assertThat(result.get(0).id()).isEqualTo(MOVIE_ID);
		assertThat(result.get(0).title()).isEqualTo(MOVIE_TITLE);
	}

	@Test
	void searchMoviesForSession_ByDate_ReturnsList() {
		LocalDate date = LocalDate.now();
		MovieCardProjection projection = createMovieCardProjection();
		when(movieRepository.findMoviesByDate(date)).thenReturn(List.of(projection));

		var result = movieService.searchMoviesForSession(date.toString());

		assertThat(result).hasSize(1);
	}

	@Test
	void existsBySlug_ReturnsTrue() {
		when(movieRepository.findBySlug(SLUG)).thenReturn(Optional.of(movie));

		boolean result = movieService.existsBySlug(SLUG);

		assertThat(result).isTrue();
	}

	private MovieDetailProjection createMovieDetailProjection() {
		return new MovieDetailProjection() {
			@Override
			public Long getId() {
				return MOVIE_ID;
			}

			@Override
			public String getTitle() {
				return MOVIE_TITLE;
			}

			@Override
			public String getSlug() {
				return SLUG;
			}

			@Override
			public String getTrailerUrl() {
				return "trailer.mp4";
			}

			@Override
			public String getDescription() {
				return "Description";
			}

			@Override
			public Integer getDurationMinutes() {
				return 120;
			}

			@Override
			public LocalDate getReleaseDate() {
				return LocalDate.now();
			}

			@Override
			public LocalDate getEndShowingDate() {
				return LocalDate.now().plusDays(30);
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
			public String getPosterFileName() {
				return "poster.jpg";
			}
		};
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
				return LocalDate.now();
			}

			@Override
			public LocalDate getEndShowingDate() {
				return LocalDate.now().plusDays(30);
			}
		};
	}
}