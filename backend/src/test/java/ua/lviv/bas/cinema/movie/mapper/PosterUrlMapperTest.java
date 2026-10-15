package ua.lviv.bas.cinema.movie.mapper;

import org.junit.jupiter.api.Test;
import ua.lviv.bas.cinema.integration.PosterService;
import ua.lviv.bas.cinema.movie.domain.Movie;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PosterUrlMapperTest {

    private final PosterService posterService = mock(PosterService.class);
    private final PosterUrlMapper posterUrlMapper = new PosterUrlMapper(posterService);

    @Test
    void shouldUseCdnUrlWhenStorageProvidesOne() {
        var movie = Movie.builder().id(1L).posterFileName("https://res.cloudinary.com/x/image/upload/p.jpg").build();
        when(posterService.publicPosterUrl(movie.getPosterFileName()))
                .thenReturn(Optional.of("https://res.cloudinary.com/x/image/upload/c_limit,w_600/p.jpg"));

        assertThat(posterUrlMapper.toPosterUrl(movie))
                .isEqualTo("https://res.cloudinary.com/x/image/upload/c_limit,w_600/p.jpg");
    }

    @Test
    void shouldFallBackToApiEndpointWithoutCdnUrl() {
        var movie = Movie.builder().id(1L).posterFileName("poster.jpg").build();
        when(posterService.publicPosterUrl("poster.jpg")).thenReturn(Optional.empty());

        assertThat(posterUrlMapper.toPosterUrl(movie)).isEqualTo("/api/movies/1/poster");
    }

    @Test
    void shouldReturnNullForUnsavedMovie() {
        assertThat(posterUrlMapper.toPosterUrl(Movie.builder().build())).isNull();
    }
}
