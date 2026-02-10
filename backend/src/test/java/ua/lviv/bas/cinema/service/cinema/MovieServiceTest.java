package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.projection.MovieDetailProjection;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieValidationException;
import ua.lviv.bas.cinema.mapper.MovieMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;
import ua.lviv.bas.cinema.scheduler.MovieScheduler;
import ua.lviv.bas.cinema.service.integration.file.PosterService;
import ua.lviv.bas.cinema.service.integration.slug.SlugService;

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
	private MovieScheduler movieScheduler;
	@Mock
	private PosterService posterService;

	@InjectMocks
	private MovieService movieService;

	private final Long MOVIE_ID = 1L;
	private final String MOVIE_TITLE = "Test Movie";
	private final String SLUG = "test-movie";

	@Test
	void createMovie_Success() {
		MovieCreateRequest request = MovieCreateRequest.builder().title(MOVIE_TITLE)
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.genreIds(Collections.emptyList()).actorIds(Collections.emptyList())
				.directorIds(Collections.emptyList()).screenwriterIds(Collections.emptyList()).build();

		Movie movie = new Movie();
		MovieDetailResponse response = new MovieDetailResponse();

		when(slugService.generateUniqueSlug(MOVIE_TITLE)).thenReturn(SLUG);
		when(movieRepository.findBySlug(SLUG)).thenReturn(Optional.empty());
		when(movieMapper.toMovie(request)).thenReturn(movie);
		when(movieScheduler.calculateMovieStatus(movie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());
		when(personRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());
		when(movieRepository.save(movie)).thenReturn(movie);
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);

		MovieDetailResponse result = movieService.createMovie(request);

		assertThat(result).isEqualTo(response);
		verify(movieRepository).save(movie);
	}

	@Test
	void createMovie_InvalidDates_ThrowsException() {
		MovieCreateRequest request = MovieCreateRequest.builder().title(MOVIE_TITLE)
				.releaseDate(LocalDate.now().plusDays(10)).endShowingDate(LocalDate.now().plusDays(5))
				.genreIds(Collections.emptyList()).actorIds(Collections.emptyList())
				.directorIds(Collections.emptyList()).screenwriterIds(Collections.emptyList()).build();

		assertThatThrownBy(() -> movieService.createMovie(request)).isInstanceOf(MovieValidationException.class);
	}

	@Test
	void createMovie_DuplicateSlug_ThrowsException() {
		MovieCreateRequest request = MovieCreateRequest.builder().title(MOVIE_TITLE)
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.genreIds(Collections.emptyList()).actorIds(Collections.emptyList())
				.directorIds(Collections.emptyList()).screenwriterIds(Collections.emptyList()).build();

		when(slugService.generateUniqueSlug(MOVIE_TITLE)).thenReturn(SLUG);
		when(movieRepository.findBySlug(SLUG)).thenReturn(Optional.of(new Movie()));

		assertThatThrownBy(() -> movieService.createMovie(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void updateMovie_Success() {
		Movie existingMovie = new Movie();
		existingMovie.setId(MOVIE_ID);
		existingMovie.setTitle("Old Title");

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("New Title")
				.releaseDate(LocalDate.now().plusDays(1)).endShowingDate(LocalDate.now().plusDays(30))
				.genreIds(Collections.emptyList()).actorIds(Collections.emptyList())
				.directorIds(Collections.emptyList()).screenwriterIds(Collections.emptyList()).build();

		MovieDetailResponse response = new MovieDetailResponse();

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(existingMovie));
		when(slugService.generateUniqueSlug("New Title")).thenReturn("new-movie");
		when(slugService.isSlugAvailableForMovie("new-movie", MOVIE_ID)).thenReturn(true);
		when(movieScheduler.calculateMovieStatus(existingMovie, LocalDate.now())).thenReturn(MovieStatus.UPCOMING);
		when(genreRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());
		when(personRepository.findAllById(Collections.emptyList())).thenReturn(Collections.emptyList());
		when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
		when(movieMapper.toMovieDetailResponse(existingMovie)).thenReturn(response);

		MovieDetailResponse result = movieService.updateMovie(MOVIE_ID, request);

		assertThat(result).isEqualTo(response);
		verify(movieMapper).updateMovieFromRequest(request, existingMovie);
		verify(movieRepository).save(existingMovie);
	}

	@Test
	void deleteMovie_Success() {
		Movie movie = new Movie();
		movie.setId(MOVIE_ID);
		movie.setPosterFileName("poster.jpg");

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

		movieService.deleteMovie(MOVIE_ID);

		verify(posterService).deletePoster("poster.jpg");
		verify(movieRepository).delete(movie);
	}

	@Test
	void getMovieById_Success() {
		MovieDetailProjection projection = createMovieDetailProjection();
		MovieDetailResponse response = new MovieDetailResponse();

		when(movieRepository.findDetailProjectionById(MOVIE_ID)).thenReturn(Optional.of(projection));
		when(movieMapper.toMovieDetailResponse(projection)).thenReturn(response);

		MovieDetailResponse result = movieService.getMovieById(MOVIE_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getMovieById_NotFound_ThrowsException() {
		when(movieRepository.findDetailProjectionById(MOVIE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovieById(MOVIE_ID)).isInstanceOf(MovieNotFoundException.class);
	}

	@Test
	void getMovieBySlug_Success() {
		Movie movie = new Movie();
		movie.setId(MOVIE_ID);
		movie.setTitle(MOVIE_TITLE);

		MovieDetailResponse response = new MovieDetailResponse();

		when(movieRepository.findBySlug(SLUG)).thenReturn(Optional.of(movie));
		when(movieMapper.toMovieDetailResponse(movie)).thenReturn(response);

		MovieDetailResponse result = movieService.getMovieBySlug(SLUG);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getMoviePoster_Success() {
		Movie movie = new Movie();
		movie.setId(MOVIE_ID);
		movie.setPosterFileName("poster.jpg");

		when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

		movieService.getMoviePoster(MOVIE_ID);

		verify(posterService).getPosterResponse("poster.jpg");
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
				return null;
			}

			@Override
			public String getDescription() {
				return null;
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
				return MovieStatus.CURRENT;
			}

			@Override
			public String getPosterFileName() {
				return "poster.jpg";
			}
		};
	}
}