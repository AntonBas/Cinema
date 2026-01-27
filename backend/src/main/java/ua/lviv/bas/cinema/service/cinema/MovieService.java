package ua.lviv.bas.cinema.service.cinema;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
import ua.lviv.bas.cinema.exception.domain.cinema.MovieValidationException;
import ua.lviv.bas.cinema.mapper.MovieMapper;
import ua.lviv.bas.cinema.repository.GenreRepository;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;
import ua.lviv.bas.cinema.scheduler.MovieScheduler;
import ua.lviv.bas.cinema.service.integration.file.PosterService;
import ua.lviv.bas.cinema.service.integration.slug.SlugService;

@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

	private final MovieRepository movieRepository;
	private final GenreRepository genreRepository;
	private final PersonRepository personRepository;
	private final MovieMapper movieMapper;
	private final SlugService slugService;
	private final MovieScheduler movieScheduler;
	private final PosterService posterService;

	@Transactional
	public MovieDetailResponse createMovie(MovieCreateRequest request) {
		log.info("Creating movie: {}", request.getTitle());

		validateCreateRequest(request);

		String slug = slugService.generateUniqueSlug(request.getTitle());
		validateSlugUniqueness(slug);

		Movie movie = movieMapper.toMovie(request);
		movie.setSlug(slug);
		movie.setStatus(movieScheduler.calculateMovieStatus(movie, LocalDate.now()));

		handlePosterUpload(request.getPosterFile(), movie);
		setMovieRelations(movie, request);

		Movie saved = movieRepository.save(movie);
		return buildDetailResponse(saved);
	}

	@Transactional
	public MovieDetailResponse updateMovie(Long id, MovieUpdateRequest request) {
		log.info("Updating movie with id: {}", id);

		Movie existing = findMovieById(id);
		validateUpdateRequest(request);

		movieMapper.updateMovieFromRequest(request, existing);

		if (!existing.getTitle().equals(request.getTitle())) {
			String newSlug = slugService.generateUniqueSlug(request.getTitle());
			if (!slugService.isSlugAvailableForMovie(newSlug, id)) {
				throw new DuplicateEntityException("Movie", "slug " + newSlug);
			}
			existing.setSlug(newSlug);
		}

		handlePosterUpdate(existing, request.getPosterFile(), request.getRemovePoster());
		existing.setStatus(movieScheduler.calculateMovieStatus(existing, LocalDate.now()));
		updateMovieRelations(existing, request);

		Movie updated = movieRepository.save(existing);
		return buildDetailResponse(updated);
	}

	@Transactional
	public void deleteMovie(Long id) {
		log.info("Deleting movie with id: {}", id);

		Movie movie = findMovieById(id);
		posterService.deletePoster(movie.getPosterFileName());
		movieRepository.delete(movie);
	}

	@Transactional(readOnly = true)
	public MovieDetailResponse getMovieById(Long id) {
		Movie movie = findMovieById(id);
		return buildDetailResponse(movie);
	}

	@Transactional(readOnly = true)
	public MovieDetailResponse getMovieBySlug(String slug) {
		Movie movie = movieRepository.findBySlug(slug).orElseThrow(() -> new MovieNotFoundException(slug));
		return buildDetailResponse(movie);
	}

	@Transactional(readOnly = true)
	public Page<MovieCardResponse> findFilteredMovies(String search, MovieStatus status, Pageable pageable) {
		log.debug("Finding filtered movies: search='{}', status={}", search, status);

		if (search != null && !search.isBlank()) {
			return searchMoviesByTitle(search, status, pageable);
		} else if (status != null) {
			return getMoviesByStatus(status, pageable);
		} else {
			return getCurrentlyShowingPage(pageable);
		}
	}

	@Transactional(readOnly = true)
	public Page<MovieCardResponse> getMoviesByStatus(MovieStatus status, Pageable pageable) {
		Page<Movie> movies = movieRepository.findByStatusWithSearch(status, null, pageable);
		return movies.map(this::buildCardResponse);
	}

	@Transactional(readOnly = true)
	public List<MovieCardResponse> getCurrentlyShowing(int limit) {
		Pageable pageable = PageRequest.of(0, limit, Sort.by("releaseDate").descending());
		List<Movie> movies = movieRepository.findCurrentlyShowing(pageable);
		return movies.stream().map(this::buildCardResponse).toList();
	}

	@Transactional(readOnly = true)
	public Page<MovieCardResponse> getCurrentlyShowingPage(Pageable pageable) {
		Page<Movie> movies = movieRepository.findCurrentlyShowingPage(pageable);
		return movies.map(this::buildCardResponse);
	}

	@Transactional(readOnly = true)
	public List<MovieCardResponse> getUpcoming(int limit) {
		Pageable pageable = PageRequest.of(0, limit, Sort.by("releaseDate"));
		List<Movie> movies = movieRepository.findUpcoming(pageable);
		return movies.stream().map(this::buildCardResponse).toList();
	}

	@Transactional(readOnly = true)
	public Page<MovieCardResponse> getUpcomingPage(Pageable pageable) {
		Page<Movie> movies = movieRepository.findUpcomingPage(pageable);
		return movies.map(this::buildCardResponse);
	}

	@Transactional(readOnly = true)
	public Page<MovieCardResponse> getArchivedMovies(Pageable pageable) {
		Page<Movie> movies = movieRepository.findArchived(pageable);
		return movies.map(this::buildCardResponse);
	}

	@Transactional(readOnly = true)
	public List<MovieSessionSearchResponse> searchMoviesForSessionCreation(String searchTerm, LocalDate sessionDate) {
		List<Movie> movies = movieRepository.findMoviesForSessionCreation(searchTerm, sessionDate);
		return movies.stream().map(this::toSessionSearchResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<MovieSessionSearchResponse> searchActiveMovies(String searchTerm) {
		List<Movie> movies = movieRepository.findActiveMoviesForSearch(searchTerm);
		return movies.stream().map(this::toSessionSearchResponse).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Page<MovieCardResponse> searchMoviesByTitle(String search, MovieStatus status, Pageable pageable) {
		Page<Movie> movies = movieRepository.findByStatusWithSearch(status, search, pageable);
		return movies.map(this::buildCardResponse);
	}

	@Transactional(readOnly = true)
	public ResponseEntity<byte[]> getMoviePoster(Long id) {
		Movie movie = findMovieById(id);
		return posterService.getPosterResponse(movie.getPosterFileName());
	}

	private MovieCardResponse buildCardResponse(Movie movie) {
		MovieCardResponse response = movieMapper.toMovieCardResponse(movie);
		enrichCardResponse(response, movie);
		return response;
	}

	private MovieDetailResponse buildDetailResponse(Movie movie) {
		MovieDetailResponse response = movieMapper.toMovieDetailResponse(movie);
		enrichDetailResponse(response, movie);
		return response;
	}

	private Movie findMovieById(Long id) {
		return movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
	}

	private void validateCreateRequest(MovieCreateRequest request) {
		if (request.getEndShowingDate().isBefore(request.getReleaseDate())) {
			throw MovieValidationException.invalidDates(request.getReleaseDate(), request.getEndShowingDate());
		}
	}

	private void validateUpdateRequest(MovieUpdateRequest request) {
		if (request.getEndShowingDate().isBefore(request.getReleaseDate())) {
			throw MovieValidationException.invalidDates(request.getReleaseDate(), request.getEndShowingDate());
		}
	}

	private void validateSlugUniqueness(String slug) {
		if (movieRepository.findBySlug(slug).isPresent()) {
			throw new DuplicateEntityException("Movie", "slug " + slug);
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
			String posterFileName = posterService.uploadPoster(posterFile);
			movie.setPosterFileName(posterFileName);
		}
	}

	private void handlePosterUpdate(Movie movie, MultipartFile posterFile, Boolean removePoster) {
		if (posterFile != null && !posterFile.isEmpty()) {
			posterService.deletePoster(movie.getPosterFileName());
			String newPosterFileName = posterService.uploadPoster(posterFile);
			movie.setPosterFileName(newPosterFileName);
		} else if (Boolean.TRUE.equals(removePoster)) {
			posterService.deletePoster(movie.getPosterFileName());
			movie.setPosterFileName(null);
		}
	}

	private void enrichCardResponse(MovieCardResponse response, Movie movie) {
		String posterUrl = posterService.getPosterUrl(movie.getId(), movie.getPosterFileName());
		response.setPosterUrl(posterUrl);
		response.setCurrentlyShowing(movie.getStatus() == MovieStatus.CURRENT);
	}

	private void enrichDetailResponse(MovieDetailResponse response, Movie movie) {
		String posterUrl = posterService.getPosterUrl(movie.getId(), movie.getPosterFileName());
		response.setPosterUrl(posterUrl);
		response.setCurrentlyShowing(movie.getStatus() == MovieStatus.CURRENT);
		response.setUpcoming(movie.getStatus() == MovieStatus.UPCOMING);
		response.setArchived(movie.getStatus() == MovieStatus.ARCHIVED);
	}

	private MovieSessionSearchResponse toSessionSearchResponse(Movie movie) {
		return MovieSessionSearchResponse.builder().id(movie.getId()).title(movie.getTitle())
				.releaseYear(movie.getReleaseDate().getYear()).durationMinutes(movie.getDurationMinutes()).build();
	}
}