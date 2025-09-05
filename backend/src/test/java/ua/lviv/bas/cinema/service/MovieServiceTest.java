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

import ua.lviv.bas.cinema.dao.MovieRepository;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.dto.MovieDto;
import ua.lviv.bas.cinema.mapper.MovieMapper;

@ExtendWith(MockitoExtension.class)
public class MovieServiceTest {

	@Mock
	private MovieRepository movieRepository;

	@Mock
	private MovieMapper movieMapper;

	@InjectMocks
	private MovieService movieService;

	@Test
	void createMovie_ShouldReturnMovieDto() {
		MovieDto requestDto = MovieDto.builder().title("Test Movie").build();
		Movie movieEntity = Movie.builder().id(1L).title("Test Movie").build();
		MovieDto responseDto = MovieDto.builder().id(1L).title("Test Movie").build();

		when(movieMapper.toEntity(requestDto)).thenReturn(movieEntity);
		when(movieRepository.save(movieEntity)).thenReturn(movieEntity);
		when(movieMapper.toDto(movieEntity)).thenReturn(responseDto);

		MovieDto result = movieService.createMovie(requestDto);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getTitle()).isEqualTo("Test Movie");
		verify(movieRepository).save(movieEntity);
	}

	@Test
	void getMovieById_WhenNotFound_ShouldThrowException() {
		when(movieRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.getMovieById(999L)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("not found");
	}

	@Test
	void updateMovie_WhenNotFound_ShouldThrowException() {
		MovieDto dto = MovieDto.builder().id(999L).title("Test").build();
		when(movieRepository.findById(999L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> movieService.updateMovie(999L, dto)).isInstanceOf(RuntimeException.class);
	}

	@Test
	void getAllMovies_ShouldReturnList() {
		Movie movie = Movie.builder().id(1L).title("Test").build();
		MovieDto dto = MovieDto.builder().id(1L).title("Test").build();

		when(movieRepository.findAll()).thenReturn(List.of(movie));
		when(movieMapper.toDto(movie)).thenReturn(dto);

		List<MovieDto> result = movieService.getAllMovies();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getTitle()).isEqualTo("Test");
	}
}