package ua.lviv.bas.cinema.movie.mapper;

import lombok.RequiredArgsConstructor;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;
import ua.lviv.bas.cinema.integration.PosterService;
import ua.lviv.bas.cinema.movie.domain.Movie;

@Component
@RequiredArgsConstructor
public class PosterUrlMapper {

    private final PosterService posterService;

    @Named("posterUrl")
    public String toPosterUrl(Movie movie) {
        return movie == null ? null : toPosterUrl(movie.getId(), movie.getPosterFileName());
    }

    public String toPosterUrl(Long movieId, String posterFileName) {
        if (movieId == null) {
            return null;
        }
        return posterService.publicPosterUrl(posterFileName).orElse("/api/movies/" + movieId + "/poster");
    }
}
