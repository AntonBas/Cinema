package ua.lviv.bas.cinema.mapper.cinema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ua.lviv.bas.cinema.domain.cinema.*;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.*;
import ua.lviv.bas.cinema.dto.session.response.SessionMovieInfoResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieCardProjection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {MovieMapperImpl.class, PersonMapperImpl.class, GenreMapperImpl.class,
        SessionMapperImpl.class})
public class MovieMapperTest {

    @Autowired
    private MovieMapper mapper;
    @MockitoBean
    private PersonMapper personMapper;
    @MockitoBean
    private GenreMapper genreMapper;
    @MockitoBean
    private SessionMapper sessionMapper;

    private Movie movie;
    private MovieCardProjection cardProjection;

    @BeforeEach
    void setUp() {
        Genre genre = Genre.builder().id(1L).name("Action").build();
        Person actor = Person.builder().id(1L).name("Actor").build();
        Person director = Person.builder().id(2L).name("Director").build();
        Person screenwriter = Person.builder().id(3L).name("Screenwriter").build();

        movie = Movie.builder().id(1L).title("Test Movie").slug("test-movie")
                .trailerUrl("https://youtube.com/watch?v=123").description("Description").durationMinutes(120)
                .releaseDate(LocalDate.of(2024, 1, 15)).endShowingDate(LocalDate.of(2024, 3, 15))
                .ageRating(AgeRating.PEGI_12).status(MovieStatus.CURRENT).posterFileName("poster.jpg")
                .genres(Set.of(genre)).actors(Set.of(actor)).directors(Set.of(director))
                .screenwriters(Set.of(screenwriter)).sessions(new HashSet<>()).build();

        Session session = Session.builder().id(1L).startTime(LocalDateTime.now()).basePrice(BigDecimal.valueOf(150))
                .movie(movie).hall(CinemaHall.builder().id(1L).name("Hall A").build()).build();
        movie.getSessions().add(session);

        cardProjection = createCardProjection();

        when(genreMapper.toGenreResponse(any())).thenReturn(new GenreResponse(1L, "Action"));
        when(personMapper.toPersonResponse(any())).thenReturn(new PersonResponse(1L, "Name", null));
        when(sessionMapper.toSessionMovieInfoResponse(any())).thenReturn(new SessionMovieInfoResponse(1L,
                LocalDateTime.now(), LocalDateTime.now(), BigDecimal.valueOf(150), 105, "Hall A"));
    }

    private MovieCardProjection createCardProjection() {
        return new MovieCardProjection() {
            public Long getId() {
                return 1L;
            }

            public String getSlug() {
                return "test-movie";
            }

            public String getTitle() {
                return "Test Movie";
            }

            public String getPosterFileName() {
                return "poster.jpg";
            }

            public Integer getDurationMinutes() {
                return 120;
            }

            public AgeRating getAgeRating() {
                return AgeRating.PEGI_12;
            }

            public MovieStatus getStatus() {
                return MovieStatus.CURRENT;
            }

            public LocalDate getReleaseDate() {
                return LocalDate.of(2024, 1, 15);
            }

            public LocalDate getEndShowingDate() {
                return LocalDate.of(2024, 3, 15);
            }
        };
    }

    @Nested
    class ToMovieCardResponseTests {
        @Test
        void fromMovieShouldMapAllFields() {
            var response = mapper.toMovieCardResponse(movie);
            assertThat(response).returns(1L, MovieCardResponse::id).returns("test-movie", MovieCardResponse::slug)
                    .returns("/api/movies/1/poster", MovieCardResponse::posterUrl);
        }

        @Test
        void fromProjectionShouldMapAllFields() {
            var response = mapper.toMovieCardResponse(cardProjection);
            assertThat(response).returns(1L, MovieCardResponse::id).returns("test-movie", MovieCardResponse::slug);
        }

        @Test
        void withNullShouldReturnNull() {
            assertThat(mapper.toMovieCardResponse((Movie) null)).isNull();
            assertThat(mapper.toMovieCardResponse((MovieCardProjection) null)).isNull();
        }
    }

    @Nested
    class ToMovieDetailResponseTests {
        @Test
        void fromMovieShouldMapAllFieldsIncludingCollections() {
            var response = mapper.toMovieDetailResponse(movie);
            assertThat(response).returns(1L, MovieDetailResponse::id).returns("/api/movies/1/poster",
                    MovieDetailResponse::posterUrl);
            assertThat(response.genres()).hasSize(1);
            assertThat(response.actors()).hasSize(1);
            assertThat(response.sessions()).hasSize(1);
        }

        @Test
        void withEmptyCollectionsShouldMapEmptyLists() {
            var emptyMovie = Movie.builder().id(1L).title("Empty").slug("empty").trailerUrl("url").description("desc")
                    .durationMinutes(120).releaseDate(LocalDate.now()).endShowingDate(LocalDate.now())
                    .ageRating(AgeRating.PEGI_3).status(MovieStatus.CURRENT).posterFileName("poster.jpg")
                    .genres(new HashSet<>()).actors(new HashSet<>()).directors(new HashSet<>())
                    .screenwriters(new HashSet<>()).sessions(new HashSet<>()).build();
            var response = mapper.toMovieDetailResponse(emptyMovie);
            assertThat(response.genres()).isEmpty();
            assertThat(response.sessions()).isEmpty();
        }

        @Test
        void withNullShouldReturnNull() {
            assertThat(mapper.toMovieDetailResponse(null)).isNull();
        }
    }

    @Nested
    class ToMovieAdminResponseTests {
        @Test
        void shouldMapAllFields() {
            var response = mapper.toMovieAdminResponse(movie);
            assertThat(response).returns(1L, MovieAdminResponse::id).returns("/api/movies/1/poster",
                    MovieAdminResponse::posterUrl);
            assertThat(response.genres()).hasSize(1);
        }

        @Test
        void withNullShouldReturnNull() {
            assertThat(mapper.toMovieAdminResponse(null)).isNull();
        }
    }

    @Nested
    class ToMovieSessionSearchResponseTests {
        @Test
        void fromCardProjectionShouldMapFields() {
            var response = mapper.toMovieSessionSearchResponse(cardProjection);
            assertThat(response).returns(1L, MovieSessionSearchResponse::id);
        }

        @Test
        void withNullShouldReturnNull() {
            assertThat(mapper.toMovieSessionSearchResponse(null)).isNull();
        }
    }

    @Nested
    class ToMovieTests {
        @Test
        void shouldMapCreateRequestIgnoringRelations() {
            var request = MovieCreateRequest.builder().title("New").trailerUrl("url").description("desc")
                    .durationMinutes(120).releaseDate(LocalDate.now()).endShowingDate(LocalDate.now().plusDays(1))
                    .ageRating(AgeRating.PEGI_12).genreIds(List.of(1L)).actorIds(List.of(1L)).build();
            var result = mapper.toMovie(request);
            assertThat(result).returns(null, Movie::getId).returns(null, Movie::getSlug).returns("New",
                    Movie::getTitle);
            assertThat(result.getGenres()).isEmpty();
        }

        @Test
        void withNullShouldReturnNull() {
            assertThat(mapper.toMovie(null)).isNull();
        }
    }

    @Nested
    class UpdateMovieFromRequestTests {
        @Test
        void shouldUpdateNonNullFields() {
            var movie = Movie.builder().id(1L).title("Old").description("Old").durationMinutes(100).build();
            var request = MovieUpdateRequest.builder().title("New").description("New").durationMinutes(120).build();
            mapper.updateMovieFromRequest(request, movie);
            assertThat(movie).returns("New", Movie::getTitle).returns(120, Movie::getDurationMinutes);
        }

        @Test
        void shouldIgnoreNullFields() {
            var movie = Movie.builder().id(1L).title("Old").description("Old").durationMinutes(100).build();
            var request = MovieUpdateRequest.builder().title(null).description("New").durationMinutes(null).build();
            mapper.updateMovieFromRequest(request, movie);
            assertThat(movie).returns("Old", Movie::getTitle).returns(100, Movie::getDurationMinutes);
        }

        @Test
        void shouldNotChangeIdSlugStatusPosterFileName() {
            var movie = Movie.builder().id(1L).slug("slug").status(MovieStatus.CURRENT).posterFileName("poster.jpg")
                    .title("Old").build();
            mapper.updateMovieFromRequest(MovieUpdateRequest.builder().title("New").build(), movie);
            assertThat(movie).returns(1L, Movie::getId).returns("slug", Movie::getSlug)
                    .returns(MovieStatus.CURRENT, Movie::getStatus).returns("poster.jpg", Movie::getPosterFileName);
        }

        @Test
        void withNullRequestShouldNotChange() {
            var movie = Movie.builder().title("Old").build();
            mapper.updateMovieFromRequest(null, movie);
            assertThat(movie.getTitle()).isEqualTo("Old");
        }
    }

    @Test
    void getPosterUrlShouldReturnCorrectUrlOrNull() {
        assertThat(mapper.getPosterUrl(1L)).isEqualTo("/api/movies/1/poster");
        assertThat(mapper.getPosterUrl(null)).isNull();
    }
}