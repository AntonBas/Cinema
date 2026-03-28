package ua.lviv.bas.cinema.service.cinema;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.projection.cinema.GenreProjection;
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
@CacheConfig(cacheNames = "genres")
public class GenreService {

	private final GenreRepository genreRepository;
	private final GenreMapper genreMapper;

	@CacheEvict(allEntries = true)
	@Transactional
	public GenreResponse createGenre(GenreRequest request) {
		log.info("Creating genre: {}", request.name());

		String genreName = request.name().trim();
		validateGenreUniqueness(genreName, null);

		Genre genre = genreMapper.toGenre(request);
		genre.setName(genreName);
		Genre savedGenre = genreRepository.save(genre);

		log.debug("Genre created with ID: {}", savedGenre.getId());
		return genreMapper.toGenreResponse(savedGenre);
	}

	@Cacheable(key = "#id")
	public GenreResponse getGenreById(Long id) {
		log.debug("Retrieving genre by id: {}", id);
		return genreRepository.findById(id).map(genreMapper::toGenreResponse)
				.orElseThrow(() -> new GenreNotFoundException(id));
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(allEntries = true) })
	@Transactional
	public GenreResponse updateGenre(Long id, GenreRequest request) {
		log.info("Updating genre with id: {}", id);

		Genre existingGenre = genreRepository.findById(id).orElseThrow(() -> new GenreNotFoundException(id));

		String genreName = request.name().trim();
		validateGenreUniqueness(genreName, id);

		genreMapper.updateGenreFromRequest(request, existingGenre);
		existingGenre.setName(genreName);

		Genre updatedGenre = genreRepository.save(existingGenre);
		log.debug("Genre updated with ID: {}", updatedGenre.getId());
		return genreMapper.toGenreResponse(updatedGenre);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(allEntries = true) })
	@Transactional
	public void deleteGenre(Long id) {
		log.info("Deleting genre with id: {}", id);

		if (!genreRepository.existsById(id)) {
			throw new GenreNotFoundException(id);
		}

		genreRepository.deleteById(id);
		log.debug("Genre deleted with ID: {}", id);
	}

	@Cacheable(key = "'popular-' + #query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<GenreResponse> searchGenres(String query, Pageable pageable) {
		log.info("Searching genres by popularity: query='{}', pageable={}", query, pageable);
		Page<GenreProjection> projections = genreRepository.findProjectionsByQuery(query, pageable);
		return projections.map(genreMapper::toGenreResponse);
	}

	private void validateGenreUniqueness(String name, Long excludeId) {
		boolean exists = excludeId != null ? genreRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId)
				: genreRepository.existsByNameIgnoreCase(name);

		if (exists) {
			throw new DuplicateEntityException("Genre", name);
		}
	}
}