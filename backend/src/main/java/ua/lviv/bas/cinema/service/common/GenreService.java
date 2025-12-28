package ua.lviv.bas.cinema.service.common;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.QGenre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
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

		Genre genre = genreMapper.toEntity(request);
		genre.setName(genreName);
		Genre savedGenre = genreRepository.save(genre);

		log.debug("Genre created with ID: {}", savedGenre.getId());
		return genreMapper.toDto(savedGenre);
	}

	public GenreResponse getGenreById(Long id) {
		log.debug("Retrieving genre by id: {}", id);

		return genreRepository.findById(id).map(genreMapper::toDto).orElseThrow(() -> new GenreNotFoundException(id));
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
		return genreMapper.toDto(updatedGenre);
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

	public List<GenreResponse> getAllGenres() {
		log.debug("Retrieving all genres");
		return genreMapper.toDtoList(genreRepository.findAll());
	}

	public List<GenreResponse> getGenresByIds(List<Long> ids) {
		log.debug("Retrieving genres by ids: {}", ids);

		if (ids == null || ids.isEmpty()) {
			return List.of();
		}

		List<Genre> genres = genreRepository.findAllById(ids);
		return genreMapper.toDtoList(genres);
	}

	public PageResponse<GenreResponse> searchGenres(String query, Pageable pageable) {
		log.info("Searching genres: query='{}', pageable={}", query, pageable);

		QGenre qGenre = QGenre.genre;
		BooleanBuilder predicate = new BooleanBuilder();

		if (StringUtils.hasText(query)) {
			String searchQuery = query.trim().toLowerCase();
			predicate.and(qGenre.name.toLowerCase().contains(searchQuery));
		}

		Page<Genre> genrePage = genreRepository.findAll(predicate, pageable);

		log.debug("Found {} genres for query: '{}'", genrePage.getTotalElements(), query);
		return PageResponse.of(genrePage, genreMapper::toDto);
	}

	public PageResponse<GenreResponse> getAllGenresPaginated(Pageable pageable) {
		log.debug("Retrieving all genres with pagination");
		Page<Genre> genrePage = genreRepository.findAll(pageable);
		return PageResponse.of(genrePage, genreMapper::toDto);
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