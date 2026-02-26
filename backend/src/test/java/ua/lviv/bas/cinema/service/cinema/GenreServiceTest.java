package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
	void createGenreShouldSaveNewGenre() {
		GenreRequest request = GenreRequest.builder().name(GENRE_NAME).build();
		Genre genre = new Genre();
		genre.setId(GENRE_ID);
		genre.setName(GENRE_NAME);
		GenreResponse response = new GenreResponse();
		response.setId(GENRE_ID);
		response.setName(GENRE_NAME);

		when(genreRepository.existsByNameIgnoreCase(GENRE_NAME)).thenReturn(false);
		when(genreMapper.toGenre(request)).thenReturn(genre);
		when(genreRepository.save(genre)).thenReturn(genre);
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		GenreResponse result = genreService.createGenre(request);

		assertThat(result.getId()).isEqualTo(GENRE_ID);
		assertThat(result.getName()).isEqualTo(GENRE_NAME);
	}

	@Test
	void createGenreShouldThrowExceptionWhenNameExists() {
		GenreRequest request = GenreRequest.builder().name(GENRE_NAME).build();
		when(genreRepository.existsByNameIgnoreCase(GENRE_NAME)).thenReturn(true);

		assertThatThrownBy(() -> genreService.createGenre(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getGenreByIdShouldReturnGenre() {
		Genre genre = new Genre();
		genre.setId(GENRE_ID);
		genre.setName(GENRE_NAME);
		GenreResponse response = new GenreResponse();
		response.setId(GENRE_ID);
		response.setName(GENRE_NAME);

		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(genre));
		when(genreMapper.toGenreResponse(genre)).thenReturn(response);

		GenreResponse result = genreService.getGenreById(GENRE_ID);

		assertThat(result.getId()).isEqualTo(GENRE_ID);
	}

	@Test
	void getGenreByIdShouldThrowExceptionWhenNotFound() {
		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> genreService.getGenreById(GENRE_ID)).isInstanceOf(GenreNotFoundException.class);
	}

	@Test
	void updateGenreShouldUpdateName() {
		Genre existingGenre = new Genre();
		existingGenre.setId(GENRE_ID);
		existingGenre.setName("Old Name");
		GenreRequest request = GenreRequest.builder().name(GENRE_NAME).build();
		GenreResponse response = new GenreResponse();
		response.setId(GENRE_ID);
		response.setName(GENRE_NAME);

		when(genreRepository.findById(GENRE_ID)).thenReturn(Optional.of(existingGenre));
		when(genreRepository.existsByNameIgnoreCaseAndIdNot(GENRE_NAME, GENRE_ID)).thenReturn(false);
		when(genreRepository.save(existingGenre)).thenReturn(existingGenre);
		when(genreMapper.toGenreResponse(existingGenre)).thenReturn(response);

		GenreResponse result = genreService.updateGenre(GENRE_ID, request);

		assertThat(result.getName()).isEqualTo(GENRE_NAME);
	}

	@Test
	void deleteGenreShouldDeleteGenre() {
		when(genreRepository.existsById(GENRE_ID)).thenReturn(true);

		genreService.deleteGenre(GENRE_ID);

		verify(genreRepository).deleteById(GENRE_ID);
	}

	@Test
	void searchGenresShouldReturnPage() {
		String query = "act";
		Pageable pageable = PageRequest.of(0, 10);

		GenreProjection projection = new GenreProjection(GENRE_ID, GENRE_NAME, 5);
		Page<GenreProjection> projectionPage = new PageImpl<>(java.util.List.of(projection));

		GenreResponse response = new GenreResponse();
		response.setId(GENRE_ID);
		response.setName(GENRE_NAME);
		response.setMovieCount(5);

		when(genreRepository.findProjectionsByQuery(query, pageable)).thenReturn(projectionPage);
		when(genreMapper.toGenreResponse(projection)).thenReturn(response);

		Page<GenreResponse> result = genreService.searchGenres(query, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getId()).isEqualTo(GENRE_ID);
	}
}