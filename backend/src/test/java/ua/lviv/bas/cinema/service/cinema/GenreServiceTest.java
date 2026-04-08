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
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.mapper.cinema.GenreMapper;
import ua.lviv.bas.cinema.repository.cinema.GenreRepository;
import ua.lviv.bas.cinema.repository.cinema.projection.GenreProjection;

@ExtendWith(MockitoExtension.class)
public class GenreServiceTest {

	@Mock
	private GenreRepository genreRepository;

	@Mock
	private GenreMapper genreMapper;

	@InjectMocks
	private GenreService genreService;

	private final Long GENRE_ID = 1L;
	private final String GENRE_NAME = "Action";

	@Test
	void createGenre_ShouldSaveNewGenre() {
		GenreRequest request = new GenreRequest(GENRE_NAME);
		Genre genre = new Genre();
		genre.setId(GENRE_ID);
		genre.setName(GENRE_NAME);
		GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, 0);

		when(genreRepository.existsByNameIgnoreCase(GENRE_NAME)).thenReturn(false);
		when(genreMapper.toGenre(request)).thenReturn(genre);
		when(genreRepository.save(genre)).thenReturn(genre);
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		GenreListResponse result = genreService.createGenre(request);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(GENRE_ID);
		assertThat(result.name()).isEqualTo(GENRE_NAME);
		assertThat(result.movieCount()).isZero();
		verify(genreRepository).save(genre);
	}

	@Test
	void createGenre_ShouldThrowExceptionWhenNameExists() {
		GenreRequest request = new GenreRequest(GENRE_NAME);
		when(genreRepository.existsByNameIgnoreCase(GENRE_NAME)).thenReturn(true);

		assertThatThrownBy(() -> genreService.createGenre(request)).isInstanceOf(DuplicateEntityException.class)
				.hasMessageContaining(GENRE_NAME);

		verify(genreRepository, never()).save(any());
	}

	@Test
	void getGenreById_ShouldReturnGenre() {
		Genre genre = new Genre();
		genre.setId(GENRE_ID);
		genre.setName(GENRE_NAME);
		GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, 0);

		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(genre));
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		GenreListResponse result = genreService.getGenreById(GENRE_ID);

		assertThat(result).isNotNull();
		assertThat(result.id()).isEqualTo(GENRE_ID);
		assertThat(result.name()).isEqualTo(GENRE_NAME);
	}

	@Test
	void getGenreById_ShouldThrowExceptionWhenNotFound() {
		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.getGenreById(GENRE_ID)).isInstanceOf(GenreNotFoundException.class);
	}

	@Test
	void updateGenre_ShouldUpdateName() {
		Genre existingGenre = new Genre();
		existingGenre.setId(GENRE_ID);
		existingGenre.setName("Old Name");
		GenreRequest request = new GenreRequest(GENRE_NAME);
		GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, 0);

		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(existingGenre));
		when(genreRepository.existsByNameIgnoreCaseAndIdNot(GENRE_NAME, GENRE_ID)).thenReturn(false);
		when(genreRepository.save(existingGenre)).thenReturn(existingGenre);
		when(genreMapper.toGenreResponse(existingGenre)).thenReturn(response);

		GenreListResponse result = genreService.updateGenre(GENRE_ID, request);

		assertThat(result).isNotNull();
		assertThat(result.name()).isEqualTo(GENRE_NAME);
		verify(genreMapper).updateGenreFromRequest(request, existingGenre);
		verify(genreRepository).save(existingGenre);
	}

	@Test
	void updateGenre_ShouldThrowExceptionWhenGenreNotFound() {
		GenreRequest request = new GenreRequest(GENRE_NAME);
		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.updateGenre(GENRE_ID, request))
				.isInstanceOf(GenreNotFoundException.class);
	}

	@Test
	void updateGenre_ShouldThrowExceptionWhenNameExists() {
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
	void deleteGenre_ShouldDeleteGenre() {
		when(genreRepository.existsById(GENRE_ID)).thenReturn(true);

		genreService.deleteGenre(GENRE_ID);

		verify(genreRepository).deleteById(GENRE_ID);
	}

	@Test
	void deleteGenre_ShouldThrowExceptionWhenGenreNotFound() {
		when(genreRepository.existsById(GENRE_ID)).thenReturn(false);

		assertThatThrownBy(() -> genreService.deleteGenre(GENRE_ID)).isInstanceOf(GenreNotFoundException.class);

		verify(genreRepository, never()).deleteById(any());
	}

	@Test
	void searchGenres_ShouldReturnPage() {
		String query = "act";
		Pageable pageable = PageRequest.of(0, 10);

		GenreProjection projection = new TestGenreProjection(GENRE_ID, GENRE_NAME, 5);
		Page<GenreProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);
		GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, 5);

		when(genreRepository.findProjectionsByQuery(query, pageable)).thenReturn(projectionPage);
		when(genreMapper.toGenreResponse(projection)).thenReturn(response);

		Page<GenreListResponse> result = genreService.searchGenres(query, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).id()).isEqualTo(GENRE_ID);
		assertThat(result.getContent().get(0).name()).isEqualTo(GENRE_NAME);
		assertThat(result.getContent().get(0).movieCount()).isEqualTo(5);
	}

	@Test
	void searchGenres_WithNullQuery_ShouldReturnAll() {
		String query = null;
		Pageable pageable = PageRequest.of(0, 10);

		GenreProjection projection = new TestGenreProjection(GENRE_ID, GENRE_NAME, 5);
		Page<GenreProjection> projectionPage = new PageImpl<>(List.of(projection), pageable, 1);
		GenreListResponse response = new GenreListResponse(GENRE_ID, GENRE_NAME, 5);

		when(genreRepository.findProjectionsByQuery(query, pageable)).thenReturn(projectionPage);
		when(genreMapper.toGenreResponse(projection)).thenReturn(response);

		Page<GenreListResponse> result = genreService.searchGenres(query, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	private static class TestGenreProjection implements GenreProjection {
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