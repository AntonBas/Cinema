package ua.lviv.bas.cinema.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;
import ua.lviv.bas.cinema.mapper.MovieMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

	private final MovieRepository movieRepository;
	private final GenreRepository genreRepository;
	private final PersonRepository personRepository;
	private final MovieMapper movieMapper;
	private final SlugService slugService;

	@Value("${app.upload.dir:uploads}")
	private String uploadDir;

	@Transactional
	public MovieDetailResponse createMovie(MovieCreateRequest request) {
		log.info("Creating movie: {}", request.getTitle());

		validateCreateRequest(request);

		String slug = slugService.generateUniqueSlug(request.getTitle());
		validateSlugUniqueness(slug);

		Movie movie = movieMapper.toEntity(request);
		movie.setSlug(slug);
		movie.setStatus(calculateStatus(movie, LocalDate.now()));

		handlePosterUpload(request.getPosterFile(), movie);
		setMovieRelations(movie, request);

		Movie saved = movieRepository.save(movie);
		return enrichWithComputedFields(saved);
	}

	@Transactional
	public MovieDetailResponse updateMovie(Long id, MovieUpdateRequest request) {
		log.info("Updating movie with id: {}", id);

		Movie existing = findMovieById(id);
		validateUpdateRequest(request);

		movieMapper.updateEntityFromRequest(request, existing);

		if (!existing.getTitle().equals(request.getTitle())) {
			String newSlug = slugService.generateUniqueSlug(request.getTitle());
			if (!slugService.isSlugAvailableForMovie(newSlug, id)) {
				throw new DuplicateEntityException("Movie", "slug " + newSlug);
			}
			existing.setSlug(newSlug);
		}

		handlePosterUpdate(existing, null, request.getRemovePoster());
		existing.setStatus(calculateStatus(existing, LocalDate.now()));
		updateMovieRelations(existing, request);

		Movie updated = movieRepository.save(existing);
		return enrichWithComputedFields(updated);
	}

	@Transactional
	public void deleteMovie(Long id) {
		log.info("Deleting movie with id: {}", id);

		Movie movie = findMovieById(id);
		deletePosterFile(movie.getPosterFileName());
		movieRepository.delete(movie);
	}

	@Transactional(readOnly = true)
	public MovieDetailResponse getMovieById(Long id) {
		Movie movie = findMovieById(id);
		return enrichWithComputedFields(movie);
	}

	@Transactional(readOnly = true)
	public MovieDetailResponse getMovieBySlug(String slug) {
		Movie movie = movieRepository.findBySlug(slug).orElseThrow(() -> new MovieNotFoundException(slug));
		return enrichWithComputedFields(movie);
	}

	@Transactional(readOnly = true)
	public List<MovieDetailResponse> getAllMovies() {
		return movieRepository.findAll().stream().map(this::enrichWithComputedFields).toList();
	}

	@Transactional(readOnly = true)
	public Movie getMovieEntityById(Long id) {
		return findMovieById(id);
	}

	@Transactional(readOnly = true)
	public Page<MovieDetailResponse> getMoviesPaginated(Pageable pageable) {
		return movieRepository.findAll(pageable).map(this::enrichWithComputedFields);
	}

	@Transactional(readOnly = true)
	public List<MovieCardResponse> getCurrentlyShowingMovies() {
		LocalDate today = LocalDate.now();
		return movieRepository.findAll().stream().filter(movie -> calculateStatus(movie, today) == MovieStatus.CURRENT)
				.map(movieMapper::toCardResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<MovieCardResponse> getUpcomingMovies() {
		LocalDate today = LocalDate.now();
		return movieRepository.findAll().stream().filter(movie -> calculateStatus(movie, today) == MovieStatus.UPCOMING)
				.map(movieMapper::toCardResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<MovieCardResponse> getArchivedMovies() {
		LocalDate today = LocalDate.now();
		return movieRepository.findAll().stream().filter(movie -> calculateStatus(movie, today) == MovieStatus.ARCHIVED)
				.map(movieMapper::toCardResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<MovieSessionSearchResponse> searchMoviesForSessionCreation(String searchTerm, LocalDate sessionDate) {
		Page<Movie> movies;

		if (searchTerm != null && !searchTerm.trim().isEmpty()) {
			movies = movieRepository.findByTitleContainingIgnoreCase(searchTerm.trim(),
					PageRequest.of(0, 20, Sort.by("title")));
		} else {
			movies = movieRepository.findAll(PageRequest.of(0, 20, Sort.by("title")));
		}

		return movies.getContent().stream().filter(movie -> isMovieAvailableForDate(movie, sessionDate))
				.map(this::toSessionSearchResponse).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Page<MovieCardResponse> searchMovies(String searchTerm, Pageable pageable) {
		Page<Movie> movies;

		if (searchTerm != null && !searchTerm.trim().isEmpty()) {
			movies = movieRepository.findByTitleContainingIgnoreCase(searchTerm.trim(), pageable);
		} else {
			movies = movieRepository.findAll(pageable);
		}

		return movies.map(movieMapper::toCardResponse);
	}

	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> getMoviePoster(Long id) {
		try {
			Movie movie = findMovieById(id);
			if (movie.getPosterFileName() == null || movie.getPosterFileName().isBlank()) {
				return ResponseEntity.notFound().build();
			}

			Path path = Paths.get(uploadDir, "posters", movie.getPosterFileName());
			if (!Files.exists(path)) {
				return ResponseEntity.notFound().build();
			}

			byte[] data = Files.readAllBytes(path);
			String contentType = determineContentType(movie.getPosterFileName());

			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CACHE_CONTROL, "max-age=3600").body(data);
		} catch (IOException e) {
			log.error("Error loading poster for movie id: {}", id, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Scheduled(cron = "0 0 0 * * ?")
	@Transactional
	public void updateMovieStatuses() {
		LocalDate today = LocalDate.now();
		List<Movie> allMovies = movieRepository.findAll();
		int updatedCount = 0;

		for (Movie movie : allMovies) {
			MovieStatus currentStatus = movie.getStatus();
			MovieStatus newStatus = calculateStatus(movie, today);

			if (currentStatus != newStatus) {
				movie.setStatus(newStatus);
				updatedCount++;
			}
		}

		if (updatedCount > 0) {
			movieRepository.saveAll(allMovies);
		}

		log.info("Movie status update completed. Updated {} movies", updatedCount);
	}

	private Movie findMovieById(Long id) {
		return movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
	}

	private void validateCreateRequest(MovieCreateRequest request) {
		if (request.getEndShowingDate().isBefore(request.getReleaseDate())) {
			throw new IllegalArgumentException("End showing date cannot be before release date");
		}
	}

	private void validateUpdateRequest(MovieUpdateRequest request) {
		if (request.getEndShowingDate().isBefore(request.getReleaseDate())) {
			throw new IllegalArgumentException("End showing date cannot be before release date");
		}
	}

	private void validateSlugUniqueness(String slug) {
		if (movieRepository.findBySlug(slug).isPresent()) {
			throw new DuplicateEntityException("Movie", "slug " + slug);
		}
	}

	private MovieStatus calculateStatus(Movie movie, LocalDate referenceDate) {
		if (referenceDate.isBefore(movie.getReleaseDate())) {
			return MovieStatus.UPCOMING;
		} else if (movie.getEndShowingDate() != null && referenceDate.isAfter(movie.getEndShowingDate())) {
			return MovieStatus.ARCHIVED;
		} else {
			return MovieStatus.CURRENT;
		}
	}

	private void setMovieRelations(Movie movie, MovieCreateRequest request) {
		if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
			movie.setGenres(new HashSet<>(genreRepository.findAllById(request.getGenreIds())));
		}
		if (request.getActorIds() != null && !request.getActorIds().isEmpty()) {
			movie.setActors(new HashSet<>(personRepository.findAllById(request.getActorIds())));
		}
		if (request.getDirectorIds() != null && !request.getDirectorIds().isEmpty()) {
			movie.setDirectors(new HashSet<>(personRepository.findAllById(request.getDirectorIds())));
		}
		if (request.getScreenwriterIds() != null && !request.getScreenwriterIds().isEmpty()) {
			movie.setScreenwriters(new HashSet<>(personRepository.findAllById(request.getScreenwriterIds())));
		}
	}

	private void updateMovieRelations(Movie movie, MovieUpdateRequest request) {
		movie.getGenres().clear();
		movie.getActors().clear();
		movie.getDirectors().clear();
		movie.getScreenwriters().clear();

		if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
			movie.getGenres().addAll(new HashSet<>(genreRepository.findAllById(request.getGenreIds())));
		}
		if (request.getActorIds() != null && !request.getActorIds().isEmpty()) {
			movie.getActors().addAll(new HashSet<>(personRepository.findAllById(request.getActorIds())));
		}
		if (request.getDirectorIds() != null && !request.getDirectorIds().isEmpty()) {
			movie.getDirectors().addAll(new HashSet<>(personRepository.findAllById(request.getDirectorIds())));
		}
		if (request.getScreenwriterIds() != null && !request.getScreenwriterIds().isEmpty()) {
			movie.getScreenwriters().addAll(new HashSet<>(personRepository.findAllById(request.getScreenwriterIds())));
		}
	}

	private void handlePosterUpload(MultipartFile posterFile, Movie movie) {
		if (posterFile != null && !posterFile.isEmpty()) {
			String posterFileName = savePosterFile(posterFile);
			movie.setPosterFileName(posterFileName);
		}
	}

	private void handlePosterUpdate(Movie movie, MultipartFile posterFile, Boolean removePoster) {
		if (posterFile != null && !posterFile.isEmpty()) {
			deletePosterFile(movie.getPosterFileName());
			String newPosterFileName = savePosterFile(posterFile);
			movie.setPosterFileName(newPosterFileName);
		} else if (Boolean.TRUE.equals(removePoster)) {
			deletePosterFile(movie.getPosterFileName());
			movie.setPosterFileName(null);
		}
	}

	private String savePosterFile(MultipartFile file) {
		try {
			String originalFileName = file.getOriginalFilename();
			String extension = Optional.ofNullable(originalFileName).filter(name -> name.contains("."))
					.map(name -> name.substring(name.lastIndexOf("."))).orElse(".jpg");

			String fileName = UUID.randomUUID() + extension;
			Path uploadPath = Paths.get(uploadDir, "posters");
			Files.createDirectories(uploadPath);

			Path filePath = uploadPath.resolve(fileName);
			Files.write(filePath, file.getBytes());

			return fileName;
		} catch (IOException e) {
			log.error("Failed to save poster for movie", e);
			throw new ExternalServiceException("File Storage", e);
		}
	}

	private void deletePosterFile(String fileName) {
		if (fileName == null || fileName.isBlank()) {
			return;
		}

		try {
			Path path = Paths.get(uploadDir, "posters", fileName);
			if (Files.exists(path)) {
				Files.delete(path);
			}
		} catch (IOException e) {
			log.error("Failed to delete poster: {}", fileName, e);
		}
	}

	private String determineContentType(String fileName) {
		if (fileName == null) {
			return MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}

		String lower = fileName.toLowerCase();
		if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (lower.endsWith(".png")) {
			return "image/png";
		} else if (lower.endsWith(".gif")) {
			return "image/gif";
		} else if (lower.endsWith(".webp")) {
			return "image/webp";
		} else {
			return MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}
	}

	private MovieDetailResponse enrichWithComputedFields(Movie movie) {
		MovieDetailResponse dto = movieMapper.toDetailResponse(movie);

		dto.setGenreIds(movie.getGenres().stream().map(genre -> genre.getId()).collect(Collectors.toList()));
		dto.setActorIds(movie.getActors().stream().map(person -> person.getId()).collect(Collectors.toList()));
		dto.setDirectorIds(movie.getDirectors().stream().map(person -> person.getId()).collect(Collectors.toList()));
		dto.setScreenwriterIds(
				movie.getScreenwriters().stream().map(person -> person.getId()).collect(Collectors.toList()));

		return dto;
	}

	private boolean isMovieAvailableForDate(Movie movie, LocalDate sessionDate) {
		return !sessionDate.isBefore(movie.getReleaseDate())
				&& (movie.getEndShowingDate() == null || !sessionDate.isAfter(movie.getEndShowingDate()));
	}

	private MovieSessionSearchResponse toSessionSearchResponse(Movie movie) {
		return MovieSessionSearchResponse.builder().id(movie.getId()).title(movie.getTitle())
				.releaseYear(movie.getReleaseDate().getYear()).durationMinutes(movie.getDurationMinutes()).build();
	}
}