package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.projection.GenreProjection;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.mapper.GenreMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;

@ExtendWith(MockitoExtension.class)
class GenreServiceTest {

	@Mock
	private GenreRepository genreRepository;

	@Mock
	private GenreMapper genreMapper;

	@InjectMocks
	private GenreService genreService;

	private final Long GENRE_ID = 1L;
	private final String GENRE_NAME = "Action";

	@Test
	void createGenre_Success() {
		GenreRequest request = GenreRequest.builder().name(GENRE_NAME).build();

		Genre genre = createGenre();
		GenreResponse response = createGenreResponse();

		when(genreRepository.existsByNameIgnoreCase(GENRE_NAME)).thenReturn(false);
		when(genreMapper.toGenre(request)).thenReturn(genre);
		when(genreRepository.save(genre)).thenReturn(genre);
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		GenreResponse result = genreService.createGenre(request);

		assertThat(result).isEqualTo(response);
		verify(genreRepository).save(genre);
	}

	@Test
	void createGenre_DuplicateName_ThrowsException() {
		GenreRequest request = GenreRequest.builder().name(GENRE_NAME).build();

		when(genreRepository.existsByNameIgnoreCase(GENRE_NAME)).thenReturn(true);

		assertThatThrownBy(() -> genreService.createGenre(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getGenreById_Success() {
		Genre genre = createGenre();
		GenreResponse response = createGenreResponse();

		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(genre));
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		GenreResponse result = genreService.getGenreById(GENRE_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getGenreById_NotFound_ThrowsException() {
		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.getGenreById(GENRE_ID)).isInstanceOf(GenreNotFoundException.class);
	}

	@Test
	void updateGenre_Success() {
		Genre existingGenre = createGenre();
		existingGenre.setName("Old Name");

		GenreRequest request = GenreRequest.builder().name(GENRE_NAME).build();

		GenreResponse response = createGenreResponse();

		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(existingGenre));
		when(genreRepository.existsByNameIgnoreCaseAndIdNot(GENRE_NAME, GENRE_ID)).thenReturn(false);
		when(genreRepository.save(existingGenre)).thenReturn(existingGenre);
		when(genreMapper.toGenreResponse(existingGenre)).thenReturn(response);

		GenreResponse result = genreService.updateGenre(GENRE_ID, request);

		assertThat(result).isEqualTo(response);
		assertThat(existingGenre.getName()).isEqualTo(GENRE_NAME);
	}

	@Test
	void updateGenre_DuplicateName_ThrowsException() {
		Genre existingGenre = createGenre();
		existingGenre.setName("Old Name");

		GenreRequest request = GenreRequest.builder().name("Existing Genre").build();

		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(existingGenre));
		when(genreRepository.existsByNameIgnoreCaseAndIdNot("Existing Genre", GENRE_ID)).thenReturn(true);

		assertThatThrownBy(() -> genreService.updateGenre(GENRE_ID, request))
				.isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void deleteGenre_Success() {
		when(genreRepository.existsById(GENRE_ID)).thenReturn(true);

		genreService.deleteGenre(GENRE_ID);

		verify(genreRepository).deleteById(GENRE_ID);
	}

	@Test
	void deleteGenre_NotFound_ThrowsException() {
		when(genreRepository.existsById(GENRE_ID)).thenReturn(false);

		assertThatThrownBy(() -> genreService.deleteGenre(GENRE_ID)).isInstanceOf(GenreNotFoundException.class);
	}

	@Test
	void searchGenres_WithQuery_Success() {
		String query = "act";
		Pageable pageable = PageRequest.of(0, 10);
		GenreProjection projection = createGenreProjection(10);
		Page<GenreProjection> projectionPage = new PageImpl<>(List.of(projection));
		GenreResponse response = createGenreResponse();

		when(genreRepository.searchProjectionsByName(query, pageable)).thenReturn(projectionPage);
		when(genreMapper.toGenreResponse(projection)).thenReturn(response);

		Page<GenreResponse> result = genreService.searchGenres(query, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(response);
	}

	@Test
	void searchGenres_WithoutQuery_Success() {
		Pageable pageable = PageRequest.of(0, 10);
		GenreProjection projection = createGenreProjection(5);
		Page<GenreProjection> projectionPage = new PageImpl<>(List.of(projection));
		GenreResponse response = createGenreResponse();

		when(genreRepository.findAllProjections(pageable)).thenReturn(projectionPage);
		when(genreMapper.toGenreResponse(projection)).thenReturn(response);

		Page<GenreResponse> result = genreService.searchGenres(null, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(response);
	}

	@Test
	void getPopularGenres_WithQuery_Success() {
		String query = "act";
		int limit = 5;

		GenreProjection projection1 = createGenreProjection(10);
		GenreProjection projection2 = createGenreProjection(5);

		Page<GenreProjection> page = new PageImpl<>(List.of(projection1, projection2));
		GenreResponse response = createGenreResponse();

		when(genreRepository.searchProjectionsByName(query, PageRequest.of(0, 100))).thenReturn(page);
		when(genreMapper.toGenreResponse(any(GenreProjection.class))).thenReturn(response);

		List<GenreResponse> result = genreService.getPopularGenres(query, limit);

		assertThat(result).hasSize(2);
	}

	@Test
	void getPopularGenres_WithoutQuery_Success() {
		int limit = 5;
		GenreProjection projection = createGenreProjection(10);
		Page<GenreProjection> page = new PageImpl<>(List.of(projection));
		GenreResponse response = createGenreResponse();

		when(genreRepository.findAllProjections(PageRequest.of(0, 100))).thenReturn(page);
		when(genreMapper.toGenreResponse(projection)).thenReturn(response);

		List<GenreResponse> result = genreService.getPopularGenres(null, limit);

		assertThat(result).hasSize(1);
	}

	@Test
	void getGenresByIds_Success() {
		List<Long> ids = List.of(1L, 2L);
		Genre genre = createGenre();
		GenreResponse response = createGenreResponse();

		when(genreRepository.findAllById(ids)).thenReturn(List.of(genre));
		when(genreMapper.toGenreResponseList(List.of(genre))).thenReturn(List.of(response));

		List<GenreResponse> result = genreService.getGenresByIds(ids);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void getGenresByIds_EmptyList_Success() {
		List<GenreResponse> result = genreService.getGenresByIds(List.of());

		assertThat(result).isEmpty();
	}

	@Test
	void existsByName_Success() {
		when(genreRepository.existsByNameIgnoreCase(GENRE_NAME)).thenReturn(true);

		boolean result = genreService.existsByName(GENRE_NAME);

		assertThat(result).isTrue();
	}

	private Genre createGenre() {
		Genre genre = new Genre();
		genre.setId(GENRE_ID);
		genre.setName(GENRE_NAME);
		return genre;
	}

	private GenreResponse createGenreResponse() {
		return GenreResponse.builder().id(GENRE_ID).name(GENRE_NAME).build();
	}

	private GenreProjection createGenreProjection(Integer movieCount) {
		return new GenreProjection() {
			@Override
			public Long getId() {
				return GENRE_ID;
			}

			@Override
			public String getName() {
				return GENRE_NAME;
			}

			@Override
			public Integer getMovieCount() {
				return movieCount;
			}
		};
	}
}