package ua.lviv.bas.cinema.movie.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.movie.domain.Genre;
import ua.lviv.bas.cinema.movie.dto.request.GenreRequest;
import ua.lviv.bas.cinema.movie.dto.response.GenreListResponse;
import ua.lviv.bas.cinema.movie.dto.response.GenreResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreHasMoviesException;
import ua.lviv.bas.cinema.movie.mapper.GenreMapper;
import ua.lviv.bas.cinema.movie.repository.GenreRepository;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.common.UniquenessValidator;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GenreService {

    private final GenreRepository genreRepository;
    private final GenreMapper genreMapper;
    private final MovieRepository movieRepository;

    @CacheEvict(value = "genres", allEntries = true)
    @Transactional
    public GenreResponse createGenre(GenreRequest request) {
        log.info("Creating genre: {}", request.name());
        validateGenreUniqueness(request.name(), null);

        var genre = genreMapper.toGenre(request);
        var saved = genreRepository.save(genre);

        log.debug("Genre created with ID: {}", saved.getId());
        return genreMapper.toGenreResponse(saved);
    }

    @Cacheable(value = "genres", key = "'list-' + #query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<GenreListResponse> getGenres(String query, Pageable pageable) {
        log.info("Getting genres: query='{}', page={}, size={}", query, pageable.getPageNumber(),
                pageable.getPageSize());
        return genreRepository.findGenresByFilters(query, pageable).map(genreMapper::toGenreListResponse);
    }

    @CacheEvict(value = "genres", allEntries = true)
    @Transactional
    public GenreResponse updateGenre(Long id, GenreRequest request) {
        log.info("Updating genre with id: {}, new name: {}", id, request.name());

        var genre = genreRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Genre", id));
        validateGenreUniqueness(request.name(), id);

        genreMapper.updateGenreFromRequest(request, genre);
        var updated = genreRepository.save(genre);

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