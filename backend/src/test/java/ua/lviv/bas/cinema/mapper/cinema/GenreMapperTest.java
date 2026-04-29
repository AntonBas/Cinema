package ua.lviv.bas.cinema.mapper.cinema;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.lviv.bas.cinema.domain.cinema.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreListResponse;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.GenreListProjection;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class GenreMapperTest {

    private final GenreMapper mapper = Mappers.getMapper(GenreMapper.class);

    @Test
    void toGenreListResponseFromProjectionShouldMapAllFields() {
        GenreListProjection projection = new GenreListProjection() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getName() {
                return "Comedy";
            }

            @Override
            public Integer getMovieCount() {
                return 5;
            }
        };

        GenreListResponse response = mapper.toGenreListResponse(projection);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Comedy");
        assertThat(response.movieCount()).isEqualTo(5);
    }

    @Test
    void toGenreListResponseFromProjectionWithNullMovieCountShouldMapNull() {
        GenreListProjection projection = new GenreListProjection() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getName() {
                return "Comedy";
            }

            @Override
            public Integer getMovieCount() {
                return null;
            }
        };

        GenreListResponse response = mapper.toGenreListResponse(projection);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Comedy");
        assertThat(response.movieCount()).isNull();
    }

    @Test
    void toGenreResponseShouldMapEntityToResponse() {
        Genre genre = Genre.builder().id(1L).name("Action").build();

        GenreResponse response = mapper.toGenreResponse(genre);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Action");
    }

    @Test
    void toGenreShouldMapRequestToEntity() {
        GenreRequest request = new GenreRequest("Drama");
        Genre genre = mapper.toGenre(request);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isNull();
        assertThat(genre.getName()).isEqualTo("Drama");
        assertThat(genre.getMovies()).isNotNull();
        assertThat(genre.getMovies()).isEmpty();
    }

    @Test
    void updateGenreFromRequestShouldUpdateOnlyNonNullFields() {
        Genre existing = Genre.builder().id(1L).name("Old").movies(new HashSet<>()).build();
        GenreRequest request = new GenreRequest("New");
        mapper.updateGenreFromRequest(request, existing);

        assertThat(existing.getId()).isEqualTo(1L);
        assertThat(existing.getName()).isEqualTo("New");
        assertThat(existing.getMovies()).isNotNull();
    }

    @Test
    void updateGenreFromRequestWithSameNameShouldNotChange() {
        Genre existing = Genre.builder().id(1L).name("Action").build();
        GenreRequest request = new GenreRequest("Action");
        mapper.updateGenreFromRequest(request, existing);

        assertThat(existing.getName()).isEqualTo("Action");
    }

    @Test
    void toGenreListResponseWithNullProjectionShouldReturnNull() {
        GenreListResponse response = mapper.toGenreListResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toGenreResponseWithNullEntityShouldReturnNull() {
        GenreResponse response = mapper.toGenreResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toGenreWithNullRequestShouldReturnNull() {
        Genre genre = mapper.toGenre(null);
        assertThat(genre).isNull();
    }

    @Test
    void updateGenreFromRequestWithNullRequestShouldNotChange() {
        Genre existing = Genre.builder().id(1L).name("Action").build();
        mapper.updateGenreFromRequest(null, existing);

        assertThat(existing.getName()).isEqualTo("Action");
    }
}