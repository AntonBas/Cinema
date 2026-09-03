package ua.lviv.bas.cinema.movie.service;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.movie.domain.Genre;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.GenreRepository;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class GenreServiceIntegrationTest {

    @Autowired
    private GenreService genreService;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private MovieRepository movieRepository;

    @Test
    void getGenresShouldReturnCorrectMovieCountPerGenre() {
        var genreWithMovies = genreRepository.save(Genre.builder().name("ZZTEST Genre With Movies").build());
        var genreWithoutMovies = genreRepository.save(Genre.builder().name("ZZTEST Genre Without Movies").build());

        movieRepository.save(buildMovie("zztest-genre-movie-1", Set.of(genreWithMovies)));
        movieRepository.save(buildMovie("zztest-genre-movie-2", Set.of(genreWithMovies)));
        movieRepository.save(buildMovie("zztest-genre-movie-3", Set.of(genreWithMovies)));

        var page = genreService.getGenres("ZZTEST Genre", PageRequest.of(0, 10));

        var withMovies = page.getContent().stream().filter(g -> g.id().equals(genreWithMovies.getId())).findFirst()
                .orElseThrow();
        var withoutMovies = page.getContent().stream().filter(g -> g.id().equals(genreWithoutMovies.getId()))
                .findFirst().orElseThrow();

        assertThat(withMovies.movieCount()).isEqualTo(3);
        assertThat(withoutMovies.movieCount()).isEqualTo(0);
    }

    private Movie buildMovie(String slug, Set<Genre> genres) {
        return Movie.builder().title("ZZTEST Movie " + slug).slug(slug).trailerUrl("https://example.com/trailer")
                .description("Test movie for genre regression test").durationMinutes(120)
                .releaseDate(LocalDate.now().minusDays(1)).endShowingDate(LocalDate.now().plusMonths(1))
                .status(MovieStatus.CURRENT).posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).genres(genres)
                .build();
    }
}
