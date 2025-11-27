package ua.lviv.bas.cinema.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.data.domain.Sort;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
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

	@Test
	void createGenre_ShouldReturnGenreDto_WhenValidRequest() {
		GenreRequest request = new GenreRequest("Action");
		Genre genre = Genre.builder().name("Action").build();
		Genre savedGenre = Genre.builder().id(1L).name("Action").build();
		GenreResponse responseDto = GenreResponse.builder().id(1L).name("Action").build();

		when(genreRepository.existsByNameIgnoreCase("Action")).thenReturn(false);
		when(genreMapper.toEntity(request)).thenReturn(genre);
		when(genreRepository.save(genre)).thenReturn(savedGenre);
		when(genreMapper.toDto(savedGenre)).thenReturn(responseDto);

		GenreResponse result = genreService.createGenre(request);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Action", result.getName());
		verify(genreRepository).existsByNameIgnoreCase("Action");
		verify(genreRepository).save(genre);
		verify(genreMapper).toDto(savedGenre);
	}

	@Test
	void createGenre_ShouldThrowDuplicateEntityException_WhenNameExists() {
		GenreRequest request = new GenreRequest("Action");

		when(genreRepository.existsByNameIgnoreCase("Action")).thenReturn(true);

		DuplicateEntityException exception = assertThrows(DuplicateEntityException.class,
				() -> genreService.createGenre(request));

		assertNotNull(exception);
		verify(genreRepository).existsByNameIgnoreCase("Action");
		verify(genreRepository, never()).save(any(Genre.class));
		verify(genreMapper, never()).toDto(any(Genre.class));
	}

	@Test
	void createGenre_ShouldTrimName_WhenRequestHasSpaces() {
		GenreRequest request = new GenreRequest("  Action  ");
		Genre genre = Genre.builder().name("Action").build();
		Genre savedGenre = Genre.builder().id(1L).name("Action").build();
		GenreResponse responseDto = GenreResponse.builder().id(1L).name("Action").build();

		when(genreRepository.existsByNameIgnoreCase("Action")).thenReturn(false);
		when(genreMapper.toEntity(any(GenreRequest.class))).thenReturn(genre);
		when(genreRepository.save(genre)).thenReturn(savedGenre);
		when(genreMapper.toDto(savedGenre)).thenReturn(responseDto);

		GenreResponse result = genreService.createGenre(request);

		assertNotNull(result);
		verify(genreRepository).existsByNameIgnoreCase("Action");
	}

	@Test
	void getGenreById_ShouldReturnGenreDto_WhenGenreExists() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreResponse dto = GenreResponse.builder().id(1L).name("Action").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
		when(genreMapper.toDto(genre)).thenReturn(dto);

		GenreResponse result = genreService.getGenreById(1L);

		assertNotNull(result);
		assertEquals(1L, result.getId());
		assertEquals("Action", result.getName());
		verify(genreRepository).findById(1L);
		verify(genreMapper).toDto(genre);
	}

	@Test
	void getGenreById_ShouldThrowGenreNotFoundException_WhenGenreNotExists() {
		when(genreRepository.findById(999L)).thenReturn(Optional.empty());

		GenreNotFoundException exception = assertThrows(GenreNotFoundException.class,
				() -> genreService.getGenreById(999L));

		assertNotNull(exception);
		verify(genreRepository).findById(999L);
		verify(genreMapper, never()).toDto(any(Genre.class));
	}

	@Test
	void updateGenre_ShouldUpdateAndReturnDto_WhenValidRequest() {
		GenreRequest request = new GenreRequest("Updated Action");
		Genre existing = Genre.builder().id(1L).name("Action").build();
		Genre updated = Genre.builder().id(1L).name("Updated Action").build();
		GenreResponse responseDto = GenreResponse.builder().id(1L).name("Updated Action").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(genreRepository.existsByNameIgnoreCaseAndIdNot("Updated Action", 1L)).thenReturn(false);
		when(genreRepository.save(existing)).thenReturn(updated);
		when(genreMapper.toDto(updated)).thenReturn(responseDto);

		GenreResponse result = genreService.updateGenre(1L, request);

		assertNotNull(result);
		assertEquals("Updated Action", result.getName());
		verify(genreRepository).findById(1L);
		verify(genreRepository).existsByNameIgnoreCaseAndIdNot("Updated Action", 1L);
		verify(genreRepository).save(existing);
		verify(genreMapper).toDto(updated);
	}

	@Test
	void updateGenre_ShouldThrowGenreNotFoundException_WhenGenreNotExists() {
		GenreRequest request = new GenreRequest("Updated Action");

		when(genreRepository.findById(999L)).thenReturn(Optional.empty());

		GenreNotFoundException exception = assertThrows(GenreNotFoundException.class,
				() -> genreService.updateGenre(999L, request));

		assertNotNull(exception);
		verify(genreRepository).findById(999L);
		verify(genreRepository, never()).save(any(Genre.class));
	}

	@Test
	void updateGenre_ShouldThrowDuplicateEntityException_WhenNewNameExists() {
		GenreRequest request = new GenreRequest("Existing Genre");
		Genre existing = Genre.builder().id(1L).name("Action").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(genreRepository.existsByNameIgnoreCaseAndIdNot("Existing Genre", 1L)).thenReturn(true);

		DuplicateEntityException exception = assertThrows(DuplicateEntityException.class,
				() -> genreService.updateGenre(1L, request));

		assertNotNull(exception);
		verify(genreRepository).findById(1L);
		verify(genreRepository).existsByNameIgnoreCaseAndIdNot("Existing Genre", 1L);
		verify(genreRepository, never()).save(any(Genre.class));
	}

	@Test
	void updateGenre_ShouldTrimName_WhenRequestHasSpaces() {
		GenreRequest request = new GenreRequest("  Updated Action  ");
		Genre existing = Genre.builder().id(1L).name("Action").build();
		Genre updated = Genre.builder().id(1L).name("Updated Action").build();
		GenreResponse responseDto = GenreResponse.builder().id(1L).name("Updated Action").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(genreRepository.existsByNameIgnoreCaseAndIdNot("Updated Action", 1L)).thenReturn(false);
		when(genreRepository.save(existing)).thenReturn(updated);
		when(genreMapper.toDto(updated)).thenReturn(responseDto);

		GenreResponse result = genreService.updateGenre(1L, request);

		assertNotNull(result);
		verify(genreRepository).existsByNameIgnoreCaseAndIdNot("Updated Action", 1L);
	}

	@Test
	void deleteGenre_ShouldDeleteGenre_WhenGenreExists() {
		when(genreRepository.existsById(1L)).thenReturn(true);

		genreService.deleteGenre(1L);

		verify(genreRepository).existsById(1L);
		verify(genreRepository).deleteById(1L);
	}

	@Test
	void deleteGenre_ShouldThrowGenreNotFoundException_WhenGenreNotExists() {
		when(genreRepository.existsById(999L)).thenReturn(false);

		GenreNotFoundException exception = assertThrows(GenreNotFoundException.class,
				() -> genreService.deleteGenre(999L));

		assertNotNull(exception);
		verify(genreRepository).existsById(999L);
		verify(genreRepository, never()).deleteById(anyLong());
	}

	@Test
	void getAllGenres_ShouldReturnListOfGenres() {
		Genre genre1 = Genre.builder().id(1L).name("Action").build();
		Genre genre2 = Genre.builder().id(2L).name("Comedy").build();
		List<Genre> genres = List.of(genre1, genre2);
		GenreResponse dto1 = GenreResponse.builder().id(1L).name("Action").build();
		GenreResponse dto2 = GenreResponse.builder().id(2L).name("Comedy").build();
		List<GenreResponse> dtos = List.of(dto1, dto2);

		when(genreRepository.findAll()).thenReturn(genres);
		when(genreMapper.toDtoList(genres)).thenReturn(dtos);

		List<GenreResponse> result = genreService.getAllGenres();

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("Action", result.get(0).getName());
		assertEquals("Comedy", result.get(1).getName());
		verify(genreRepository).findAll();
		verify(genreMapper).toDtoList(genres);
	}

	@Test
	void getAllGenres_WhenNoGenres_ShouldReturnEmptyList() {
		when(genreRepository.findAll()).thenReturn(List.of());
		when(genreMapper.toDtoList(List.of())).thenReturn(List.of());

		List<GenreResponse> result = genreService.getAllGenres();

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(genreRepository).findAll();
		verify(genreMapper).toDtoList(List.of());
	}

	@Test
	void searchGenres_WithQuery_ShouldReturnFilteredPage() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreResponse dto = GenreResponse.builder().id(1L).name("Action").build();
		Page<Genre> page = new PageImpl<>(List.of(genre), PageRequest.of(0, 10), 1);

		when(genreRepository.findByNameContainingIgnoreCase(eq("Act"), any(Pageable.class))).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		PageResponse<GenreResponse> result = genreService.searchGenres("Act", 0, 10);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals("Action", result.getContent().get(0).getName());
		assertEquals(0, result.getCurrentPage());
		assertEquals(1, result.getTotalPages());
		assertEquals(1, result.getTotalElements());
		assertEquals(10, result.getPageSize());
		verify(genreRepository).findByNameContainingIgnoreCase(eq("Act"), any(Pageable.class));
	}

	@Test
	void searchGenres_WithoutQuery_ShouldReturnAllGenres() {
		Genre genre = Genre.builder().id(1L).name("Comedy").build();
		GenreResponse dto = GenreResponse.builder().id(1L).name("Comedy").build();
		Page<Genre> page = new PageImpl<>(List.of(genre), PageRequest.of(0, 10), 1);

		when(genreRepository.findAll(any(Pageable.class))).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		PageResponse<GenreResponse> result = genreService.searchGenres(null, 0, 10);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals("Comedy", result.getContent().get(0).getName());
		verify(genreRepository).findAll(any(Pageable.class));
	}

	@Test
	void searchGenres_WithEmptyQuery_ShouldReturnAllGenres() {
		Genre genre = Genre.builder().id(1L).name("Drama").build();
		GenreResponse dto = GenreResponse.builder().id(1L).name("Drama").build();
		Page<Genre> page = new PageImpl<>(List.of(genre), PageRequest.of(0, 10), 1);

		when(genreRepository.findAll(any(Pageable.class))).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		PageResponse<GenreResponse> result = genreService.searchGenres("", 0, 10);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		verify(genreRepository).findAll(any(Pageable.class));
	}

	@Test
	void searchGenres_ShouldLimitPageSizeToMax() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreResponse dto = GenreResponse.builder().id(1L).name("Action").build();
		Page<Genre> page = new PageImpl<>(List.of(genre), PageRequest.of(0, 50), 1);

		when(genreRepository.findAll(any(Pageable.class))).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		PageResponse<GenreResponse> result = genreService.searchGenres(null, 0, 100);

		assertNotNull(result);
		assertEquals(50, result.getPageSize());
		verify(genreRepository).findAll(PageRequest.of(0, 50, Sort.by("name").ascending()));
	}

	@Test
	void searchGenres_ShouldHandleNegativePage() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreResponse dto = GenreResponse.builder().id(1L).name("Action").build();
		Page<Genre> page = new PageImpl<>(List.of(genre), PageRequest.of(0, 10), 1);

		when(genreRepository.findAll(any(Pageable.class))).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		PageResponse<GenreResponse> result = genreService.searchGenres(null, -1, 10);

		assertNotNull(result);
		assertEquals(0, result.getCurrentPage());
		verify(genreRepository).findAll(PageRequest.of(0, 10, Sort.by("name").ascending()));
	}

	@Test
	void searchGenres_ShouldHandleZeroSize() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreResponse dto = GenreResponse.builder().id(1L).name("Action").build();
		Page<Genre> page = new PageImpl<>(List.of(genre), PageRequest.of(0, 12), 1);

		when(genreRepository.findAll(any(Pageable.class))).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		PageResponse<GenreResponse> result = genreService.searchGenres(null, 0, 0);

		assertNotNull(result);
		assertEquals(12, result.getPageSize());
		verify(genreRepository).findAll(PageRequest.of(0, 12, Sort.by("name").ascending()));
	}
}