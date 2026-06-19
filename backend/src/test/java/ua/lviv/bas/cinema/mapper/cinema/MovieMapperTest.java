package ua.lviv.bas.cinema.mapper.cinema;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class MovieMapperTest {

    private final MovieMapper mapper = Mappers.getMapper(MovieMapper.class);

    @Test
    void toMovieCardResponse() {
        var movie = createMovie();
        var response = mapper.toMovieCardResponse(movie);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.slug()).isEqualTo("test-movie");
        assertThat(response.title()).isEqualTo("Test Movie");
        assertThat(response.posterUrl()).isEqualTo("/api/movies/1/poster");
        assertThat(response.durationMinutes()).isEqualTo(120);
        assertThat(response.ageRating()).isEqualTo(AgeRating.PEGI_12);
        assertThat(response.status()).isEqualTo(MovieStatus.CURRENT);
    }

    @Test
    void toMovieCardResponseWithNull() {
        assertThat(mapper.toMovieCardResponse(null)).isNull();
    }

    @Test
    void toMovieSessionSearchResponse() {
        var movie = createMovie();
        var response = mapper.toMovieSessionSearchResponse(movie);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Test Movie");
        assertThat(response.durationMinutes()).isEqualTo(120);
    }

    @Test
    void toMovieSessionSearchResponseWithNull() {
        assertThat(mapper.toMovieSessionSearchResponse(null)).isNull();
    }

    @Test
    void toMovieFromCreateRequest() {
        var request = MovieCreateRequest.builder().title("New").trailerUrl("url").description("desc")
                .durationMinutes(120).releaseDate(LocalDate.now()).endShowingDate(LocalDate.now().plusDays(1))
                .ageRating(AgeRating.PEGI_12).genreIds(List.of(1L)).actorIds(List.of(1L)).build();
        var result = mapper.toMovie(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isNull();
        assertThat(result.getSlug()).isNull();
        assertThat(result.getTitle()).isEqualTo("New");
        assertThat(result.getGenres()).isEmpty();
    }

    @Test
    void toMovieWithNullRequest() {
        assertThat(mapper.toMovie(null)).isNull();
    }

    @Test
    void updateMovieFromRequestShouldUpdateNonNullFields() {
        var movie = Movie.builder().id(1L).title("Old").description("Old").durationMinutes(100).build();
        var request = MovieUpdateRequest.builder().title("New").description("New").durationMinutes(120).build();
        mapper.updateMovieFromRequest(request, movie);

        assertThat(movie.getTitle()).isEqualTo("New");
        assertThat(movie.getDurationMinutes()).isEqualTo(120);
    }

    @Test
    void updateMovieFromRequestShouldIgnoreNullFields() {
        var movie = Movie.builder().id(1L).title("Old").description("Old").durationMinutes(100).build();
        var request = MovieUpdateRequest.builder().title(null).description("New").durationMinutes(null).build();
        mapper.updateMovieFromRequest(request, movie);

        assertThat(movie.getTitle()).isEqualTo("Old");
        assertThat(movie.getDurationMinutes()).isEqualTo(100);
    }

    @Test
    void updateMovieFromRequestShouldNotChangeIdSlugStatusPosterFileName() {
        var movie = Movie.builder().id(1L).slug("slug").status(MovieStatus.CURRENT).posterFileName("poster.jpg")
                .title("Old").build();
        mapper.updateMovieFromRequest(MovieUpdateRequest.builder().title("New").build(), movie);

        assertThat(movie.getId()).isEqualTo(1L);
        assertThat(movie.getSlug()).isEqualTo("slug");
        assertThat(movie.getStatus()).isEqualTo(MovieStatus.CURRENT);
        assertThat(movie.getPosterFileName()).isEqualTo("poster.jpg");
    }

    @Test
    void updateMovieFromRequestWithNullRequest() {
        var movie = Movie.builder().title("Old").build();
        mapper.updateMovieFromRequest(null, movie);

        assertThat(movie.getTitle()).isEqualTo("Old");
    }

    @Test
    void getPosterUrlShouldReturnCorrectUrl() {
        assertThat(mapper.getPosterUrl(1L)).isEqualTo("/api/movies/1/poster");
    }

    @Test
    void getPosterUrlWithNullShouldReturnNull() {
        assertThat(mapper.getPosterUrl(null)).isNull();
    }

    private Movie createMovie() {
        return Movie.builder().id(1L).title("Test Movie").slug("test-movie")
                .trailerUrl("https://youtube.com/watch?v=123").description("Description").durationMinutes(120)
                .releaseDate(LocalDate.of(2024, 1, 15)).endShowingDate(LocalDate.of(2024, 3, 15))
                .ageRating(AgeRating.PEGI_12).status(MovieStatus.CURRENT).posterFileName("poster.jpg")
                .genres(new HashSet<>()).actors(new HashSet<>()).directors(new HashSet<>())
                .screenwriters(new HashSet<>()).sessions(new HashSet<>()).build();
    }
}