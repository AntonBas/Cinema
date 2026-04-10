package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.cinema.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreListResponse;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreHasMoviesException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.mapper.cinema.GenreMapper;
import ua.lviv.bas.cinema.repository.cinema.GenreRepository;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;
import ua.lviv.bas.cinema.repository.cinema.projection.GenreListProjection;

@ExtendWith(MockitoExtension.class)
public class GenreServiceTest {

	@Mock
	private GenreRepository genreRepository;

	@Mock
	private MovieRepository movieRepository;

	@Mock
	private GenreMapper genreMapper;

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
		when(genreMapper.toGenre(request)).thenReturn(genre);
		when(genreRepository.save(genre)).thenReturn(genre);
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		GenreResponse result = genreService.createGenre(request);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(GENRE_ID);
		assertThat(result.name()).isEqualTo(GENRE_NAME);
		verify(genreRepository).save(genre);
	}

	@Test
	void createGenreShouldThrowExceptionWhenNameExists() {
		GenreRequest request = new GenreRequest(GENRE_NAME);
		when(genreRepository.existsByNameIgnoreCase(GENRE_NAME)).thenReturn(true);

		assertThatThrownBy(() -> genreService.createGenre(request)).isInstanceOf(DuplicateEntityException.class)
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
		assertThat(result.getContent().get(0).id()).isEqualTo(GENRE_ID);
		assertThat(result.getContent().get(0).name()).isEqualTo(GENRE_NAME);
		assertThat(result.getContent().get(0).movieCount()).isEqualTo(5);
	}

	@Test
	void getGenresWithNullQueryShouldReturnAll() {
		String query = null;
		Pageable pageable = PageRequest.of(0, 10);

		GenreListProjection projection = new TestGenreProjection(GENRE_ID, GENRE_NAME, 5);
		Page<GenreListProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);
		GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, 5);

		when(genreRepository.findGenresByFilters(query, pageable)).thenReturn(projectionPage);
		when(genreMapper.toGenreListResponse(projection)).thenReturn(response);

		Page<GenreListResponse> result = genreService.getGenres(query, pageable);

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
		verify(genreMapper).updateGenreFromRequest(request, existingGenre);
		verify(genreRepository).save(existingGenre);
	}

	@Test
	void updateGenreShouldThrowExceptionWhenGenreNotFound() {
		GenreRequest request = new GenreRequest(GENRE_NAME);
		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.updateGenre(GENRE_ID, request))
				.isInstanceOf(GenreNotFoundException.class);
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
	}

	@Test
	void deleteGenreShouldThrowExceptionWhenGenreNotFound() {
		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.deleteGenre(GENRE_ID)).isInstanceOf(GenreNotFoundException.class);

		verify(genreRepository, never()).deleteById(any());
	}

	@Test
	void deleteGenreShouldThrowExceptionWhenGenreHasMovies() {
		Genre genre = new Genre();
		genre.setId(GENRE_ID);
		genre.setName(GENRE_NAME);

		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(genre));
		when(movieRepository.countMovieUsageByGenreId(GENRE_ID)).thenReturn(5L);

		assertThatThrownBy(() -> genreService.deleteGenre(GENRE_ID)).isInstanceOf(GenreHasMoviesException.class);

		verify(genreRepository, never()).deleteById(any());
	}

	private static class TestGenreProjection implements GenreListProjection {
		private final Long id;
		private final String name;
		private final Integer movieCount;

		TestGenreProjection(Long id, String name, Integer movieCount) {
			this.id = id;
			this.name = name;
			this.movieCount = movieCount;
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Integer getMovieCount() {
			return movieCount;
		}
	}
}