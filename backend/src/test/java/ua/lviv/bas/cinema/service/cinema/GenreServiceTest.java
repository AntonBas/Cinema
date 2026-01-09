package ua.lviv.bas.cinema.service.cinema;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
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
		Genre genre = new Genre();
		genre.setName("Action");

		Genre savedGenre = new Genre();
		savedGenre.setId(1L);
		savedGenre.setName("Action");

		GenreResponse responseDto = new GenreResponse();
		responseDto.setId(1L);
		responseDto.setName("Action");

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
	}

	@Test
	void createGenre_ShouldTrimName_WhenRequestHasSpaces() {
		GenreRequest request = new GenreRequest("  Action  ");
		Genre genre = new Genre();
		genre.setName("Action");

		Genre savedGenre = new Genre();
		savedGenre.setId(1L);
		savedGenre.setName("Action");

		GenreResponse responseDto = new GenreResponse();
		responseDto.setId(1L);
		responseDto.setName("Action");

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
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		GenreResponse dto = new GenreResponse();
		dto.setId(1L);
		dto.setName("Action");

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
		Genre existing = new Genre();
		existing.setId(1L);
		existing.setName("Action");

		Genre updated = new Genre();
		updated.setId(1L);
		updated.setName("Updated Action");

		GenreResponse responseDto = new GenreResponse();
		responseDto.setId(1L);
		responseDto.setName("Updated Action");

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
		Genre existing = new Genre();
		existing.setId(1L);
		existing.setName("Action");

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
	void getGenres_ShouldReturnListOfGenres() {
		Genre genre1 = new Genre();
		genre1.setId(1L);
		genre1.setName("Action");

		Genre genre2 = new Genre();
		genre2.setId(2L);
		genre2.setName("Comedy");

		List<Genre> genres = List.of(genre1, genre2);

		GenreResponse dto1 = new GenreResponse();
		dto1.setId(1L);
		dto1.setName("Action");

		GenreResponse dto2 = new GenreResponse();
		dto2.setId(2L);
		dto2.setName("Comedy");

		List<GenreResponse> dtos = List.of(dto1, dto2);

		when(genreRepository.findAll()).thenReturn(genres);
		when(genreMapper.toDtoList(genres)).thenReturn(dtos);

		List<GenreResponse> result = genreService.getGenres();

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("Action", result.get(0).getName());
		assertEquals("Comedy", result.get(1).getName());
		verify(genreRepository).findAll();
		verify(genreMapper).toDtoList(genres);
	}

	@Test
	void getGenres_WhenNoGenres_ShouldReturnEmptyList() {
		when(genreRepository.findAll()).thenReturn(List.of());
		when(genreMapper.toDtoList(List.of())).thenReturn(List.of());

		List<GenreResponse> result = genreService.getGenres();

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(genreRepository).findAll();
		verify(genreMapper).toDtoList(List.of());
	}

	@Test
	void getGenresSorted_ShouldReturnSortedListOfGenres() {
		Genre genre1 = new Genre();
		genre1.setId(1L);
		genre1.setName("Action");

		Genre genre2 = new Genre();
		genre2.setId(2L);
		genre2.setName("Comedy");

		List<Genre> genres = List.of(genre1, genre2);

		GenreResponse dto1 = new GenreResponse();
		dto1.setId(1L);
		dto1.setName("Action");

		GenreResponse dto2 = new GenreResponse();
		dto2.setId(2L);
		dto2.setName("Comedy");

		List<GenreResponse> dtos = List.of(dto1, dto2);

		when(genreRepository.findAll(Sort.by("name").ascending())).thenReturn(genres);
		when(genreMapper.toDtoList(genres)).thenReturn(dtos);

		List<GenreResponse> result = genreService.getGenresSorted();

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("Action", result.get(0).getName());
		assertEquals("Comedy", result.get(1).getName());
		verify(genreRepository).findAll(Sort.by("name").ascending());
		verify(genreMapper).toDtoList(genres);
	}

	@Test
	void getGenresByIds_ShouldReturnListOfGenres() {
		Genre genre1 = new Genre();
		genre1.setId(1L);
		genre1.setName("Action");

		Genre genre2 = new Genre();
		genre2.setId(2L);
		genre2.setName("Comedy");

		List<Genre> genres = List.of(genre1, genre2);
		List<Long> ids = List.of(1L, 2L);

		GenreResponse dto1 = new GenreResponse();
		dto1.setId(1L);
		dto1.setName("Action");

		GenreResponse dto2 = new GenreResponse();
		dto2.setId(2L);
		dto2.setName("Comedy");

		List<GenreResponse> dtos = List.of(dto1, dto2);

		when(genreRepository.findAllById(ids)).thenReturn(genres);
		when(genreMapper.toDtoList(genres)).thenReturn(dtos);

		List<GenreResponse> result = genreService.getGenresByIds(ids);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("Action", result.get(0).getName());
		assertEquals("Comedy", result.get(1).getName());
		verify(genreRepository).findAllById(ids);
		verify(genreMapper).toDtoList(genres);
	}

	@Test
	void getGenresByIds_WithEmptyList_ShouldReturnEmptyList() {
		List<GenreResponse> result = genreService.getGenresByIds(List.of());

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(genreRepository, never()).findAllById(anyList());
		verify(genreMapper, never()).toDtoList(anyList());
	}

	@Test
	void getGenresByIds_WithNullList_ShouldReturnEmptyList() {
		List<GenreResponse> result = genreService.getGenresByIds(null);

		assertNotNull(result);
		assertTrue(result.isEmpty());
		verify(genreRepository, never()).findAllById(anyList());
		verify(genreMapper, never()).toDtoList(anyList());
	}

	@Test
	void searchGenres_WithQuery_ShouldReturnFilteredPage() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Action");

		GenreResponse dto = new GenreResponse();
		dto.setId(1L);
		dto.setName("Action");

		Pageable pageable = PageRequest.of(0, 10);
		Page<Genre> page = new PageImpl<>(List.of(genre), pageable, 1);

		when(genreRepository.findByNameContainingIgnoreCase("Act", pageable)).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		Page<GenreResponse> result = genreService.searchGenres("Act", pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals("Action", result.getContent().get(0).getName());
		assertEquals(0, result.getNumber());
		assertEquals(1, result.getTotalElements());
		assertEquals(10, result.getSize());
		verify(genreRepository).findByNameContainingIgnoreCase("Act", pageable);
	}

	@Test
	void searchGenres_WithEmptyQuery_ShouldReturnAllGenres() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Comedy");

		GenreResponse dto = new GenreResponse();
		dto.setId(1L);
		dto.setName("Comedy");

		Pageable pageable = PageRequest.of(0, 10);
		Page<Genre> page = new PageImpl<>(List.of(genre), pageable, 1);

		when(genreRepository.findAll(pageable)).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		Page<GenreResponse> result = genreService.searchGenres("", pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals("Comedy", result.getContent().get(0).getName());
		verify(genreRepository).findAll(pageable);
	}

	@Test
	void searchGenres_WithNullQuery_ShouldReturnAllGenres() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Drama");

		GenreResponse dto = new GenreResponse();
		dto.setId(1L);
		dto.setName("Drama");

		Pageable pageable = PageRequest.of(0, 10);
		Page<Genre> page = new PageImpl<>(List.of(genre), pageable, 1);

		when(genreRepository.findAll(pageable)).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		Page<GenreResponse> result = genreService.searchGenres(null, pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		assertEquals("Drama", result.getContent().get(0).getName());
		verify(genreRepository).findAll(pageable);
	}

	@Test
	void searchGenres_WithQueryContainingSpaces_ShouldTrimAndSearch() {
		Genre genre = new Genre();
		genre.setId(1L);
		genre.setName("Science Fiction");

		GenreResponse dto = new GenreResponse();
		dto.setId(1L);
		dto.setName("Science Fiction");

		Pageable pageable = PageRequest.of(0, 10);
		Page<Genre> page = new PageImpl<>(List.of(genre), pageable, 1);

		when(genreRepository.findByNameContainingIgnoreCase("science", pageable)).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		Page<GenreResponse> result = genreService.searchGenres("  science  ", pageable);

		assertNotNull(result);
		assertEquals(1, result.getContent().size());
		verify(genreRepository).findByNameContainingIgnoreCase("science", pageable);
	}

	@Test
	void getGenresPage_ShouldReturnPagedGenres() {
		Genre genre1 = new Genre();
		genre1.setId(1L);
		genre1.setName("Action");

		Genre genre2 = new Genre();
		genre2.setId(2L);
		genre2.setName("Comedy");

		Pageable pageable = PageRequest.of(0, 10);
		Page<Genre> page = new PageImpl<>(Arrays.asList(genre1, genre2), pageable, 2);

		GenreResponse dto1 = new GenreResponse();
		dto1.setId(1L);
		dto1.setName("Action");

		GenreResponse dto2 = new GenreResponse();
		dto2.setId(2L);
		dto2.setName("Comedy");

		when(genreRepository.findAll(pageable)).thenReturn(page);
		when(genreMapper.toDto(genre1)).thenReturn(dto1);
		when(genreMapper.toDto(genre2)).thenReturn(dto2);

		Page<GenreResponse> result = genreService.getGenresPage(pageable);

		assertNotNull(result);
		assertEquals(2, result.getContent().size());
		assertEquals("Action", result.getContent().get(0).getName());
		assertEquals("Comedy", result.getContent().get(1).getName());
		assertEquals(0, result.getNumber());
		assertEquals(10, result.getSize());
		assertEquals(2, result.getTotalElements());
		verify(genreRepository).findAll(pageable);
	}

	@Test
	void existsById_ShouldReturnTrue_WhenGenreExists() {
		when(genreRepository.existsById(1L)).thenReturn(true);

		boolean result = genreService.existsById(1L);

		assertTrue(result);
		verify(genreRepository).existsById(1L);
	}

	@Test
	void existsById_ShouldReturnFalse_WhenGenreNotExists() {
		when(genreRepository.existsById(999L)).thenReturn(false);

		boolean result = genreService.existsById(999L);

		assertFalse(result);
		verify(genreRepository).existsById(999L);
	}

	@Test
	void existsByName_ShouldReturnTrue_WhenGenreExists() {
		when(genreRepository.existsByNameIgnoreCase("Action")).thenReturn(true);

		boolean result = genreService.existsByName("Action");

		assertTrue(result);
		verify(genreRepository).existsByNameIgnoreCase("Action");
	}

	@Test
	void existsByName_ShouldReturnFalse_WhenGenreNotExists() {
		when(genreRepository.existsByNameIgnoreCase("Unknown")).thenReturn(false);

		boolean result = genreService.existsByName("Unknown");

		assertFalse(result);
		verify(genreRepository).existsByNameIgnoreCase("Unknown");
	}

	@Test
	void existsByName_ShouldTrimName_WhenSpacesPresent() {
		when(genreRepository.existsByNameIgnoreCase("Action")).thenReturn(true);

		boolean result = genreService.existsByName("  Action  ");

		assertTrue(result);
		verify(genreRepository).existsByNameIgnoreCase("Action");
	}
}