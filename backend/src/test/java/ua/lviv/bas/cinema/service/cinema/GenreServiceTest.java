package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
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
import org.springframework.data.domain.Sort;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.mapper.GenreMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;

@ExtendWith(MockitoExtension.class)
public class GenreServiceTest {

	@Mock
	private GenreRepository genreRepository;

	@Mock
	private GenreMapper genreMapper;

	@InjectMocks
	private GenreService genreService;

	@Test
	void createGenre_Success() {
		GenreRequest request = GenreRequest.builder().name("Action").build();

		Genre genre = new Genre();
		genre.setName("Action");

		Genre savedGenre = new Genre();
		savedGenre.setId(1L);
		savedGenre.setName("Action");

		GenreResponse response = GenreResponse.builder().id(1L).name("Action").build();

		when(genreRepository.existsByNameIgnoreCase("Action")).thenReturn(false);
		when(genreMapper.toGenre(request)).thenReturn(genre);
		when(genreRepository.save(genre)).thenReturn(savedGenre);
		when(genreMapper.toGenreResponse(savedGenre)).thenReturn(response);

		GenreResponse result = genreService.createGenre(request);

		assertThat(result.getName()).isEqualTo("Action");
		verify(genreRepository).existsByNameIgnoreCase("Action");
	}

	@Test
	void createGenre_WhenNameExists_ShouldThrowException() {
		GenreRequest request = GenreRequest.builder().name("Action").build();

		when(genreRepository.existsByNameIgnoreCase("Action")).thenReturn(true);

		assertThatThrownBy(() -> genreService.createGenre(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getGenreById_Success() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		GenreResponse response = GenreResponse.builder().id(1L).name("Action").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		GenreResponse result = genreService.getGenreById(1L);

		assertThat(result.getName()).isEqualTo("Action");
	}

	@Test
	void getGenreById_WhenNotFound_ShouldThrowException() {
		when(genreRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.getGenreById(1L)).isInstanceOf(GenreNotFoundException.class);
	}

	@Test
	void updateGenre_Success() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Old Name");

		GenreRequest request = GenreRequest.builder().name("New Name").build();

		GenreResponse response = GenreResponse.builder().id(1L).name("New Name").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
		when(genreRepository.existsByNameIgnoreCaseAndIdNot("New Name", 1L)).thenReturn(false);
		when(genreRepository.save(genre)).thenReturn(genre);
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		GenreResponse result = genreService.updateGenre(1L, request);

		assertThat(result.getName()).isEqualTo("New Name");
	}

	@Test
	void updateGenre_WhenNotFound_ShouldThrowException() {
		GenreRequest request = GenreRequest.builder().name("New Name").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.updateGenre(1L, request)).isInstanceOf(GenreNotFoundException.class);
	}

	@Test
	void updateGenre_WhenNameExists_ShouldThrowException() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Old Name");

		GenreRequest request = GenreRequest.builder().name("Existing Name").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
		when(genreRepository.existsByNameIgnoreCaseAndIdNot("Existing Name", 1L)).thenReturn(true);

		assertThatThrownBy(() -> genreService.updateGenre(1L, request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void deleteGenre_Success() {
		when(genreRepository.existsById(1L)).thenReturn(true);
		doNothing().when(genreRepository).deleteById(1L);

		genreService.deleteGenre(1L);

		verify(genreRepository).deleteById(1L);
	}

	@Test
	void deleteGenre_WhenNotFound_ShouldThrowException() {
		when(genreRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> genreService.deleteGenre(1L)).isInstanceOf(GenreNotFoundException.class);
	}

	@Test
	void getGenres_Success() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		GenreResponse response = GenreResponse.builder().id(1L).name("Action").build();

		when(genreRepository.findAll()).thenReturn(Collections.singletonList(genre));
		when(genreMapper.toGenreResponseList(Collections.singletonList(genre)))
				.thenReturn(Collections.singletonList(response));

		List<GenreResponse> result = genreService.getGenres();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Action");
	}

	@Test
	void getGenresSorted_Success() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		GenreResponse response = GenreResponse.builder().id(1L).name("Action").build();

		when(genreRepository.findAll(Sort.by("name").ascending())).thenReturn(Collections.singletonList(genre));
		when(genreMapper.toGenreResponseList(Collections.singletonList(genre)))
				.thenReturn(Collections.singletonList(response));

		List<GenreResponse> result = genreService.getGenresSorted();

		assertThat(result).hasSize(1);
	}

	@Test
	void getGenresByIds_Success() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		GenreResponse response = GenreResponse.builder().id(1L).name("Action").build();

		when(genreRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Collections.singletonList(genre));
		when(genreMapper.toGenreResponseList(Collections.singletonList(genre)))
				.thenReturn(Collections.singletonList(response));

		List<GenreResponse> result = genreService.getGenresByIds(Arrays.asList(1L, 2L));

		assertThat(result).hasSize(1);
	}

	@Test
	void searchGenres_WithQuery() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		GenreResponse response = GenreResponse.builder().id(1L).name("Action").build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Genre> page = new PageImpl<>(Collections.singletonList(genre));

		when(genreRepository.findByNameContainingIgnoreCase("Act", pageable)).thenReturn(page);
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		Page<GenreResponse> result = genreService.searchGenres("Act", pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void searchGenres_WithEmptyQuery() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		GenreResponse response = GenreResponse.builder().id(1L).name("Action").build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Genre> page = new PageImpl<>(Collections.singletonList(genre));

		when(genreRepository.findAll(pageable)).thenReturn(page);
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		Page<GenreResponse> result = genreService.searchGenres("", pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getGenresPage_Success() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		GenreResponse response = GenreResponse.builder().id(1L).name("Action").build();

		Pageable pageable = PageRequest.of(0, 10);
		Page<Genre> page = new PageImpl<>(Collections.singletonList(genre));

		when(genreRepository.findAll(pageable)).thenReturn(page);
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		Page<GenreResponse> result = genreService.getGenresPage(pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void existsById_Success() {
		when(genreRepository.existsById(1L)).thenReturn(true);

		boolean result = genreService.existsById(1L);

		assertThat(result).isTrue();
	}

	@Test
	void existsByName_Success() {
		when(genreRepository.existsByNameIgnoreCase("Action")).thenReturn(true);

		boolean result = genreService.existsByName("Action");

		assertThat(result).isTrue();
	}
}