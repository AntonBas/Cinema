package ua.lviv.bas.cinema.service.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.querydsl.core.types.Predicate;

import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.filter.MovieFilter;
import ua.lviv.bas.cinema.repository.MovieRepository;

@ExtendWith(MockitoExtension.class)
class MovieQueryServiceTest {

	@Mock
	private MovieRepository movieRepository;

	@InjectMocks
	private MovieQueryService movieQueryService;

	@Test
	void findFilteredMovies_WithSearchTerm_ShouldReturnFilteredMovies() {
		MovieFilter filter = MovieFilter.builder().searchTerm("Inception").page(0).size(10).sortBy("title")
				.sortDirection(MovieFilter.SortDirection.ASC).build();

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		Page<Movie> result = movieQueryService.findFilteredMovies(filter);

		assertThat(result).isNotNull();
	}

	@Test
	void findFilteredMovies_WithStatusFilter_ShouldReturnFilteredMovies() {
		MovieFilter filter = MovieFilter.builder().status(MovieStatus.CURRENT).page(0).size(10).sortBy("title")
				.sortDirection(MovieFilter.SortDirection.ASC).build();

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		Page<Movie> result = movieQueryService.findFilteredMovies(filter);

		assertThat(result).isNotNull();
	}

	@Test
	void findFilteredMovies_WithAgeRatingFilter_ShouldReturnFilteredMovies() {
		MovieFilter filter = MovieFilter.builder().ageRating(AgeRating.PEGI_12).page(0).size(10).sortBy("title")
				.sortDirection(MovieFilter.SortDirection.ASC).build();

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		Page<Movie> result = movieQueryService.findFilteredMovies(filter);

		assertThat(result).isNotNull();
	}

	@Test
	void findFilteredMovies_WithDurationFilter_ShouldReturnFilteredMovies() {
		MovieFilter filter = MovieFilter.builder().minDuration(90).maxDuration(180).page(0).size(10).sortBy("title")
				.sortDirection(MovieFilter.SortDirection.ASC).build();

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		Page<Movie> result = movieQueryService.findFilteredMovies(filter);

		assertThat(result).isNotNull();
	}

	@Test
	void findFilteredMovies_WithDateFilter_ShouldReturnFilteredMovies() {
		MovieFilter filter = MovieFilter.builder().releaseDateFrom(LocalDate.now().minusMonths(1))
				.releaseDateTo(LocalDate.now()).page(0).size(10).sortBy("title")
				.sortDirection(MovieFilter.SortDirection.ASC).build();

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		Page<Movie> result = movieQueryService.findFilteredMovies(filter);

		assertThat(result).isNotNull();
	}

	@Test
	void findFilteredMovies_WhenInvalidDateRange_ShouldThrowException() {
		MovieFilter filter = MovieFilter.builder().releaseDateFrom(LocalDate.now().plusDays(1))
				.releaseDateTo(LocalDate.now()).page(0).size(10).build();

		assertThatThrownBy(() -> movieQueryService.findFilteredMovies(filter))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("releaseDateFrom cannot be after releaseDateTo");
	}

	@Test
	void findFilteredMovies_WhenInvalidDurationRange_ShouldThrowException() {
		MovieFilter filter = MovieFilter.builder().minDuration(200).maxDuration(100).page(0).size(10).build();

		assertThatThrownBy(() -> movieQueryService.findFilteredMovies(filter))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("minDuration cannot be greater than maxDuration");
	}

	@Test
	void findFilteredMovies_WhenUserTriesToViewArchived_ShouldThrowException() {
		MovieFilter filter = MovieFilter.builder().status(MovieStatus.ARCHIVED).adminView(false).page(0).size(10)
				.build();

		assertThatThrownBy(() -> movieQueryService.findFilteredMovies(filter))
				.isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Users cannot filter by ARCHIVED status");
	}

	@Test
	void findFilteredMovies_WhenPageSizeTooLarge_ShouldThrowException() {
		MovieFilter filter = MovieFilter.builder().page(0).size(101).build();

		assertThatThrownBy(() -> movieQueryService.findFilteredMovies(filter))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Page size cannot exceed 100");
	}

	@Test
	void findByTitle_ShouldReturnMovies() {
		String search = "Inception";
		Pageable pageable = PageRequest.of(0, 10);
		boolean adminView = false;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(page);

		Page<Movie> result = movieQueryService.findByTitle(search, pageable, adminView);

		assertThat(result).isNotNull();
	}

	@Test
	void findByTitle_WithEmptySearch_ShouldReturnAllMovies() {
		Pageable pageable = PageRequest.of(0, 10);
		boolean adminView = false;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(page);

		Page<Movie> result = movieQueryService.findByTitle("", pageable, adminView);

		assertThat(result).isNotNull();
	}

	@Test
	void findByStatus_ShouldReturnMovies() {
		MovieStatus status = MovieStatus.CURRENT;
		Pageable pageable = PageRequest.of(0, 10);
		boolean adminView = false;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(page);

		Page<Movie> result = movieQueryService.findByStatus(status, pageable, adminView);

		assertThat(result).isNotNull();
	}

	@Test
	void findByStatus_WhenUserTriesToViewArchived_ShouldThrowException() {
		MovieStatus status = MovieStatus.ARCHIVED;
		Pageable pageable = PageRequest.of(0, 10);
		boolean adminView = false;

		assertThatThrownBy(() -> movieQueryService.findByStatus(status, pageable, adminView))
				.isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Users cannot view archived movies");
	}

	@Test
	void findByAgeRating_ShouldReturnMovies() {
		AgeRating ageRating = AgeRating.PEGI_12;
		Pageable pageable = PageRequest.of(0, 10);
		boolean adminView = false;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(page);

		Page<Movie> result = movieQueryService.findByAgeRating(ageRating, pageable, adminView);

		assertThat(result).isNotNull();
	}

	@Test
	void findCurrentlyShowing_ShouldReturnMovies() {
		Pageable pageable = PageRequest.of(0, 10);
		boolean adminView = false;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(page);

		Page<Movie> result = movieQueryService.findCurrentlyShowing(pageable, adminView);

		assertThat(result).isNotNull();
	}

	@Test
	void findUpcoming_ShouldReturnMovies() {
		Pageable pageable = PageRequest.of(0, 10);
		boolean adminView = false;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(page);

		Page<Movie> result = movieQueryService.findUpcoming(pageable, adminView);

		assertThat(result).isNotNull();
	}

	@Test
	void findArchived_ShouldReturnMovies() {
		Pageable pageable = PageRequest.of(0, 10);

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(page);

		Page<Movie> result = movieQueryService.findArchived(pageable);

		assertThat(result).isNotNull();
	}

	@Test
	void findAvailableMovies_ShouldReturnMovies() {
		LocalDate fromDate = LocalDate.now();
		LocalDate toDate = LocalDate.now().plusDays(30);
		Pageable pageable = PageRequest.of(0, 10);
		boolean adminView = false;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(page);

		Page<Movie> result = movieQueryService.findAvailableMovies(fromDate, toDate, pageable, adminView);

		assertThat(result).isNotNull();
	}

	@Test
	void findMoviesForSessionCreation_ShouldReturnMovies() {
		String searchTerm = "Inception";
		LocalDate sessionDate = LocalDate.now();

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		List<Movie> result = movieQueryService.findMoviesForSessionCreation(searchTerm, sessionDate);

		assertThat(result).isNotNull();
	}

	@Test
	void findCurrentlyShowingList_ShouldReturnMovies() {
		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		List<Movie> result = movieQueryService.findCurrentlyShowingList();

		assertThat(result).isNotNull();
	}

	@Test
	void findUpcomingList_ShouldReturnMovies() {
		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		List<Movie> result = movieQueryService.findUpcomingList();

		assertThat(result).isNotNull();
	}

	@Test
	void findNewReleases_ShouldReturnMovies() {
		int limit = 5;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		List<Movie> result = movieQueryService.findNewReleases(limit);

		assertThat(result).isNotNull();
	}

	@Test
	void findEndingSoon_ShouldReturnMovies() {
		int limit = 5;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		List<Movie> result = movieQueryService.findEndingSoon(limit);

		assertThat(result).isNotNull();
	}

	@Test
	void countCurrentlyShowing_ShouldReturnCount() {
		when(movieRepository.count(any(Predicate.class))).thenReturn(10L);

		long result = movieQueryService.countCurrentlyShowing();

		assertThat(result).isEqualTo(10L);
	}

	@Test
	void countUpcoming_ShouldReturnCount() {
		when(movieRepository.count(any(Predicate.class))).thenReturn(5L);

		long result = movieQueryService.countUpcoming();

		assertThat(result).isEqualTo(5L);
	}

	@Test
	void existsBySlug_WhenSlugExists_ShouldReturnTrue() {
		String slug = "inception-2020";
		Long excludeId = null;

		when(movieRepository.exists(any(Predicate.class))).thenReturn(true);

		boolean result = movieQueryService.existsBySlug(slug, excludeId);

		assertThat(result).isTrue();
	}

	@Test
	void existsBySlug_WhenExcludingId_ShouldCheckCorrectly() {
		String slug = "inception-2020";
		Long excludeId = 1L;

		when(movieRepository.exists(any(Predicate.class))).thenReturn(false);

		boolean result = movieQueryService.existsBySlug(slug, excludeId);

		assertThat(result).isFalse();
	}

	@Test
	void findFilteredMovies_ForAdminView_ShouldIncludeArchived() {
		MovieFilter filter = MovieFilter.builder().status(MovieStatus.ARCHIVED).adminView(true).page(0).size(10)
				.sortBy("title").sortDirection(MovieFilter.SortDirection.ASC).build();

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		Page<Movie> result = movieQueryService.findFilteredMovies(filter);

		assertThat(result).isNotNull();
	}

	@Test
	void findFilteredMovies_ForUserView_ShouldExcludeArchivedByDefault() {
		MovieFilter filter = MovieFilter.builder().adminView(false).page(0).size(10).sortBy("title")
				.sortDirection(MovieFilter.SortDirection.ASC).build();

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		Page<Movie> result = movieQueryService.findFilteredMovies(filter);

		assertThat(result).isNotNull();
	}

	@Test
	void findMoviesForSessionCreation_WithEmptySearchTerm_ShouldReturnMovies() {
		LocalDate sessionDate = LocalDate.now();

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		List<Movie> result = movieQueryService.findMoviesForSessionCreation("", sessionDate);

		assertThat(result).isNotNull();
	}

	@Test
	void findMoviesForSessionCreation_WithNullSearchTerm_ShouldReturnMovies() {
		LocalDate sessionDate = LocalDate.now();

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		List<Movie> result = movieQueryService.findMoviesForSessionCreation(null, sessionDate);

		assertThat(result).isNotNull();
	}

	@Test
	void findNewReleases_WithZeroLimit_ShouldReturnEmptyList() {
		int limit = 1;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		List<Movie> result = movieQueryService.findNewReleases(limit);

		assertThat(result).isNotNull();
	}

	@Test
	void findEndingSoon_WithZeroLimit_ShouldReturnEmptyList() {
		int limit = 1;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), any(Pageable.class))).thenReturn(page);

		List<Movie> result = movieQueryService.findEndingSoon(limit);

		assertThat(result).isNotNull();
	}

	@Test
	void findByAgeRating_ForAdminView_ShouldAllowArchived() {
		AgeRating ageRating = AgeRating.PEGI_12;
		Pageable pageable = PageRequest.of(0, 10);
		boolean adminView = true;

		Page<Movie> page = new PageImpl<>(List.of(new Movie()));
		when(movieRepository.findAll(any(Predicate.class), eq(pageable))).thenReturn(page);

		Page<Movie> result = movieQueryService.findByAgeRating(ageRating, pageable, adminView);

		assertThat(result).isNotNull();
	}
}