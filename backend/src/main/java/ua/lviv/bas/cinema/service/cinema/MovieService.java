package ua.lviv.bas.cinema.service.cinema;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
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
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieValidationException;
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
	public MovieDetailResponse createMovie(MovieCreateRequest request) {
		log.info("Creating movie: {}", request.getTitle());

		validateDates(request.getReleaseDate(), request.getEndShowingDate());

		String slug = slugService.generateUniqueSlug(request.getTitle());
		validateSlugUniqueness(slug);

		Movie movie = movieMapper.toMovie(request);
		movie.setSlug(slug);
		movie.setStatus(movieScheduler.calculateMovieStatus(movie, LocalDate.now()));

		handlePoster(movie, request.getPosterFile(), false);
		setMovieRelations(movie, request.getGenreIds(), request.getActorIds(), request.getDirectorIds(),
				request.getScreenwriterIds(), false);

		Movie saved = movieRepository.save(movie);
		log.info("Movie created successfully with id: {}", saved.getId());

		Map<String, Object> details = new HashMap<>();
		details.put("title", saved.getTitle());
		details.put("slug", saved.getSlug());
		details.put("durationMinutes", saved.getDurationMinutes());

		auditService.logChange("Movie", saved.getId(), saved.getTitle(), AuditAction.CREATED, null, details);

		return movieMapper.toMovieDetailResponse(saved);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(allEntries = true) })
	@Transactional
	public MovieDetailResponse updateMovie(Long id, MovieUpdateRequest request) {
		log.info("Updating movie with id: {}", id);

		Movie existing = findAdminMovieById(id);
		String oldTitle = existing.getTitle();
		validateDates(request.getReleaseDate(), request.getEndShowingDate());

		if (!existing.getTitle().equals(request.getTitle()) && movieRepository.existsByTitle(request.getTitle())) {
			throw new DuplicateEntityException("Movie", "title '" + request.getTitle() + "'");
		}

		movieMapper.updateMovieFromRequest(request, existing);

		if (!existing.getTitle().equals(request.getTitle())) {
			String newSlug = slugService.generateUniqueSlug(request.getTitle());
			if (!slugService.isSlugAvailableForMovie(newSlug, id)) {
				throw new DuplicateEntityException("Movie", "slug " + newSlug);
			}
			existing.setSlug(newSlug);
		}

		handlePoster(existing, request.getPosterFile(), Boolean.TRUE.equals(request.getRemovePoster()));
		existing.setStatus(movieScheduler.calculateMovieStatus(existing, LocalDate.now()));
		updateMovieRelations(existing, request.getGenreIds(), request.getActorIds(), request.getDirectorIds(),
				request.getScreenwriterIds());

		Movie updated = movieRepository.save(existing);
		log.info("Movie updated successfully with id: {}", updated.getId());

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("title", oldTitle);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("title", updated.getTitle());
		newDetails.put("slug", updated.getSlug());

		auditService.logChange("Movie", id, oldTitle, AuditAction.UPDATED, oldDetails, newDetails);

		return movieMapper.toMovieDetailResponse(updated);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(allEntries = true) })
	@Transactional
	public void deleteMovie(Long id) {
		log.info("Deleting movie with id: {}", id);

		Movie movie = findAdminMovieById(id);
		String movieTitle = movie.getTitle();
		if (movie.getPosterFileName() != null) {
			posterService.deletePoster(movie.getPosterFileName());
		}
		movieRepository.delete(movie);
		log.info("Movie deleted successfully with id: {}", id);

		Map<String, Object> details = new HashMap<>();
		details.put("deleted", movieTitle);

		auditService.logChange("Movie", id, movieTitle, AuditAction.DELETED, details, null);
	}

	@Cacheable(key = "#id")
	public MovieDetailResponse getMovieById(Long id) {
		return movieRepository.findDetailProjectionById(id).map(movieMapper::toMovieDetailResponse)
				.orElseThrow(() -> new MovieNotFoundException(id));
	}

	public MovieDetailResponse getAdminMovieById(Long id) {
		Movie movie = findAdminMovieById(id);
		return movieMapper.toMovieDetailResponse(movie);
	}

	@Cacheable(key = "#slug")
	public MovieDetailResponse getMovieBySlug(String slug) {
		Movie movie = movieRepository.findBySlug(slug).orElseThrow(() -> new MovieNotFoundException(slug));
		return movieMapper.toMovieDetailResponse(movie);
	}

	public MovieDetailResponse getAdminMovieBySlug(String slug) {
		Movie movie = movieRepository.findAdminMovieBySlug(slug).orElseThrow(() -> new MovieNotFoundException(slug));
		return movieMapper.toMovieDetailResponse(movie);
	}

	@Cacheable(key = "'filtered-' + #title + '-' + #status + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<MovieCardResponse> getFilteredMovies(String title, MovieStatus status, Pageable pageable) {
		return movieRepository.findMoviesByFilters(title, status, pageable).map(movieMapper::toMovieCardResponse);
	}

	@Cacheable(key = "'now-showing-home-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public List<MovieCardResponse> getNowShowingMoviesForHome(Pageable pageable) {
		log.info("Fetching now showing movies for home page");
		Page<MovieCardProjection> page = movieRepository.findNowShowingMovies(pageable);
		return page.getContent().stream().map(movieMapper::toMovieCardResponse).toList();
	}

	@Cacheable(key = "'coming-soon-home-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public List<MovieCardResponse> getComingSoonMoviesForHome(Pageable pageable) {
		log.info("Fetching coming soon movies for home page");
		Page<MovieCardProjection> page = movieRepository.findComingSoonMovies(pageable);
		return page.getContent().stream().map(movieMapper::toMovieCardResponse).toList();
	}

	@Cacheable(key = "'leaving-soon-home-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public List<MovieCardResponse> getLeavingSoonMoviesForHome(Pageable pageable) {
		log.info("Fetching leaving soon movies for home page");
		Page<MovieCardProjection> page = movieRepository.findLeavingSoonMovies(pageable);
		return page.getContent().stream().map(movieMapper::toMovieCardResponse).toList();
	}

	public List<MovieSessionSearchResponse> searchMoviesForSession(String searchTerm) {
		log.info("Searching movies for session with term: {}", searchTerm);

		if (searchTerm == null || searchTerm.isBlank()) {
			return List.of();
		}

		List<MovieCardProjection> projections;

		if (isValidDate(searchTerm)) {
			LocalDate date = LocalDate.parse(searchTerm);
			projections = movieRepository.findMoviesByDate(date);
		} else {
			projections = movieRepository.findMoviesForSession(searchTerm);
		}

		return projections.stream().map(movieMapper::toMovieSessionSearchResponse).toList();
	}

	public ResponseEntity<byte[]> getMoviePoster(Long id) {
		return movieRepository.findPosterFileNameById(id).map(posterService::getPosterResponse)
				.orElse(ResponseEntity.notFound().build());
	}

	public boolean existsBySlug(String slug) {
		return movieRepository.findBySlug(slug).isPresent();
	}

	private Movie findAdminMovieById(Long id) {
		return movieRepository.findAdminMovieById(id).orElseThrow(() -> new MovieNotFoundException(id));
	}

	private void validateDates(LocalDate releaseDate, LocalDate endShowingDate) {
		if (endShowingDate.isBefore(releaseDate)) {
			throw MovieValidationException.invalidDates(releaseDate, endShowingDate);
		}
		if (endShowingDate.isBefore(LocalDate.now())) {
			throw MovieValidationException.endDateInPast(endShowingDate);
		}
	}

	private void validateSlugUniqueness(String slug) {
		if (movieRepository.findBySlug(slug).isPresent()) {
			throw new DuplicateEntityException("Movie", "slug " + slug);
		}
	}

	private void setMovieRelations(Movie movie, List<Long> genreIds, List<Long> actorIds, List<Long> directorIds,
			List<Long> screenwriterIds, boolean clearFirst) {
		if (clearFirst) {
			movie.getGenres().clear();
			movie.getActors().clear();
			movie.getDirectors().clear();
			movie.getScreenwriters().clear();
		}
		movie.setGenres(new HashSet<>(genreRepository.findAllById(genreIds)));
		movie.setActors(new HashSet<>(personRepository.findAllById(actorIds)));
		movie.setDirectors(new HashSet<>(personRepository.findAllById(directorIds)));
		movie.setScreenwriters(new HashSet<>(personRepository.findAllById(screenwriterIds)));
	}

	private void updateMovieRelations(Movie movie, List<Long> genreIds, List<Long> actorIds, List<Long> directorIds,
			List<Long> screenwriterIds) {
		setMovieRelations(movie, genreIds, actorIds, directorIds, screenwriterIds, true);
	}

	private void handlePoster(Movie movie, MultipartFile posterFile, boolean removePoster) {
		if (posterFile != null && !posterFile.isEmpty()) {
			if (movie.getPosterFileName() != null) {
				posterService.deletePoster(movie.getPosterFileName());
			}
			String newPosterFileName = posterService.uploadPoster(posterFile);
			movie.setPosterFileName(newPosterFileName);
			log.debug("Poster updated: {}", newPosterFileName);
		} else if (removePoster && movie.getPosterFileName() != null) {
			posterService.deletePoster(movie.getPosterFileName());
			movie.setPosterFileName(null);
			log.debug("Poster removed");
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
}