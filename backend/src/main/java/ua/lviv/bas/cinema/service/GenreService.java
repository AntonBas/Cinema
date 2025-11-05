package ua.lviv.bas.cinema.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.movie.GenreDto;
import ua.lviv.bas.cinema.dto.movie.GenreRequest;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.GenreNotFoundException;
import ua.lviv.bas.cinema.mapper.GenreMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenreService {

	private final GenreRepository genreRepository;
	private final GenreMapper genreMapper;

	public GenreDto createGenre(GenreRequest request) {
		log.info("Creating genre: {}", request.getName());
		Genre genre = genreMapper.toEntity(request);
		Genre savedGenre = genreRepository.save(genre);
		return genreMapper.toDto(savedGenre);
	}

	public GenreDto getGenreById(Long id) {
		log.info("Reading genre by id: {}", id);
		return genreRepository.findById(id).map(genreMapper::toDto)
				.orElseThrow(() -> new GenreNotFoundException("Genre not found with id: " + id));
	}

	public GenreDto updateGenre(Long id, GenreRequest request) {
		log.info("Updating genre with id: {}", id);

		Genre existingGenre = genreRepository.findById(id)
				.orElseThrow(() -> new GenreNotFoundException("Genre not found with id: " + id));

		genreMapper.updateGenreFromRequest(request, existingGenre);

		Genre updatedGenre = genreRepository.save(existingGenre);
		return genreMapper.toDto(updatedGenre);
	}

	public void deleteGenre(Long id) {
		log.info("Deleting genre by id: {}", id);
		if (!genreRepository.existsById(id)) {
			throw new GenreNotFoundException("Genre not found with id: " + id);
		}
		genreRepository.deleteById(id);
	}

	public List<GenreDto> getAllGenres() {
		log.info("Retrieving all genres");
		return genreMapper.toDtoList(genreRepository.findAll());
	}

	public PageResponse<GenreDto> searchGenres(String query, int page, int size) {
		log.info("Searching genres: query='{}', page={}, size={}", query, page, size);

		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
		Page<Genre> genrePage;

		if (query != null && !query.trim().isEmpty()) {
			genrePage = genreRepository.findByNameContainingIgnoreCase(query.trim(), pageable);
		} else {
			genrePage = genreRepository.findAll(pageable);
		}

		log.debug("Search found {} results for query: '{}'", genrePage.getTotalElements(), query);
		return toPageResponse(genrePage);
	}

	private PageResponse<GenreDto> toPageResponse(Page<Genre> genrePage) {
		List<GenreDto> content = genrePage.getContent().stream().map(genreMapper::toDto).toList();

		return new PageResponse<>(content, genrePage.getNumber(), genrePage.getTotalPages(),
				genrePage.getTotalElements(), genrePage.getSize());
	}
}
