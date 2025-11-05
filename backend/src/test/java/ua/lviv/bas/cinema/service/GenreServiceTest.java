package ua.lviv.bas.cinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.movie.GenreDto;
import ua.lviv.bas.cinema.dto.movie.GenreRequest;
import ua.lviv.bas.cinema.exception.GenreNotFoundException;
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
	void createGenre_ShouldReturnGenreDto() {
		GenreRequest request = GenreRequest.builder().name("Action").build();
		Genre genre = Genre.builder().name("Action").build();
		Genre savedGenre = Genre.builder().id(1L).name("Action").build();
		GenreDto responseDto = GenreDto.builder().id(1L).name("Action").build();

		when(genreMapper.toEntity(request)).thenReturn(genre);
		when(genreRepository.save(genre)).thenReturn(savedGenre);
		when(genreMapper.toDto(savedGenre)).thenReturn(responseDto);

		GenreDto result = genreService.createGenre(request);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Action");
		verify(genreRepository).save(genre);
	}

	@Test
	void getGenreById_WhenExists_ShouldReturnDto() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreDto dto = GenreDto.builder().id(1L).name("Action").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
		when(genreMapper.toDto(genre)).thenReturn(dto);

		GenreDto result = genreService.getGenreById(1L);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Action");
	}

	@Test
	void getGenreById_WhenNotExists_ShouldThrowException() {
		when(genreRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.getGenreById(999L)).isInstanceOf(GenreNotFoundException.class)
				.hasMessageContaining("Genre not found");
	}

	@Test
	void updateGenre_ShouldUpdateAndReturnDto() {
		GenreRequest request = GenreRequest.builder().name("Updated Action").build();
		Genre existing = Genre.builder().id(1L).name("Action").build();
		Genre updated = Genre.builder().id(1L).name("Updated Action").build();
		GenreDto responseDto = GenreDto.builder().id(1L).name("Updated Action").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(genreRepository.save(existing)).thenReturn(updated);
		when(genreMapper.toDto(updated)).thenReturn(responseDto);

		GenreDto result = genreService.updateGenre(1L, request);

		assertThat(result.getName()).isEqualTo("Updated Action");
		verify(genreRepository).save(existing);
	}

	@Test
	void updateGenre_WhenNotFound_ShouldThrowException() {
		GenreRequest request = GenreRequest.builder().name("Action").build();
		when(genreRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.updateGenre(999L, request)).isInstanceOf(GenreNotFoundException.class)
				.hasMessageContaining("Genre not found");
	}

	@Test
	void deleteGenre_WhenExists_ShouldDelete() {
		when(genreRepository.existsById(1L)).thenReturn(true);

		genreService.deleteGenre(1L);

		verify(genreRepository).deleteById(1L);
	}

	@Test
	void deleteGenre_WhenNotExists_ShouldThrowException() {
		when(genreRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> genreService.deleteGenre(1L)).isInstanceOf(GenreNotFoundException.class)
				.hasMessageContaining("Genre not found");

		verify(genreRepository, never()).deleteById(1L);
	}

	@Test
	void getAllGenres_ShouldReturnList() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreDto dto = GenreDto.builder().id(1L).name("Action").build();

		when(genreRepository.findAll()).thenReturn(List.of(genre));
		when(genreMapper.toDto(genre)).thenReturn(dto);

		List<GenreDto> result = genreService.getAllGenres();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Action");
	}

	@Test
	void searchGenres_WithQuery_ShouldReturnFilteredPage() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreDto dto = GenreDto.builder().id(1L).name("Action").build();

		Page<Genre> page = new org.springframework.data.domain.PageImpl<>(List.of(genre));

		when(genreRepository.findByNameContainingIgnoreCase("Act", PageRequest.of(0, 5, Sort.by("name").ascending())))
				.thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		var result = genreService.searchGenres("Act", 0, 5);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getName()).isEqualTo("Action");
		assertThat(result.getTotalPages()).isEqualTo(1);
		assertThat(result.getCurrentPage()).isEqualTo(0);
		assertThat(result.getPageSize()).isEqualTo(1);
		verify(genreRepository).findByNameContainingIgnoreCase("Act",
				PageRequest.of(0, 5, Sort.by("name").ascending()));
	}

	@Test
	void searchGenres_WithoutQuery_ShouldReturnAll() {
		Genre genre = Genre.builder().id(1L).name("Comedy").build();
		GenreDto dto = GenreDto.builder().id(1L).name("Comedy").build();

		Page<Genre> page = new org.springframework.data.domain.PageImpl<>(List.of(genre));

		when(genreRepository.findAll(PageRequest.of(0, 5, Sort.by("name").ascending()))).thenReturn(page);
		when(genreMapper.toDto(genre)).thenReturn(dto);

		var result = genreService.searchGenres(null, 0, 5);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getName()).isEqualTo("Comedy");
		assertThat(result.getTotalElements()).isEqualTo(1);
		assertThat(result.getCurrentPage()).isEqualTo(0);
		verify(genreRepository).findAll(PageRequest.of(0, 5, Sort.by("name").ascending()));
	}

}