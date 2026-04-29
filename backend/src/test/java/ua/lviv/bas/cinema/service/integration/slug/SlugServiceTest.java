package ua.lviv.bas.cinema.service.integration.slug;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.exception.domain.technical.SlugGenerationException;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SlugServiceTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private SlugService slugService;

    @Test
    void generateSlugShouldGenerateValidSlugFromNormalTitle() {
        String result = slugService.generateSlug("Test Movie Title");
        assertThat(result).isEqualTo("test-movie-title");
    }

    @Test
    void generateSlugShouldRemoveSpecialCharacters() {
        String result = slugService.generateSlug("Test & Movie: Special@Characters!");
        assertThat(result).isEqualTo("test-movie-specialcharacters");
    }

    @Test
    void generateSlugShouldHandleAccentedCharacters() {
        String result = slugService.generateSlug("Café Crème Français");
        assertThat(result).isEqualTo("cafe-creme-francais");
    }

    @Test
    void generateSlugShouldHandleMultipleSpaces() {
        String result = slugService.generateSlug("Test    Movie   Title");
        assertThat(result).isEqualTo("test-movie-title");
    }

    @Test
    void generateSlugShouldHandleTrailingSpaces() {
        String result = slugService.generateSlug("  Test Movie Title  ");
        assertThat(result).isEqualTo("test-movie-title");
    }

    @Test
    void generateSlugShouldThrowExceptionWhenTitleIsNull() {
        assertThatThrownBy(() -> slugService.generateSlug(null))
                .isInstanceOf(SlugGenerationException.class)
                .hasMessage("Title cannot be null or empty");
    }

    @Test
    void generateSlugShouldThrowExceptionWhenTitleIsEmpty() {
        assertThatThrownBy(() -> slugService.generateSlug(""))
                .isInstanceOf(SlugGenerationException.class)
                .hasMessage("Title cannot be null or empty");
    }

    @Test
    void generateSlugShouldThrowExceptionWhenTitleIsBlank() {
        assertThatThrownBy(() -> slugService.generateSlug("   "))
                .isInstanceOf(SlugGenerationException.class)
                .hasMessage("Title cannot be null or empty");
    }

    @Test
    void generateUniqueSlugShouldReturnBaseSlugWhenNoConflict() {
        when(movieRepository.findBySlug("unique-movie-title")).thenReturn(Optional.empty());
        String result = slugService.generateUniqueSlug("Unique Movie Title");
        assertThat(result).isEqualTo("unique-movie-title");
    }

    @Test
    void generateUniqueSlugShouldAddCounterWhenSlugExists() {
        Movie existingMovie = new Movie();
        existingMovie.setId(1L);
        when(movieRepository.findBySlug("existing-movie")).thenReturn(Optional.of(existingMovie));
        when(movieRepository.findBySlug("existing-movie-1")).thenReturn(Optional.empty());
        String result = slugService.generateUniqueSlug("Existing Movie");
        assertThat(result).isEqualTo("existing-movie-1");
    }

    @Test
    void generateUniqueSlugShouldIncrementCounterWhenMultipleConflicts() {
        Movie movie1 = new Movie();
        movie1.setId(1L);
        Movie movie2 = new Movie();
        movie2.setId(2L);
        when(movieRepository.findBySlug("popular-movie")).thenReturn(Optional.of(movie1));
        when(movieRepository.findBySlug("popular-movie-1")).thenReturn(Optional.of(movie2));
        when(movieRepository.findBySlug("popular-movie-2")).thenReturn(Optional.empty());
        String result = slugService.generateUniqueSlug("Popular Movie");
        assertThat(result).isEqualTo("popular-movie-2");
    }

    @Test
    void isSlugAvailableForMovieShouldReturnTrueWhenSlugNotExists() {
        when(movieRepository.findBySlug("new-slug")).thenReturn(Optional.empty());
        boolean result = slugService.isSlugAvailableForMovie("new-slug", 1L);
        assertThat(result).isTrue();
    }

    @Test
    void isSlugAvailableForMovieShouldReturnTrueWhenSlugBelongsToSameMovie() {
        Movie existingMovie = new Movie();
        existingMovie.setId(1L);
        when(movieRepository.findBySlug("existing-slug")).thenReturn(Optional.of(existingMovie));
        boolean result = slugService.isSlugAvailableForMovie("existing-slug", 1L);
        assertThat(result).isTrue();
    }

    @Test
    void isSlugAvailableForMovieShouldReturnFalseWhenSlugBelongsToDifferentMovie() {
        Movie existingMovie = new Movie();
        existingMovie.setId(2L);
        when(movieRepository.findBySlug("existing-slug")).thenReturn(Optional.of(existingMovie));
        boolean result = slugService.isSlugAvailableForMovie("existing-slug", 1L);
        assertThat(result).isFalse();
    }

    @Test
    void generateSlugShouldHandleNumbers() {
        String result = slugService.generateSlug("Movie 2024 Part 2");
        assertThat(result).isEqualTo("movie-2024-part-2");
    }

    @Test
    void generateSlugShouldHandleMixedCase() {
        String result = slugService.generateSlug("TeSt MoViE TiTlE");
        assertThat(result).isEqualTo("test-movie-title");
    }

    @Test
    void generateSlugShouldHandleSingleWord() {
        String result = slugService.generateSlug("Movie");
        assertThat(result).isEqualTo("movie");
    }
}