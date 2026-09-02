package ua.lviv.bas.cinema.movie.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.dto.request.MovieCreateRequest;
import ua.lviv.bas.cinema.movie.dto.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.movie.dto.response.MovieAdminResponse;
import ua.lviv.bas.cinema.movie.dto.response.MovieCardResponse;
import ua.lviv.bas.cinema.movie.dto.response.MovieDetailResponse;
import ua.lviv.bas.cinema.movie.dto.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieHasSessionsException;
import ua.lviv.bas.cinema.movie.mapper.MovieMapper;
import ua.lviv.bas.cinema.movie.repository.GenreRepository;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.movie.repository.PersonRepository;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.movie.repository.specification.MovieSpecification;
import ua.lviv.bas.cinema.common.UniquenessValidator;
import ua.lviv.bas.cinema.service.integration.audit.AuditDetails;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.integration.file.PosterService;
import ua.lviv.bas.cinema.service.integration.slug.SlugService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final PersonRepository personRepository;
    private final MovieMapper movieMapper;
    private final SlugService slugService;
    private final MovieStatusCalculator movieStatusCalculator;
    private final PosterService posterService;
    private final AuditService auditService;
    private final SessionRepository sessionRepository;
    private final MovieSpecification movieSpecification;

    @CacheEvict(value = "movieLists", allEntries = true)
    @Transactional
    public MovieAdminResponse createMovie(MovieCreateRequest request) {
        log.info("Creating movie: {}", request.getTitle());

        if (movieRepository.existsByTitle(request.getTitle())) {
            throw new DuplicateEntityException("Movie", "title '" + request.getTitle() + "'");
        }

        var movie = movieMapper.toMovie(request);
        movie.setSlug(generateUniqueSlug(request.getTitle(), null));
        movie.setStatus(movieStatusCalculator.calculate(movie, LocalDate.now()));

        String posterFileName = null;
        try {
            posterFileName = handlePosterUpload(request.getPosterFile());
            movie.setPosterFileName(posterFileName);
            setMovieRelations(movie, request.getGenreIds(), request.getActorIds(), request.getDirectorIds(),
                    request.getScreenwriterIds());

            var saved = movieRepository.save(movie);
            log.info("Movie created successfully with id: {}", saved.getId());
            auditCreate(saved);
            return movieMapper.toMovieAdminResponse(saved);
        } catch (Exception e) {
            if (posterFileName != null) {
                posterService.deletePoster(posterFileName);
            }
            throw e;
        }
    }

    @Cacheable(value = "singleMovies", key = "#id")
    public MovieAdminResponse getMovie(Long id) {
        return movieRepository.findMovieById(id).map(movieMapper::toMovieAdminResponse)
                .orElseThrow(() -> new EntityNotFoundException("Movie", id));
    }

    public MovieDetailResponse getMovieBySlug(String slug) {
        Movie movie = movieRepository.findMovieBySlug(slug)
                .orElseThrow(() -> new EntityNotFoundException("Movie", slug));

        if (movie.getStatus() == MovieStatus.ARCHIVED) {
            throw new EntityNotFoundException("Movie", slug);
        }

        List<Session> sessions = movieRepository.findSessionsByMovieSlug(slug);

        LocalDateTime now = LocalDateTime.now();
        sessions.removeIf(session -> session.getStartTime() == null || !session.getStartTime().isAfter(now));

        movie.setSessions(new LinkedHashSet<>(sessions));

        return movieMapper.toMovieDetailResponse(movie);
    }

    @Cacheable(value = "movieLists", key = "'list-' + #query + '-' + #status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<MovieCardResponse> getMovies(String query, MovieStatus status, Pageable pageable) {
        log.info("Getting movies: query='{}', status={}, page={}, size={}", query, status, pageable.getPageNumber(),
                pageable.getPageSize());
        Specification<Movie> spec = movieSpecification.forMovies(query, status);
        return movieRepository.findAll(spec, pageable).map(movieMapper::toMovieCardResponse);
    }

    @Cacheable(value = "movieLists", key = "'current-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public List<MovieCardResponse> getCurrentMovies(Pageable pageable) {
        Specification<Movie> spec = movieSpecification.currentMovies();
        return movieRepository.findAll(spec, pageable).map(movieMapper::toMovieCardResponse).getContent();
    }

    @Cacheable(value = "movieLists", key = "'upcoming-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public List<MovieCardResponse> getUpcomingMovies(Pageable pageable) {
        Specification<Movie> spec = movieSpecification.upcomingMovies();
        return movieRepository.findAll(spec, pageable).map(movieMapper::toMovieCardResponse).getContent();
    }

    @Cacheable(value = "movieLists", key = "'leaving-soon-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public List<MovieCardResponse> getLeavingSoonMovies(Pageable pageable) {
        Specification<Movie> spec = movieSpecification.leavingSoonMovies();
        return movieRepository.findAll(spec, pageable).map(movieMapper::toMovieCardResponse).getContent();
    }

    public List<MovieSessionSearchResponse> searchMovies(String query, LocalDate date) {
        log.info("Searching movies with query: '{}', date: {}", query, date);

        Specification<Movie> spec;

        if (date != null) {
            spec = (query != null && !query.isBlank())
                    ? movieSpecification.byDateAndTitle(date, query)
                    : movieSpecification.byDate(date);
        } else if (query != null && !query.isBlank()) {
            spec = isValidDate(query)
                    ? movieSpecification.byDate(LocalDate.parse(query))
                    : movieSpecification.forPublicListing(query);
        } else {
            return List.of();
        }

        Sort sort = Sort.by(Sort.Direction.ASC, "title");
        return movieRepository.findAll(spec, sort).stream()
                .map(movieMapper::toMovieSessionSearchResponse)
                .toList();
    }

    public ResponseEntity<byte[]> getPoster(Long id) {
        return movieRepository.findPosterFileNameById(id).map(posterService::getPosterResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @CacheEvict(value = {"singleMovies", "movieDetails", "movieLists"}, allEntries = true)
    @Transactional
    public MovieAdminResponse updateMovie(Long id, MovieUpdateRequest request) {
        log.info("Updating movie with id: {}", id);

        var movie = movieRepository.findMovieById(id).orElseThrow(() -> new EntityNotFoundException("Movie", id));
        String oldTitle = movie.getTitle();

        if (!request.getTitle().equals(oldTitle) && movieRepository.existsByTitle(request.getTitle())) {
            throw new DuplicateEntityException("Movie", "title '" + request.getTitle() + "'");
        }

        movieMapper.updateMovieFromRequest(request, movie);

        if (!movie.getTitle().equals(oldTitle)) {
            movie.setSlug(generateUniqueSlug(movie.getTitle(), id));
        }

        handlePoster(movie, request.getPosterFile(), Boolean.TRUE.equals(request.getRemovePoster()));
        movie.setStatus(movieStatusCalculator.calculate(movie, LocalDate.now()));
        setMovieRelations(movie, request.getGenreIds(), request.getActorIds(), request.getDirectorIds(),
                request.getScreenwriterIds());

        var updated = movieRepository.save(movie);
        log.info("Movie updated successfully with id: {}", updated.getId());
        auditUpdate(id, oldTitle, updated);

        return movieMapper.toMovieAdminResponse(updated);
    }

    @CacheEvict(value = {"singleMovies", "movieDetails", "movieLists"}, allEntries = true)
    @Transactional
    public void deleteMovie(Long id) {
        log.info("Deleting movie with id: {}", id);

        var movie = movieRepository.findMovieById(id).orElseThrow(() -> new EntityNotFoundException("Movie", id));

        checkMovieUsageInSessions(movie);

        if (movie.getPosterFileName() != null) {
            posterService.deletePoster(movie.getPosterFileName());
        }

        movieRepository.delete(movie);
        log.info("Movie deleted successfully with id: {}", id);
        auditDelete(id, movie.getTitle());
    }

    private String generateUniqueSlug(String title, Long excludeId) {
        String slug = slugService.generateUniqueSlug(title);
        UniquenessValidator.validate(excludeId, () -> movieRepository.findBySlug(slug).isPresent(),
                id -> !slugService.isSlugAvailableForMovie(slug, id),
                () -> new DuplicateEntityException("Movie", "slug " + slug));
        return slug;
    }

    private void setMovieRelations(Movie movie, List<Long> genreIds, List<Long> actorIds, List<Long> directorIds,
                                   List<Long> screenwriterIds) {
        movie.setGenres(new HashSet<>(genreRepository.findAllById(genreIds)));
        movie.setActors(new HashSet<>(personRepository.findAllById(actorIds)));
        movie.setDirectors(new HashSet<>(personRepository.findAllById(directorIds)));
        movie.setScreenwriters(new HashSet<>(personRepository.findAllById(screenwriterIds)));
    }

    private String handlePosterUpload(MultipartFile posterFile) {
        if (posterFile != null && !posterFile.isEmpty()) {
            return posterService.uploadPoster(posterFile);
        }
        return null;
    }

    private void handlePoster(Movie movie, MultipartFile posterFile, boolean removePoster) {
        if (posterFile != null && !posterFile.isEmpty()) {
            if (movie.getPosterFileName() != null) {
                posterService.deletePoster(movie.getPosterFileName());
            }
            movie.setPosterFileName(posterService.uploadPoster(posterFile));
            log.debug("Poster updated for movie: {}", movie.getId());
        } else if (removePoster && movie.getPosterFileName() != null) {
            posterService.deletePoster(movie.getPosterFileName());
            movie.setPosterFileName(null);
            log.debug("Poster removed for movie: {}", movie.getId());
        }
    }

    private boolean isValidDate(String query) {
        try {
            LocalDate.parse(query);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void checkMovieUsageInSessions(Movie movie) {
        long sessionCount = sessionRepository.countByMovieId(movie.getId());
        if (sessionCount > 0) {
            throw new MovieHasSessionsException(movie.getTitle(), sessionCount);
        }
    }

    private void auditCreate(Movie movie) {
        auditService.logChange("Movie", movie.getId(), movie.getTitle(), AuditAction.CREATED, null,
                AuditDetails.of().put("title", movie.getTitle()).put("slug", movie.getSlug())
                        .put("durationMinutes", movie.getDurationMinutes()).build());
    }

    private void auditUpdate(Long id, String oldTitle, Movie updated) {
        auditService.logChange("Movie", id, oldTitle, AuditAction.UPDATED,
                AuditDetails.of().put("title", oldTitle).build(),
                AuditDetails.of().put("title", updated.getTitle()).put("slug", updated.getSlug()).build());
    }

    private void auditDelete(Long id, String title) {
        auditService.logChange("Movie", id, title, AuditAction.DELETED,
                AuditDetails.of().put("deleted", title).build(), null);
    }
}