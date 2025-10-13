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
import ua.lviv.bas.cinema.dto.GenreDto;
import ua.lviv.bas.cinema.dto.GenreRequest;
import ua.lviv.bas.cinema.dto.PageResponse;
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
		Genre saved = genreRepository.save(genre);
		return genreMapper.toDto(saved);
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

		Genre updated = genreRepository.save(existingGenre);
		return genreMapper.toDto(updated);
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
		return genreRepository.findAll().stream().map(genreMapper::toDto).toList();
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

		return new PageResponse<>(genrePage.getContent().stream().map(genreMapper::toDto).toList(),
				genrePage.getNumber(), genrePage.getTotalPages(), genrePage.getTotalElements(), genrePage.getSize());
	}
}
