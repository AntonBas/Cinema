package ua.lviv.bas.cinema.service.cinema;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.projection.GenreProjection;
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
		log.info("Creating genre: {}", request.getName());

		String genreName = request.getName().trim();
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

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(key = "'projection-' + #id") })
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

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(key = "'projection-' + #id") })
	@Transactional
	public void deleteGenre(Long id) {
		log.info("Deleting genre with id: {}", id);

		if (!genreRepository.existsById(id)) {
			throw new GenreNotFoundException(id);
		}

		genreRepository.deleteById(id);
		log.debug("Genre deleted with ID: {}", id);
	}

	@Cacheable(key = "'projections-page-' + #pageable")
	public Page<GenreProjection> getGenreProjectionsPage(Pageable pageable) {
		log.debug("Retrieving genre projections with pagination");
		return genreRepository.findAllProjections(pageable);
	}

	@Cacheable(key = "'search-projections-' + #query + '-' + #pageable")
	public Page<GenreProjection> searchGenreProjections(String query, Pageable pageable) {
		log.info("Searching genre projections: query='{}'", query);

		if (!StringUtils.hasText(query)) {
			return getGenreProjectionsPage(pageable);
		}

		return genreRepository.searchProjectionsByName(query.trim(), pageable);
	}

	@Cacheable(key = "'search-popular-' + #query + '-' + #limit")
	public List<GenreResponse> searchPopularGenres(String query, int limit) {
		log.info("Searching popular genres: query='{}', limit={}", query, limit);

		List<GenreProjection> projections;

		if (StringUtils.hasText(query)) {
			Page<GenreProjection> page = genreRepository.searchProjectionsByName(query.trim(), PageRequest.of(0, 100));
			projections = page.getContent();
		} else {
			Page<GenreProjection> page = genreRepository.findAllProjections(PageRequest.of(0, 100));
			projections = page.getContent();
		}

		return projections.stream().sorted((a, b) -> Integer.compare(b.getMovieCount(), a.getMovieCount())).limit(limit)
				.map(proj -> GenreResponse.builder().id(proj.getId()).name(proj.getName()).build())
				.collect(Collectors.toList());
	}

	@Cacheable(key = "'by-ids-' + #ids.hashCode()")
	public List<GenreResponse> getGenresByIds(List<Long> ids) {
		log.debug("Retrieving genres by ids: {}", ids);

		if (ids == null || ids.isEmpty()) {
			return List.of();
		}

		List<Genre> genres = genreRepository.findAllById(ids);
		return genreMapper.toGenreResponseList(genres);
	}

	public boolean existsByName(String name) {
		return genreRepository.existsByNameIgnoreCase(name.trim());
	}

	private void validateGenreUniqueness(String name, Long excludeId) {
		boolean exists = excludeId != null ? genreRepository.existsByNameIgnoreCaseAndIdNot(name, excludeId)
				: genreRepository.existsByNameIgnoreCase(name);

		if (exists) {
			throw new DuplicateEntityException("Genre", name);
		}
	}
}