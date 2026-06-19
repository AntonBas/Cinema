package ua.lviv.bas.cinema.controller.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreListResponse;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.service.cinema.GenreService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminGenreControllerTest {

    @Mock
    private GenreService genreService;

    @InjectMocks
    private AdminGenreController controller;

    private final Long GENRE_ID = 1L;
    private final String GENRE_NAME = "Action";

    @Test
    void createGenreShouldReturnCreatedGenre() {
        GenreRequest request = new GenreRequest(GENRE_NAME);
        GenreResponse response = new GenreResponse(GENRE_ID, GENRE_NAME);

        when(genreService.createGenre(any(GenreRequest.class))).thenReturn(response);

        GenreResponse result = controller.createGenre(request);

        assertThat(result).isEqualTo(response);
        verify(genreService).createGenre(request);
    }

    @Test
    void createGenreShouldThrowExceptionWhenDuplicateName() {
        GenreRequest request = new GenreRequest("Existing");

        when(genreService.createGenre(any(GenreRequest.class)))
                .thenThrow(new DuplicateEntityException("Genre", "Existing"));

        assertThatThrownBy(() -> controller.createGenre(request)).isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    void getGenresShouldReturnPage() {
        String query = "act";
        Pageable pageable = PageRequest.of(0, 10);

        GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, 5);
        Page<GenreListResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        when(genreService.getGenres(query, pageable)).thenReturn(page);

        PageResponse<GenreListResponse> result = controller.getGenres(query, pageable);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        assertThat(result.content().getFirst().id()).isEqualTo(GENRE_ID);
        assertThat(result.content().getFirst().name()).isEqualTo(GENRE_NAME);
        verify(genreService).getGenres(query, pageable);
    }

    @Test
    void getGenresWithNullQueryShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);

        GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, 5);
        Page<GenreListResponse> page = new PageImpl<>(List.of(response), pageable, 1);

        when(genreService.getGenres(null, pageable)).thenReturn(page);

        PageResponse<GenreListResponse> result = controller.getGenres(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.content()).hasSize(1);
        verify(genreService).getGenres(null, pageable);
    }

    @Test
    void updateGenreShouldReturnUpdatedGenre() {
        GenreRequest request = new GenreRequest("Updated");
        GenreResponse response = new GenreResponse(GENRE_ID, "Updated");

        when(genreService.updateGenre(eq(GENRE_ID), any(GenreRequest.class))).thenReturn(response);

        GenreResponse result = controller.updateGenre(GENRE_ID, request);

        assertThat(result).isEqualTo(response);
        verify(genreService).updateGenre(GENRE_ID, request);
    }

    @Test
    void updateGenreShouldThrowExceptionWhenNotFound() {
        GenreRequest request = new GenreRequest("Updated");

        when(genreService.updateGenre(eq(999L), any(GenreRequest.class))).thenThrow(new GenreNotFoundException(999L));

        assertThatThrownBy(() -> controller.updateGenre(999L, request)).isInstanceOf(GenreNotFoundException.class);
    }

    @Test
    void deleteGenreShouldCallService() {
        controller.deleteGenre(GENRE_ID);

        verify(genreService).deleteGenre(GENRE_ID);
    }

    @Test
    void deleteGenreShouldThrowExceptionWhenNotFound() {
        doThrow(new GenreNotFoundException(999L)).when(genreService).deleteGenre(999L);

        assertThatThrownBy(() -> controller.deleteGenre(999L)).isInstanceOf(GenreNotFoundException.class);
    }
}