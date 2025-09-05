package ua.lviv.bas.cinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.dao.GenreRepository;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.GenreDto;
import ua.lviv.bas.cinema.mapper.GenreMapper;

@ExtendWith(MockitoExtension.class)
public class GenreServiceTest {
	@Mock
	private GenreRepository genreRepository;

	@Mock
	private GenreMapper genreMapper;

	@InjectMocks
	private GenreService genreService;

	@Test
	void createGenre_ShouldReturnGenreDto() {
		GenreDto requestDto = GenreDto.builder().name("Action").build();
		Genre genre = Genre.builder().name("Action").build();
		Genre savedGenre = Genre.builder().id(1L).name("Action").build();
		GenreDto responseDto = GenreDto.builder().id(1L).name("Action").build();

		when(genreMapper.toEntity(requestDto)).thenReturn(genre);
		when(genreRepository.save(genre)).thenReturn(savedGenre);
		when(genreMapper.toDto(savedGenre)).thenReturn(responseDto);

		GenreDto result = genreService.createGenre(requestDto);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Action");
		verify(genreRepository).save(genre);
	}

	@Test
	void readGenre_WhenExists_ShouldReturnGenreDto() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreDto dto = GenreDto.builder().id(1L).name("Action").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(genre));
		when(genreMapper.toDto(genre)).thenReturn(dto);

		GenreDto result = genreService.readGenre(1L);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Action");
	}

	@Test
	void readGenre_WhenNotExists_ShouldReturnNull() {
		when(genreRepository.findById(999L)).thenReturn(Optional.empty());

		GenreDto result = genreService.readGenre(999L);

		assertThat(result).isNull();
	}

	@Test
	void updateGenre_ShouldReturnUpdatedGenreDto() {
		GenreDto requestDto = GenreDto.builder().name("Updated Action").build();
		Genre existingGenre = Genre.builder().id(1L).name("Action").build();
		Genre updatedGenre = Genre.builder().id(1L).name("Updated Action").build();
		GenreDto responseDto = GenreDto.builder().id(1L).name("Updated Action").build();

		when(genreRepository.findById(1L)).thenReturn(Optional.of(existingGenre));
		when(genreRepository.save(existingGenre)).thenReturn(updatedGenre);
		when(genreMapper.toDto(updatedGenre)).thenReturn(responseDto);

		GenreDto result = genreService.updateGenre(1L, requestDto);

		assertThat(result.getName()).isEqualTo("Updated Action");
		verify(genreRepository).save(existingGenre);
	}

	@Test
	void updateGenre_WhenNotExists_ShouldThrowException() {
		GenreDto requestDto = GenreDto.builder().name("Action").build();
		when(genreRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.updateGenre(999L, requestDto)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("not found");
	}

	@Test
	void deleteGenre_ShouldCallRepository() {
		genreService.deleteGenre(1L);
		verify(genreRepository).deleteById(1L);
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
	void findAllById_ShouldReturnFilteredList() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		GenreDto dto = GenreDto.builder().id(1L).name("Action").build();

		when(genreRepository.findAllById(List.of(1L))).thenReturn(List.of(genre));
		when(genreMapper.toDto(genre)).thenReturn(dto);

		List<GenreDto> result = genreService.findAllById(List.of(1L));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Action");
	}
}