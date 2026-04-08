package ua.lviv.bas.cinema.service.cinema;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieAdminResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.mapper.cinema.MovieMapper;
import ua.lviv.bas.cinema.repository.cinema.GenreRepository;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;
import ua.lviv.bas.cinema.repository.cinema.PersonRepository;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieCardProjection;
import ua.lviv.bas.cinema.scheduler.MovieScheduler;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.integration.file.PosterService;
import ua.lviv.bas.cinema.service.integration.slug.SlugService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "movies")
public class MovieService {

	private final MovieRepository movieRepository;
	private final GenreRepository genreRepository;
	private final PersonRepository personRepository;
	private final MovieMapper movieMapper;
	private final SlugService slugService;
	private final MovieScheduler movieScheduler;
	private final PosterService posterService;
	private final AuditService auditService;

	@CacheEvict(allEntries = true)
	@Transactional
	public MovieAdminResponse createMovie(MovieCreateRequest request) {
		log.info("Creating movie: {}", request.getTitle());

		Movie movie = movieMapper.toMovie(request);
		movie.setSlug(generateUniqueSlug(request.getTitle(), null));
		movie.setStatus(movieScheduler.calculateMovieStatus(movie, LocalDate.now()));

		String posterFileName = null;
		try {
			posterFileName = handlePosterUpload(request.getPosterFile());
			movie.setPosterFileName(posterFileName);
			setMovieRelations(movie, request.getGenreIds(), request.getActorIds(), request.getDirectorIds(),
					request.getScreenwriterIds());

			Movie saved = movieRepository.save(movie);
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

	@Cacheable(key = "#id")
	public MovieAdminResponse getAdminMovieById(Long id) {
		return movieRepository.findById(id).map(movieMapper::toMovieAdminResponse)
				.orElseThrow(() -> new MovieNotFoundException(id));
	}

	public MovieDetailResponse getMovieBySlug(String slug) {
		return movieRepository.findBySlugWithFutureSessions(slug)
				.filter(movie -> movie.getStatus() != MovieStatus.ARCHIVED).map(movieMapper::toMovieDetailResponse)
				.orElseThrow(() -> new MovieNotFoundException(slug));
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(allEntries = true) })
	@Transactional
	public MovieAdminResponse updateMovie(Long id, MovieUpdateRequest request) {
		log.info("Updating movie with id: {}", id);

		Movie movie = movieRepository.findMovieById(id).orElseThrow(() -> new MovieNotFoundException(id));
		String oldTitle = movie.getTitle();

		if (!request.getTitle().equals(oldTitle) && movieRepository.existsByTitle(request.getTitle())) {
			throw new DuplicateEntityException("Movie", "title '" + request.getTitle() + "'");
		}

		movieMapper.updateMovieFromRequest(request, movie);

		if (!movie.getTitle().equals(oldTitle)) {
			movie.setSlug(generateUniqueSlug(movie.getTitle(), id));
		}

		handlePoster(movie, request.getPosterFile(), Boolean.TRUE.equals(request.getRemovePoster()));
		movie.setStatus(movieScheduler.calculateMovieStatus(movie, LocalDate.now()));
		setMovieRelations(movie, request.getGenreIds(), request.getActorIds(), request.getDirectorIds(),
				request.getScreenwriterIds());

		Movie updated = movieRepository.save(movie);
		log.info("Movie updated successfully with id: {}", updated.getId());
		auditUpdate(id, oldTitle, updated);

		return movieMapper.toMovieAdminResponse(updated);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(allEntries = true) })
	@Transactional
	public void deleteMovie(Long id) {
		log.info("Deleting movie with id: {}", id);

		Movie movie = movieRepository.findMovieById(id).orElseThrow(() -> new MovieNotFoundException(id));

		if (movie.getPosterFileName() != null) {
			posterService.deletePoster(movie.getPosterFileName());
		}

		movieRepository.delete(movie);
		log.info("Movie deleted successfully with id: {}", id);
		auditDelete(id, movie.getTitle());
	}

	@Cacheable(key = "'filtered-' + #title + '-' + #status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<MovieCardResponse> getFilteredMovies(String title, MovieStatus status, Pageable pageable) {
		return movieRepository.findMoviesByFilters(title, status, pageable).map(movieMapper::toMovieCardResponse);
	}

	@Cacheable(key = "'now-showing-home-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public List<MovieCardResponse> getNowShowingMoviesForHome(Pageable pageable) {
		return movieRepository.findNowShowingMovies(pageable).map(movieMapper::toMovieCardResponse).getContent();
	}

	@Cacheable(key = "'coming-soon-home-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public List<MovieCardResponse> getComingSoonMoviesForHome(Pageable pageable) {
		return movieRepository.findComingSoonMovies(pageable).map(movieMapper::toMovieCardResponse).getContent();
	}

	@Cacheable(key = "'leaving-soon-home-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public List<MovieCardResponse> getLeavingSoonMoviesForHome(Pageable pageable) {
		return movieRepository.findLeavingSoonMovies(pageable).map(movieMapper::toMovieCardResponse).getContent();
	}

	public List<MovieSessionSearchResponse> searchMoviesForSession(String searchTerm) {
		log.info("Searching movies for session with term: {}", searchTerm);

		if (searchTerm == null || searchTerm.isBlank()) {
			return List.of();
		}

		List<MovieCardProjection> projections = isValidDate(searchTerm)
				? movieRepository.findMoviesByDate(LocalDate.parse(searchTerm))
				: movieRepository.findMoviesForSession(searchTerm);

		return projections.stream().map(movieMapper::toMovieSessionSearchResponse).toList();
	}

	public ResponseEntity<byte[]> getMoviePoster(Long id) {
		return movieRepository.findPosterFileNameById(id).map(posterService::getPosterResponse)
				.orElse(ResponseEntity.notFound().build());
	}

	private String generateUniqueSlug(String title, Long excludeId) {
		String slug = slugService.generateUniqueSlug(title);
		boolean exists = excludeId != null ? !slugService.isSlugAvailableForMovie(slug, excludeId)
				: movieRepository.findBySlug(slug).isPresent();

		if (exists) {
			throw new DuplicateEntityException("Movie", "slug " + slug);
		}
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

	private boolean isValidDate(String searchTerm) {
		try {
			LocalDate.parse(searchTerm);
			return true;
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	private void auditCreate(Movie movie) {
		auditService.logChange("Movie", movie.getId(), movie.getTitle(), AuditAction.CREATED, null, Map.of("title",
				movie.getTitle(), "slug", movie.getSlug(), "durationMinutes", movie.getDurationMinutes()));
	}

	private void auditUpdate(Long id, String oldTitle, Movie updated) {
		auditService.logChange("Movie", id, oldTitle, AuditAction.UPDATED, Map.of("title", oldTitle),
				Map.of("title", updated.getTitle(), "slug", updated.getSlug()));
	}

	private void auditDelete(Long id, String title) {
		auditService.logChange("Movie", id, title, AuditAction.DELETED, Map.of("deleted", title), null);
	}
}