package ua.lviv.bas.cinema.movie.service;

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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;
import ua.lviv.bas.cinema.common.CinemaTime;
import ua.lviv.bas.cinema.movie.domain.Genre;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.Person;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.enums.PersonRole;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.dto.request.MovieCreateRequest;
import ua.lviv.bas.cinema.movie.dto.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.movie.dto.response.MovieAdminResponse;
import ua.lviv.bas.cinema.movie.dto.response.MovieCardResponse;
import ua.lviv.bas.cinema.movie.dto.response.MovieDetailResponse;
import ua.lviv.bas.cinema.movie.dto.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieValidationException;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieHasSessionsException;
import ua.lviv.bas.cinema.movie.mapper.MovieMapper;
import ua.lviv.bas.cinema.movie.repository.GenreRepository;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.movie.repository.PersonRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.movie.repository.specification.MovieSpecification;
import ua.lviv.bas.cinema.audit.service.AuditService;
import ua.lviv.bas.cinema.integration.PosterService;
import org.springframework.http.MediaType;
import ua.lviv.bas.cinema.integration.PosterImage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private MovieStatusCalculator movieStatusCalculator;
    @Mock
    private PosterService posterService;
    @Mock
    private AuditService auditService;
    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private MovieSpecification movieSpecification;

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
        movie.setReleaseDate(CinemaTime.today().plusDays(1));
        movie.setEndShowingDate(CinemaTime.today().plusDays(30));
        movie.setAgeRating(AgeRating.PEGI_12);
        movie.setStatus(MovieStatus.UPCOMING);
        movie.setPosterFileName("poster.jpg");
        movie.setSessions(new HashSet<>());

        adminResponse = new MovieAdminResponse(MOVIE_ID, MOVIE_TITLE, "trailer.mp4", "Description", 120,
                CinemaTime.today().plusDays(1), CinemaTime.today().plusDays(30), AgeRating.PEGI_12, MovieStatus.UPCOMING,
                "/api/movies/1/poster", List.of(), List.of(), List.of(), List.of());

        detailResponse = new MovieDetailResponse(MOVIE_ID, MOVIE_TITLE, SLUG, "trailer.mp4", "Description", 120,
                CinemaTime.today().plusDays(1), CinemaTime.today().plusDays(30), AgeRating.PEGI_12, MovieStatus.UPCOMING,
                "/api/movies/1/poster", List.of(), List.of(), List.of(), List.of(), List.of());

        cardResponse = new MovieCardResponse(MOVIE_ID, SLUG, MOVIE_TITLE, "/api/movies/1/poster", 120,
                AgeRating.PEGI_12, MovieStatus.UPCOMING);

        createRequest = MovieCreateRequest.builder().title(MOVIE_TITLE).trailerUrl("trailer.mp4")
                .description("Description").durationMinutes(120).releaseDate(CinemaTime.today().plusDays(1))
                .endShowingDate(CinemaTime.today().plusDays(30)).ageRating(AgeRating.PEGI_12).genreIds(List.of(1L, 2L))
                .actorIds(List.of(3L, 4L)).directorIds(List.of(5L)).screenwriterIds(List.of(6L))
                .posterFile(mock(MultipartFile.class)).build();

        updateRequest = MovieUpdateRequest.builder().title("Updated Title").trailerUrl("new-trailer.mp4")
                .description("New Description").durationMinutes(130).releaseDate(CinemaTime.today().plusDays(2))
                .endShowingDate(CinemaTime.today().plusDays(40)).ageRating(AgeRating.PEGI_16).genreIds(List.of(1L))
                .actorIds(List.of(3L)).directorIds(List.of(5L, 7L)).screenwriterIds(List.of(6L, 8L)).removePoster(false)
                .build();

        lenient().when(movieSpecification.forMovies(any(), any())).thenReturn((root, query, cb) -> cb.conjunction());
        lenient().when(movieSpecification.currentMovies()).thenReturn((root, query, cb) -> cb.conjunction());
        lenient().when(movieSpecification.upcomingMovies()).thenReturn((root, query, cb) -> cb.conjunction());
        lenient().when(movieSpecification.leavingSoonMovies()).thenReturn((root, query, cb) -> cb.conjunction());
        lenient().when(movieSpecification.byDate(any())).thenReturn((root, query, cb) -> cb.conjunction());
        lenient().when(movieSpecification.byDateAndTitle(any(), any())).thenReturn((root, query, cb) -> cb.conjunction());
        lenient().when(movieSpecification.forPublicListing(any())).thenReturn((root, query, cb) -> cb.conjunction());
    }

    @Test
    void createMovieShouldSucceed() {
        when(slugService.generateUniqueSlug(MOVIE_TITLE, null)).thenReturn(SLUG);
        when(movieMapper.toEntity(createRequest)).thenReturn(movie);
        when(movieStatusCalculator.calculate(any(Movie.class), any(LocalDate.class)))
                .thenReturn(MovieStatus.UPCOMING);
        when(posterService.uploadPoster(any())).thenReturn("poster.jpg");
        doNothing().when(auditService).logChange(anyString(), any(), anyString(), any(), any(), any());

        List<Genre> genres = genres(1L, 2L);
        List<Person> actors = persons(PersonRole.ACTOR, 3L, 4L);
        List<Person> directors = persons(PersonRole.DIRECTOR, 5L);
        List<Person> screenwriters = persons(PersonRole.SCREENWRITER, 6L);

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
    void getMovieShouldSucceed() {
        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(movieMapper.toMovieAdminResponse(movie)).thenReturn(adminResponse);

        MovieAdminResponse result = movieService.getMovie(MOVIE_ID);

        assertThat(result).isEqualTo(adminResponse);
    }

    @Test
    void getMovieWhenNotFoundShouldThrowException() {
        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovie(MOVIE_ID)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getMovieBySlugShouldSucceed() {
        when(movieRepository.findMovieBySlug(SLUG)).thenReturn(Optional.of(movie));
        when(movieRepository.findUpcomingSessionsByMovieSlug(eq(SLUG), any(LocalDateTime.class))).thenReturn(new ArrayList<>());
        when(movieMapper.toMovieDetailResponse(movie)).thenReturn(detailResponse);

        MovieDetailResponse result = movieService.getMovieBySlug(SLUG);

        assertThat(result).isEqualTo(detailResponse);
    }

    @Test
    void getMovieBySlugWhenArchivedShouldThrowException() {
        movie.setStatus(MovieStatus.ARCHIVED);
        when(movieRepository.findMovieBySlug(SLUG)).thenReturn(Optional.of(movie));

        assertThatThrownBy(() -> movieService.getMovieBySlug(SLUG)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getMovieBySlugWhenNotFoundShouldThrowException() {
        when(movieRepository.findMovieBySlug(SLUG)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.getMovieBySlug(SLUG)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getMoviesShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Movie> moviePage = new PageImpl<>(List.of(movie));

        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(moviePage);
        when(movieMapper.toMovieCardResponse(movie)).thenReturn(cardResponse);

        Page<MovieCardResponse> result = movieService.getMovies(MOVIE_TITLE, MovieStatus.UPCOMING, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(cardResponse);
    }

    @Test
    void getCurrentMoviesShouldReturnList() {
        Pageable pageable = PageRequest.of(0, 6);
        Page<Movie> moviePage = new PageImpl<>(List.of(movie));

        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(moviePage);
        when(movieMapper.toMovieCardResponse(movie)).thenReturn(cardResponse);

        List<MovieCardResponse> result = movieService.getCurrentMovies(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(cardResponse);
    }

    @Test
    void getUpcomingMoviesShouldReturnList() {
        Pageable pageable = PageRequest.of(0, 6);
        Page<Movie> moviePage = new PageImpl<>(List.of(movie));

        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(moviePage);
        when(movieMapper.toMovieCardResponse(movie)).thenReturn(cardResponse);

        List<MovieCardResponse> result = movieService.getUpcomingMovies(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(cardResponse);
    }

    @Test
    void getLeavingSoonMoviesShouldReturnList() {
        Pageable pageable = PageRequest.of(0, 6);
        Page<Movie> moviePage = new PageImpl<>(List.of(movie));

        when(movieRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(moviePage);
        when(movieMapper.toMovieCardResponse(movie)).thenReturn(cardResponse);

        List<MovieCardResponse> result = movieService.getLeavingSoonMovies(pageable);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst()).isEqualTo(cardResponse);
    }

    @Test
    void updateMovieShouldSucceed() {
        Movie existingMovie = new Movie();
        existingMovie.setId(MOVIE_ID);
        existingMovie.setTitle("Old Title");
        existingMovie.setSlug("old-title");
        existingMovie.setReleaseDate(CinemaTime.today().plusDays(1));
        existingMovie.setEndShowingDate(CinemaTime.today().plusDays(30));
        existingMovie.setSessions(new HashSet<>());

        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(existingMovie));
        when(movieRepository.existsByTitle("Updated Title")).thenReturn(false);
        when(movieStatusCalculator.calculate(any(Movie.class), any(LocalDate.class)))
                .thenReturn(MovieStatus.UPCOMING);
        doNothing().when(auditService).logChange(anyString(), any(), anyString(), any(), any(), any());

        List<Genre> genres = genres(1L);
        List<Person> actors = persons(PersonRole.ACTOR, 3L);
        List<Person> directors = persons(PersonRole.DIRECTOR, 5L, 7L);
        List<Person> screenwriters = persons(PersonRole.SCREENWRITER, 6L, 8L);

        when(genreRepository.findAllById(updateRequest.getGenreIds())).thenReturn(genres);
        when(personRepository.findAllById(updateRequest.getActorIds())).thenReturn(actors);
        when(personRepository.findAllById(updateRequest.getDirectorIds())).thenReturn(directors);
        when(personRepository.findAllById(updateRequest.getScreenwriterIds())).thenReturn(screenwriters);

        when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
        when(movieMapper.toMovieAdminResponse(existingMovie)).thenReturn(adminResponse);

        MovieAdminResponse result = movieService.updateMovie(MOVIE_ID, updateRequest);

        assertThat(result).isEqualTo(adminResponse);
        verify(movieMapper).updateEntity(updateRequest, existingMovie);
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
        existingMovie.setReleaseDate(CinemaTime.today().plusDays(1));
        existingMovie.setEndShowingDate(CinemaTime.today().plusDays(30));
        existingMovie.setSessions(new HashSet<>());

        MovieUpdateRequest sameTitleRequest = MovieUpdateRequest.builder().title("Same Title")
                .releaseDate(CinemaTime.today().plusDays(2)).endShowingDate(CinemaTime.today().plusDays(40))
                .genreIds(List.of(1L)).actorIds(List.of(1L)).directorIds(List.of(2L)).screenwriterIds(List.of(3L))
                .build();

        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(existingMovie));
        when(movieStatusCalculator.calculate(any(Movie.class), any(LocalDate.class)))
                .thenReturn(MovieStatus.UPCOMING);
        doNothing().when(auditService).logChange(anyString(), any(), anyString(), any(), any(), any());
        when(genreRepository.findAllById(List.of(1L))).thenReturn(genres(1L));
        when(personRepository.findAllById(List.of(1L))).thenReturn(persons(PersonRole.ACTOR, 1L));
        when(personRepository.findAllById(List.of(2L))).thenReturn(persons(PersonRole.DIRECTOR, 2L));
        when(personRepository.findAllById(List.of(3L))).thenReturn(persons(PersonRole.SCREENWRITER, 3L));
        when(movieRepository.save(existingMovie)).thenReturn(existingMovie);
        when(movieMapper.toMovieAdminResponse(existingMovie)).thenReturn(adminResponse);

        movieService.updateMovie(MOVIE_ID, sameTitleRequest);

        verify(slugService, never()).generateUniqueSlug(anyString(), any());
    }

    @Test
    void updateMovieWithRemovedPosterAndNoReplacementShouldThrow() {
        movie.setTitle("Updated Title");
        updateRequest.setRemovePoster(true);
        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
        stubUpdateRelations();

        assertThatThrownBy(() -> movieService.updateMovie(MOVIE_ID, updateRequest))
                .isInstanceOf(MovieValidationException.class).hasMessageContaining("poster");
        verify(posterService, never()).deletePoster(anyString());
        verify(movieRepository, never()).save(any());
    }

    @Test
    void updateMovieWithPersonOfWrongRoleShouldThrow() {
        movie.setTitle("Updated Title");
        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(genreRepository.findAllById(updateRequest.getGenreIds())).thenReturn(genres(1L));
        when(personRepository.findAllById(updateRequest.getActorIds())).thenReturn(persons(PersonRole.DIRECTOR, 3L));

        assertThatThrownBy(() -> movieService.updateMovie(MOVIE_ID, updateRequest))
                .isInstanceOf(MovieValidationException.class);
        verify(movieRepository, never()).save(any());
    }

    @Test
    void updateMovieWithUnknownGenreShouldThrowNotFound() {
        movie.setTitle("Updated Title");
        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(genreRepository.findAllById(updateRequest.getGenreIds())).thenReturn(List.of());

        assertThatThrownBy(() -> movieService.updateMovie(MOVIE_ID, updateRequest))
                .isInstanceOf(EntityNotFoundException.class);
        verify(movieRepository, never()).save(any());
    }

    @Test
    void updateMovieDurationWithActiveSessionsShouldThrow() {
        movie.setTitle("Updated Title");
        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(sessionRepository.findFirstActiveSessionStart(MOVIE_ID)).thenReturn(CinemaTime.now().plusDays(5));

        assertThatThrownBy(() -> movieService.updateMovie(MOVIE_ID, updateRequest))
                .isInstanceOf(MovieValidationException.class).hasMessageContaining("duration");
        verify(movieRepository, never()).save(any());
    }

    @Test
    void updateMovieEndingBeforeLastActiveSessionShouldThrow() {
        movie.setTitle("Updated Title");
        movie.setDurationMinutes(updateRequest.getDurationMinutes());
        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(sessionRepository.findFirstActiveSessionStart(MOVIE_ID)).thenReturn(CinemaTime.now().plusDays(5));
        when(sessionRepository.findLastActiveSessionStart(MOVIE_ID)).thenReturn(CinemaTime.now().plusDays(60));

        assertThatThrownBy(() -> movieService.updateMovie(MOVIE_ID, updateRequest))
                .isInstanceOf(MovieValidationException.class).hasMessageContaining("showing period");
        verify(movieRepository, never()).save(any());
    }

    private void stubUpdateRelations() {
        when(genreRepository.findAllById(updateRequest.getGenreIds())).thenReturn(genres(1L));
        when(personRepository.findAllById(updateRequest.getActorIds())).thenReturn(persons(PersonRole.ACTOR, 3L));
        when(personRepository.findAllById(updateRequest.getDirectorIds()))
                .thenReturn(persons(PersonRole.DIRECTOR, 5L, 7L));
        when(personRepository.findAllById(updateRequest.getScreenwriterIds()))
                .thenReturn(persons(PersonRole.SCREENWRITER, 6L, 8L));
    }

    @Test
    void deleteMovieWithPosterShouldDeletePoster() {
        movie.setPosterFileName("poster.jpg");

        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(sessionRepository.countByMovieId(MOVIE_ID)).thenReturn(0L);
        doNothing().when(auditService).logChange(anyString(), any(), anyString(), any(), any(), any());

        movieService.deleteMovie(MOVIE_ID);

        verify(posterService).deletePoster("poster.jpg");
        verify(movieRepository).delete(movie);
    }

    @Test
    void deleteMovieWithoutPosterShouldNotDeletePoster() {
        movie.setPosterFileName(null);

        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(sessionRepository.countByMovieId(MOVIE_ID)).thenReturn(0L);
        doNothing().when(auditService).logChange(anyString(), any(), anyString(), any(), any(), any());

        movieService.deleteMovie(MOVIE_ID);

        verify(posterService, never()).deletePoster(anyString());
        verify(movieRepository).delete(movie);
    }

    @Test
    void deleteMovieWithSessionsShouldThrowException() {
        movie.setPosterFileName("poster.jpg");

        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.of(movie));
        when(sessionRepository.countByMovieId(MOVIE_ID)).thenReturn(3L);

        assertThatThrownBy(() -> movieService.deleteMovie(MOVIE_ID)).isInstanceOf(MovieHasSessionsException.class);

        verify(posterService, never()).deletePoster(anyString());
        verify(movieRepository, never()).delete((Movie) any());
    }

    @Test
    void deleteMovieWhenNotFoundShouldThrowException() {
        when(movieRepository.findMovieById(MOVIE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movieService.deleteMovie(MOVIE_ID)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void searchMoviesByTitleShouldReturnList() {
        when(movieRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(movie));
        when(movieMapper.toMovieSessionSearchResponse(movie))
                .thenReturn(new MovieSessionSearchResponse(MOVIE_ID, MOVIE_TITLE, 120));

        List<MovieSessionSearchResponse> result = movieService.searchMovies("test", null);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(MOVIE_ID);
        assertThat(result.getFirst().title()).isEqualTo(MOVIE_TITLE);
    }

    @Test
    void searchMoviesByDateShouldReturnList() {
        LocalDate date = CinemaTime.today();
        when(movieRepository.findAll(any(Specification.class), any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(movie));
        when(movieMapper.toMovieSessionSearchResponse(movie))
                .thenReturn(new MovieSessionSearchResponse(MOVIE_ID, MOVIE_TITLE, 120));

        List<MovieSessionSearchResponse> result = movieService.searchMovies(null, date);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().id()).isEqualTo(MOVIE_ID);
    }

    @Test
    void searchMoviesWithNullQueryAndNullDateShouldReturnEmptyList() {
        List<MovieSessionSearchResponse> result = movieService.searchMovies(null, null);
        assertThat(result).isEmpty();
    }

    @Test
    void searchMoviesWithBlankQueryShouldReturnEmptyList() {
        List<MovieSessionSearchResponse> result = movieService.searchMovies("   ", null);
        assertThat(result).isEmpty();
    }

    @Test
    void getPosterShouldReturnResponse() {
        when(movieRepository.findPosterFileNameById(MOVIE_ID)).thenReturn(Optional.of("poster.jpg"));
        var poster = new PosterImage(new byte[0], MediaType.IMAGE_JPEG);
        when(posterService.loadPoster("poster.jpg")).thenReturn(Optional.of(poster));

        assertThat(movieService.getPoster(MOVIE_ID)).contains(poster);
    }

    @Test
    void getPosterWhenNotFoundShouldReturnNotFound() {
        when(movieRepository.findPosterFileNameById(MOVIE_ID)).thenReturn(Optional.empty());

        assertThat(movieService.getPoster(MOVIE_ID)).isEmpty();
    }

    private List<Genre> genres(Long... ids) {
        return Arrays.stream(ids).map(id -> Genre.builder().id(id).build()).toList();
    }

    private List<Person> persons(PersonRole role, Long... ids) {
        return Arrays.stream(ids).map(id -> Person.builder().id(id).role(role).build()).toList();
    }
}
