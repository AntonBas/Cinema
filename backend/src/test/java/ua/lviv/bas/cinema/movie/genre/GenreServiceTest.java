package ua.lviv.bas.cinema.movie.genre;

import java.util.stream.IntStream;
import org.springframework.data.domain.Sort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.audit.service.AuditService;
import ua.lviv.bas.cinema.movie.genre.Genre;
import ua.lviv.bas.cinema.movie.genre.dto.GenreRequest;
import ua.lviv.bas.cinema.movie.genre.dto.GenreListResponse;
import ua.lviv.bas.cinema.movie.genre.dto.GenreResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreHasMoviesException;
import ua.lviv.bas.cinema.movie.genre.GenreMapper;
import ua.lviv.bas.cinema.movie.genre.GenreRepository;
import ua.lviv.bas.cinema.movie.movie.MovieRepository;
import ua.lviv.bas.cinema.movie.genre.GenreListProjection;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GenreServiceTest {

    @Mock
    private GenreRepository genreRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private GenreMapper genreMapper;
    @Mock
    private AuditService auditService;
    @InjectMocks
    private GenreService genreService;

    private final Long GENRE_ID = 1L;
    private final String GENRE_NAME = "Action";

    @Test
    void createGenreShouldSaveNewGenre() {
        GenreRequest request = new GenreRequest(GENRE_NAME);
        Genre genre = new Genre();
        genre.setId(GENRE_ID);
        genre.setName(GENRE_NAME);
        GenreResponse response = new GenreResponse(GENRE_ID, GENRE_NAME);

        when(genreRepository.existsByNameIgnoreCase(GENRE_NAME)).thenReturn(false);
        when(genreMapper.toEntity(request)).thenReturn(genre);
        when(genreRepository.save(genre)).thenReturn(genre);
        when(genreMapper.toGenreResponse(genre)).thenReturn(response);

        GenreResponse result = genreService.createGenre(request);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(GENRE_ID);
        assertThat(result.name()).isEqualTo(GENRE_NAME);
        verify(genreRepository).save(genre);
        verify(auditService).logChange(eq("Genre"), eq(GENRE_ID), eq(GENRE_NAME), eq(AuditAction.CREATED),
                isNull(), any());
    }

    @Test
    void createGenreShouldThrowExceptionWhenNameExists() {
        GenreRequest request = new GenreRequest(GENRE_NAME);
        when(genreRepository.existsByNameIgnoreCase(GENRE_NAME)).thenReturn(true);

        assertThatThrownBy(() -> genreService.createGenre(request))
                .isInstanceOf(DuplicateEntityException.class)
                .hasMessageContaining(GENRE_NAME);

        verify(genreRepository, never()).save(any());
    }

    @Test
    void getGenresShouldReturnPage() {
        String query = "act";
        Pageable pageable = PageRequest.of(0, 10);
        GenreListProjection projection = new TestGenreProjection(GENRE_ID, GENRE_NAME, 5);
        Page<GenreListProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);
        GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, 5);

        when(genreRepository.findGenresByFilters(query, pageable)).thenReturn(projectionPage);
        when(genreMapper.toGenreListResponse(projection)).thenReturn(response);

        Page<GenreListResponse> result = genreService.getGenres(query, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().id()).isEqualTo(GENRE_ID);
        assertThat(result.getContent().getFirst().name()).isEqualTo(GENRE_NAME);
        assertThat(result.getContent().getFirst().movieCount()).isEqualTo(5);
    }

    @Test
    void getGenresWithNullQueryShouldReturnAll() {
        Pageable pageable = PageRequest.of(0, 10);
        GenreListProjection projection = new TestGenreProjection(GENRE_ID, GENRE_NAME, 5);
        Page<GenreListProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);
        GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, 5);

        when(genreRepository.findGenresByFilters(null, pageable)).thenReturn(projectionPage);
        when(genreMapper.toGenreListResponse(projection)).thenReturn(response);

        Page<GenreListResponse> result = genreService.getGenres(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updateGenreShouldUpdateName() {
        Genre existingGenre = new Genre();
        existingGenre.setId(GENRE_ID);
        existingGenre.setName("Old Name");
        GenreRequest request = new GenreRequest(GENRE_NAME);
        GenreResponse response = new GenreResponse(GENRE_ID, GENRE_NAME);

        when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(existingGenre));
        when(genreRepository.existsByNameIgnoreCaseAndIdNot(GENRE_NAME, GENRE_ID)).thenReturn(false);
        when(genreRepository.save(existingGenre)).thenReturn(existingGenre);
        when(genreMapper.toGenreResponse(existingGenre)).thenReturn(response);

        GenreResponse result = genreService.updateGenre(GENRE_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(GENRE_NAME);
        verify(genreMapper).updateEntity(request, existingGenre);
        verify(genreRepository).save(existingGenre);
    }

    @Test
    void updateGenreShouldThrowExceptionWhenGenreNotFound() {
        GenreRequest request = new GenreRequest(GENRE_NAME);
        when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> genreService.updateGenre(GENRE_ID, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateGenreShouldThrowExceptionWhenNameExists() {
        Genre existingGenre = new Genre();
        existingGenre.setId(GENRE_ID);
        existingGenre.setName("Old Name");
        GenreRequest request = new GenreRequest(GENRE_NAME);

        when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(existingGenre));
        when(genreRepository.existsByNameIgnoreCaseAndIdNot(GENRE_NAME, GENRE_ID)).thenReturn(true);

        assertThatThrownBy(() -> genreService.updateGenre(GENRE_ID, request))
                .isInstanceOf(DuplicateEntityException.class);

        verify(genreRepository, never()).save(any());
    }

    @Test
    void deleteGenreShouldDeleteGenre() {
        Genre genre = new Genre();
        genre.setId(GENRE_ID);
        genre.setName(GENRE_NAME);

        when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(genre));
        when(movieRepository.countMovieUsageByGenreId(GENRE_ID)).thenReturn(0L);

        genreService.deleteGenre(GENRE_ID);

        verify(genreRepository).deleteById(GENRE_ID);
        verify(auditService).logChange(eq("Genre"), eq(GENRE_ID), eq(GENRE_NAME), eq(AuditAction.DELETED),
                any(), isNull());
    }

    @Test
    void deleteGenreShouldThrowExceptionWhenGenreNotFound() {
        when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> genreService.deleteGenre(GENRE_ID))
                .isInstanceOf(EntityNotFoundException.class);

        verify(genreRepository, never()).deleteById(any());
    }

    @Test
    void deleteGenreShouldThrowExceptionWhenGenreHasMovies() {
        Genre genre = new Genre();
        genre.setId(GENRE_ID);
        genre.setName(GENRE_NAME);

        when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(genre));
        when(movieRepository.countMovieUsageByGenreId(GENRE_ID)).thenReturn(5L);

        assertThatThrownBy(() -> genreService.deleteGenre(GENRE_ID))
                .isInstanceOf(GenreHasMoviesException.class);

        verify(genreRepository, never()).deleteById(any());
    }

    private record TestGenreProjection(Long getId, String getName,
                                       Integer getMovieCount) implements GenreListProjection {
        @Override
        public Long getId() {
            return getId;
        }

        @Override
        public String getName() {
            return getName;
        }

        @Override
        public Integer getMovieCount() {
            return getMovieCount;
        }
    }

    @Test
    void getAllGenresShouldReturnEveryGenreSortedByNameWithoutPaging() {
        var genres = IntStream.rangeClosed(1, 15).mapToObj(i -> Genre.builder().id((long) i).name("Genre " + i).build())
                .toList();
        when(genreRepository.findAll(Sort.by("name"))).thenReturn(genres);
        when(genreMapper.toGenreResponse(any(Genre.class)))
                .thenAnswer(invocation -> new GenreResponse(invocation.<Genre>getArgument(0).getId(),
                        invocation.<Genre>getArgument(0).getName()));

        List<GenreResponse> result = genreService.getAllGenres();

        assertThat(result).hasSize(15);
        assertThat(result.getFirst().name()).isEqualTo("Genre 1");
    }
}
