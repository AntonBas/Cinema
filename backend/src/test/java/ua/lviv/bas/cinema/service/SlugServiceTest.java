package ua.lviv.bas.cinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.service.common.SlugService;

@ExtendWith(MockitoExtension.class)
class SlugServiceTest {

	@Mock
	private MovieRepository movieRepository;

	@InjectMocks
	private SlugService slugService;

	@Test
	void generateSlug_ShouldGenerateValidSlug_FromNormalTitle() {
		String title = "Test Movie Title";

		String result = slugService.generateSlug(title);

		assertThat(result).isEqualTo("test-movie-title");
	}

	@Test
	void generateSlug_ShouldRemoveSpecialCharacters() {
		String title = "Test & Movie: Special@Characters!";

		String result = slugService.generateSlug(title);

		assertThat(result).isEqualTo("test-movie-specialcharacters");
	}

	@Test
	void generateSlug_ShouldHandleAccentedCharacters() {
		String title = "Café Crème Français";

		String result = slugService.generateSlug(title);

		assertThat(result).isEqualTo("cafe-creme-francais");
	}

	@Test
	void generateSlug_ShouldHandleMultipleSpaces() {
		String title = "Test    Movie   Title";

		String result = slugService.generateSlug(title);

		assertThat(result).isEqualTo("test-movie-title");
	}

	@Test
	void generateSlug_ShouldHandleTrailingSpaces() {
		String title = "  Test Movie Title  ";

		String result = slugService.generateSlug(title);

		assertThat(result).isEqualTo("test-movie-title");
	}

	@Test
	void generateSlug_ShouldThrowException_WhenTitleIsNull() {
		assertThatThrownBy(() -> slugService.generateSlug(null)).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Title cannot be null or empty");
	}

	@Test
	void generateSlug_ShouldThrowException_WhenTitleIsEmpty() {
		assertThatThrownBy(() -> slugService.generateSlug("")).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Title cannot be null or empty");
	}

	@Test
	void generateSlug_ShouldThrowException_WhenTitleIsBlank() {
		assertThatThrownBy(() -> slugService.generateSlug("   ")).isInstanceOf(IllegalArgumentException.class)
				.hasMessage("Title cannot be null or empty");
	}

	@Test
	void generateUniqueSlug_ShouldReturnBaseSlug_WhenNoConflict() {
		String title = "Unique Movie Title";

		when(movieRepository.findBySlug("unique-movie-title")).thenReturn(Optional.empty());

		String result = slugService.generateUniqueSlug(title);

		assertThat(result).isEqualTo("unique-movie-title");
	}

	@Test
	void generateUniqueSlug_ShouldAddCounter_WhenSlugExists() {
		String title = "Existing Movie";

		Movie existingMovie = new Movie();
		existingMovie.setId(1L);
		existingMovie.setTitle("Existing Movie");

		when(movieRepository.findBySlug("existing-movie")).thenReturn(Optional.of(existingMovie));
		when(movieRepository.findBySlug("existing-movie-1")).thenReturn(Optional.empty());

		String result = slugService.generateUniqueSlug(title);

		assertThat(result).isEqualTo("existing-movie-1");
	}

	@Test
	void generateUniqueSlug_ShouldIncrementCounter_WhenMultipleConflicts() {
		String title = "Popular Movie";

		Movie existingMovie1 = new Movie();
		existingMovie1.setId(1L);
		existingMovie1.setTitle("Popular Movie");

		Movie existingMovie2 = new Movie();
		existingMovie2.setId(2L);
		existingMovie2.setTitle("Popular Movie 1");

		when(movieRepository.findBySlug("popular-movie")).thenReturn(Optional.of(existingMovie1));
		when(movieRepository.findBySlug("popular-movie-1")).thenReturn(Optional.of(existingMovie2));
		when(movieRepository.findBySlug("popular-movie-2")).thenReturn(Optional.empty());

		String result = slugService.generateUniqueSlug(title);

		assertThat(result).isEqualTo("popular-movie-2");
	}

	@Test
	void isSlugAvailable_ShouldReturnTrue_WhenSlugNotExists() {
		when(movieRepository.findBySlug("available-slug")).thenReturn(Optional.empty());

		boolean result = slugService.isSlugAvailable("available-slug");

		assertThat(result).isTrue();
	}

	@Test
	void isSlugAvailable_ShouldReturnFalse_WhenSlugExists() {
		Movie existingMovie = new Movie();
		existingMovie.setId(1L);
		existingMovie.setTitle("Existing Movie");

		when(movieRepository.findBySlug("existing-slug")).thenReturn(Optional.of(existingMovie));

		boolean result = slugService.isSlugAvailable("existing-slug");

		assertThat(result).isFalse();
	}

	@Test
	void isSlugAvailableForMovie_ShouldReturnTrue_WhenSlugNotExists() {
		when(movieRepository.findBySlug("new-slug")).thenReturn(Optional.empty());

		boolean result = slugService.isSlugAvailableForMovie("new-slug", 1L);

		assertThat(result).isTrue();
	}

	@Test
	void isSlugAvailableForMovie_ShouldReturnTrue_WhenSlugBelongsToSameMovie() {
		Movie existingMovie = new Movie();
		existingMovie.setId(1L);
		existingMovie.setTitle("Existing Movie");

		when(movieRepository.findBySlug("existing-slug")).thenReturn(Optional.of(existingMovie));

		boolean result = slugService.isSlugAvailableForMovie("existing-slug", 1L);

		assertThat(result).isTrue();
	}

	@Test
	void isSlugAvailableForMovie_ShouldReturnFalse_WhenSlugBelongsToDifferentMovie() {
		Movie existingMovie = new Movie();
		existingMovie.setId(2L);
		existingMovie.setTitle("Existing Movie");

		when(movieRepository.findBySlug("existing-slug")).thenReturn(Optional.of(existingMovie));

		boolean result = slugService.isSlugAvailableForMovie("existing-slug", 1L);

		assertThat(result).isFalse();
	}

	@Test
	void generateSlug_ShouldHandleNumbers() {
		String title = "Movie 2024 Part 2";

		String result = slugService.generateSlug(title);

		assertThat(result).isEqualTo("movie-2024-part-2");
	}

	@Test
	void generateSlug_ShouldHandleMixedCase() {
		String title = "TeSt MoViE TiTlE";

		String result = slugService.generateSlug(title);

		assertThat(result).isEqualTo("test-movie-title");
	}

	@Test
	void generateSlug_ShouldHandleSingleWord() {
		String title = "Movie";

		String result = slugService.generateSlug(title);

		assertThat(result).isEqualTo("movie");
	}
}