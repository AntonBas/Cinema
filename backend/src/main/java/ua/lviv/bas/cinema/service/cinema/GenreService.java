package ua.lviv.bas.cinema.service.cinema;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.mapper.GenreMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreService {

	private final GenreRepository genreRepository;
	private final GenreMapper genreMapper;

	@Transactional
	public GenreResponse createGenre(GenreRequest request) {
		log.info("Creating genre: {}", request.getName());

		String genreName = request.getName().trim();
		validateGenreUniqueness(genreName, null);

		Genre genre = genreMapper.toGenre(request);
		genre.setName(genreName);
		Genre savedGenre = genreRepository.save(genre);

		log.debug("Genre created with ID: {}", savedGenre.getId());
		return genreMapper.toGenreResponse(savedGenre);
	}

	public GenreResponse getGenreById(Long id) {
		log.debug("Retrieving genre by id: {}", id);

		return genreRepository.findById(id).map(genreMapper::toGenreResponse)
				.orElseThrow(() -> new GenreNotFoundException(id));
	}

	@Transactional
	public GenreResponse updateGenre(Long id, GenreRequest request) {
		log.info("Updating genre with id: {}", id);

		Genre existingGenre = genreRepository.findById(id).orElseThrow(() -> new GenreNotFoundException(id));

		String genreName = request.getName().trim();
		validateGenreUniqueness(genreName, id);

		genreMapper.updateGenreFromRequest(request, existingGenre);
		existingGenre.setName(genreName);

		Genre updatedGenre = genreRepository.save(existingGenre);

		log.debug("Genre updated with ID: {}", updatedGenre.getId());
		return genreMapper.toGenreResponse(updatedGenre);
	}

	@Transactional
	public void deleteGenre(Long id) {
		log.info("Deleting genre with id: {}", id);

		if (!genreRepository.existsById(id)) {
			throw new GenreNotFoundException(id);
		}

		genreRepository.deleteById(id);
		log.debug("Genre deleted with ID: {}", id);
	}

	public List<GenreResponse> getGenres() {
		log.debug("Retrieving all genres");
		return genreMapper.toGenreResponseList(genreRepository.findAll());
	}

	public List<GenreResponse> getGenresSorted() {
		log.debug("Retrieving all genres sorted by name");
		return genreMapper.toGenreResponseList(genreRepository.findAll(Sort.by("name").ascending()));
	}

	public List<GenreResponse> getGenresByIds(List<Long> ids) {
		log.debug("Retrieving genres by ids: {}", ids);

		if (ids == null || ids.isEmpty()) {
			return List.of();
		}

		List<Genre> genres = genreRepository.findAllById(ids);
		return genreMapper.toGenreResponseList(genres);
	}

	public Page<GenreResponse> getGenresPage(Pageable pageable) {
		log.debug("Retrieving genres with pagination");
		Page<Genre> genrePage = genreRepository.findAll(pageable);
		return genrePage.map(genreMapper::toGenreResponse);
	}

	public Page<GenreResponse> searchGenres(String query, Pageable pageable) {
		log.info("Searching genres: query='{}'", query);

		if (StringUtils.hasText(query)) {
			String searchQuery = query.trim();
			Page<Genre> genrePage = genreRepository.findByNameContainingIgnoreCase(searchQuery, pageable);
			return genrePage.map(genreMapper::toGenreResponse);
		}

		return getGenresPage(pageable);
	}

	public boolean existsById(Long id) {
		return genreRepository.existsById(id);
	}

	public boolean existsByName(String name) {
		return genreRepository.existsByNameIgnoreCase(name.trim());
	}

	private void validateGenreUniqueness(String name, Long excludeId) {
		boolean exists;

		if (excludeId != null) {
			exists = genreRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId);
		} else {
			exists = genreRepository.existsByNameIgnoreCase(name);
		}

		if (exists) {
			throw new DuplicateEntityException("Genre", name);
		}
	}
}