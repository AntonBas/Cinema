package ua.lviv.bas.cinema.movie.genre;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.audit.AuditAction;
import ua.lviv.bas.cinema.audit.AuditDetails;
import ua.lviv.bas.cinema.audit.AuditService;
import ua.lviv.bas.cinema.movie.genre.dto.GenreRequest;
import ua.lviv.bas.cinema.common.CacheableList;
import ua.lviv.bas.cinema.movie.genre.dto.GenreListResponse;
import ua.lviv.bas.cinema.movie.genre.dto.GenreResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.movie.exception.GenreHasMoviesException;
import ua.lviv.bas.cinema.movie.movie.MovieRepository;
import ua.lviv.bas.cinema.common.UniquenessValidator;
import ua.lviv.bas.cinema.common.FixedOrderPageable;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;
    private final MovieRepository movieRepository;
    private final AuditService auditService;

    @CacheEvict(value = "genres", allEntries = true)
    @Transactional
    public GenreResponse createGenre(GenreRequest request) {
        log.info("Creating genre: {}", request.name());
        validateGenreUniqueness(request.name(), null);

        var genre = genreMapper.toEntity(request);
        var saved = genreRepository.save(genre);

        auditService.logChange("Genre", saved.getId(), saved.getName(), AuditAction.CREATED, null,
                AuditDetails.of().put("name", saved.getName()).build());
        log.debug("Genre created with ID: {}", saved.getId());
        return genreMapper.toGenreResponse(saved);
    }

    @Cacheable(value = "genres", key = "'list-' + #query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    public Page<GenreListResponse> getGenres(String query, Pageable pageable) {
        log.info("Getting genres: query='{}', page={}, size={}", query, pageable.getPageNumber(),
                pageable.getPageSize());
        return genreRepository.findGenresByFilters(query, FixedOrderPageable.of(pageable)).map(genreMapper::toGenreListResponse);
    }

    @Cacheable(value = "genres", key = "'all'")
    public List<GenreResponse> getAllGenres() {
        return new CacheableList<>(genreRepository.findAll(Sort.by("name")).stream().map(genreMapper::toGenreResponse)
                .toList());
    }

    @CacheEvict(value = "genres", allEntries = true)
    @Transactional
    public GenreResponse updateGenre(Long id, GenreRequest request) {
        log.info("Updating genre with id: {}, new name: {}", id, request.name());

        var genre = genreRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Genre", id));
        validateGenreUniqueness(request.name(), id);
        var oldName = genre.getName();

        genreMapper.updateEntity(request, genre);
        var updated = genreRepository.save(genre);
        auditService.logChange("Genre", id, oldName, AuditAction.UPDATED,
                AuditDetails.of().put("name", oldName).build(),
                AuditDetails.of().put("name", updated.getName()).build());

        log.debug("Genre updated with ID: {}", updated.getId());
        return genreMapper.toGenreResponse(updated);
    }

    @CacheEvict(value = "genres", allEntries = true)
    @Transactional
    public void deleteGenre(Long id) {
        log.info("Deleting genre with id: {}", id);

        var genre = genreRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Genre", id));
        checkGenreUsageInMovies(genre);
        genreRepository.deleteById(id);
        auditService.logChange("Genre", id, genre.getName(), AuditAction.DELETED,
                AuditDetails.of().put("deleted", genre.getName()).build(), null);

        log.debug("Genre deleted with ID: {}", id);
    }

    private void validateGenreUniqueness(String name, Long excludeId) {
        UniquenessValidator.validate(excludeId, () -> genreRepository.existsByNameIgnoreCase(name),
                id -> genreRepository.existsByNameIgnoreCaseAndIdNot(name, id),
                () -> new DuplicateEntityException("Genre", name));
    }

    private void checkGenreUsageInMovies(Genre genre) {
        long usageCount = movieRepository.countMovieUsageByGenreId(genre.getId());
        if (usageCount > 0) {
            throw new GenreHasMoviesException(genre.getId(), genre.getName(), usageCount);
        }
    }
}