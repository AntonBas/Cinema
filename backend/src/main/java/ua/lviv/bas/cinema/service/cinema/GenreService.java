package ua.lviv.bas.cinema.service.cinema;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import ua.lviv.bas.cinema.exception.domain.cinema.GenreHasMoviesException;
import ua.lviv.bas.cinema.exception.domain.cinema.GenreNotFoundException;
import ua.lviv.bas.cinema.mapper.cinema.GenreMapper;
import ua.lviv.bas.cinema.repository.cinema.GenreRepository;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;

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

        var genre = genreRepository.findById(id).orElseThrow(() -> new GenreNotFoundException(id));
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

        var genre = genreRepository.findById(id).orElseThrow(() -> new GenreNotFoundException(id));
        checkGenreUsageInMovies(genre);
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

    private void checkGenreUsageInMovies(Genre genre) {
        long usageCount = movieRepository.countMovieUsageByGenreId(genre.getId());
        if (usageCount > 0) {
            throw new GenreHasMoviesException(genre.getId(), genre.getName(), usageCount);
        }
    }
}