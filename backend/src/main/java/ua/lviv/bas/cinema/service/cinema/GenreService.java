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
import ua.lviv.bas.cinema.domain.cinema.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreListResponse;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.mapper.cinema.GenreMapper;
import ua.lviv.bas.cinema.repository.cinema.GenreRepository;

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
		String genreName = request.name();
		log.info("Creating genre: {}", genreName);

		validateGenreUniqueness(genreName, null);

		Genre genre = genreMapper.toGenre(request);
		Genre savedGenre = genreRepository.save(genre);

		log.debug("Genre created with ID: {}", savedGenre.getId());
		return genreMapper.toGenreResponse(savedGenre);
	}

	@Cacheable(key = "'genres-' + #query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<GenreListResponse> getGenres(String query, Pageable pageable) {
		log.info("Getting genres: query='{}', page={}, size={}", query, pageable.getPageNumber(),
				pageable.getPageSize());
		return genreRepository.findGenresByQuery(query, pageable).map(genreMapper::toGenreListResponse);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(allEntries = true) })
	@Transactional
	public GenreResponse updateGenre(Long id, GenreRequest request) {
		String genreName = request.name();
		log.info("Updating genre with id: {}, new name: {}", id, genreName);

		Genre existingGenre = genreRepository.findById(id).orElseThrow(() -> new GenreNotFoundException(id));

		validateGenreUniqueness(genreName, id);
		genreMapper.updateGenreFromRequest(request, existingGenre);

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

	private void validateGenreUniqueness(String name, Long excludeId) {
		boolean exists = excludeId != null ? genreRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId)
				: genreRepository.existsByNameIgnoreCase(name);

		if (exists) {
			throw new DuplicateEntityException("Genre", name);
		}
	}
}