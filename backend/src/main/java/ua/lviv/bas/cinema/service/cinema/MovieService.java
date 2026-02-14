package ua.lviv.bas.cinema.service.cinema;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.projection.MovieCardProjection;
import ua.lviv.bas.cinema.domain.projection.MovieSessionSearchProjection;
import ua.lviv.bas.cinema.domain.specification.MovieSpecification;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieFilterRequest;
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
@Transactional(readOnly = true)
public class MovieService {

	private final MovieRepository movieRepository;
	private final GenreRepository genreRepository;
	private final PersonRepository personRepository;
	private final MovieMapper movieMapper;
	private final SlugService slugService;
	private final MovieScheduler movieScheduler;
	private final PosterService posterService;
	private final MovieSpecification movieSpecification;

	@Transactional
	public MovieDetailResponse createMovie(MovieCreateRequest request) {
		log.info("Creating movie: {}", request.getTitle());

		validateDates(request.getReleaseDate(), request.getEndShowingDate());

		String slug = slugService.generateUniqueSlug(request.getTitle());
		validateSlugUniqueness(slug);

		Movie movie = movieMapper.toMovie(request);
		movie.setSlug(slug);
		movie.setStatus(movieScheduler.calculateMovieStatus(movie, LocalDate.now()));

		handlePosterUpload(request.getPosterFile(), movie);
		setMovieRelations(movie, request);

		Movie saved = movieRepository.save(movie);
		log.info("Movie created successfully with id: {}", saved.getId());
		return buildDetailResponse(saved);
	}

	@Transactional
	public MovieDetailResponse updateMovie(Long id, MovieUpdateRequest request) {
		log.info("Updating movie with id: {}", id);

		Movie existing = findMovieById(id);
		validateDates(request.getReleaseDate(), request.getEndShowingDate());

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
		log.info("Movie updated successfully with id: {}", updated.getId());
		return buildDetailResponse(updated);
	}

	@Transactional
	public void deleteMovie(Long id) {
		log.info("Deleting movie with id: {}", id);

		Movie movie = findMovieById(id);
		if (movie.getPosterFileName() != null) {
			posterService.deletePoster(movie.getPosterFileName());
		}
		movieRepository.delete(movie);
		log.info("Movie deleted successfully with id: {}", id);
	}

	public MovieDetailResponse getMovieById(Long id) {
		return movieRepository.findDetailProjectionById(id).map(movieMapper::toMovieDetailResponse)
				.orElseThrow(() -> new MovieNotFoundException(id));
	}

	public MovieDetailResponse getMovieBySlug(String slug) {
		Movie movie = movieRepository.findBySlug(slug).orElseThrow(() -> new MovieNotFoundException(slug));
		return buildDetailResponse(movie);
	}

	public Page<MovieCardResponse> getFilteredMovies(MovieFilterRequest filter, Pageable pageable) {
		Specification<Movie> spec = movieSpecification.build(filter);

		if (!pageable.getSort().isSorted()) {
			pageable = Pageable.ofSize(pageable.getPageSize()).withPage(pageable.getPageNumber());
		}

		Page<MovieCardProjection> projections = movieRepository.findMovieCards(spec, pageable);

		log.debug("Found {} movies for filter: {}", projections.getTotalElements(), filter);
		return projections.map(movieMapper::toMovieCardResponse);
	}

	public List<MovieSessionSearchResponse> searchMoviesForSession(String searchTerm) {
		List<MovieSessionSearchProjection> projections = movieRepository.findMoviesForSession(searchTerm);
		return projections.stream().map(movieMapper::toMovieSessionSearchResponse).toList();
	}

	public ResponseEntity<byte[]> getMoviePoster(Long id) {
		Movie movie = findMovieById(id);
		if (movie.getPosterFileName() == null) {
			return ResponseEntity.notFound().build();
		}
		return posterService.getPosterResponse(movie.getPosterFileName());
	}

	public boolean existsBySlug(String slug) {
		return movieRepository.findBySlug(slug).isPresent();
	}

	private MovieDetailResponse buildDetailResponse(Movie movie) {
		return movieMapper.toMovieDetailResponse(movie);
	}

	private Movie findMovieById(Long id) {
		return movieRepository.findById(id).orElseThrow(() -> new MovieNotFoundException(id));
	}

	private void validateDates(LocalDate releaseDate, LocalDate endShowingDate) {
		if (endShowingDate.isBefore(releaseDate)) {
			throw MovieValidationException.invalidDates(releaseDate, endShowingDate);
		}
	}

	private void validateSlugUniqueness(String slug) {
		if (movieRepository.findBySlug(slug).isPresent()) {
			throw new DuplicateEntityException("Movie", "slug " + slug);
		}
	}

	private void setMovieRelations(Movie movie, MovieCreateRequest request) {
		movie.setGenres(new HashSet<>(genreRepository.findAllById(request.getGenreIds())));
		movie.setActors(new HashSet<>(personRepository.findAllById(request.getActorIds())));
		movie.setDirectors(new HashSet<>(personRepository.findAllById(request.getDirectorIds())));
		movie.setScreenwriters(new HashSet<>(personRepository.findAllById(request.getScreenwriterIds())));
	}

	private void updateMovieRelations(Movie movie, MovieUpdateRequest request) {
		movie.getGenres().clear();
		movie.getActors().clear();
		movie.getDirectors().clear();
		movie.getScreenwriters().clear();

		movie.getGenres().addAll(new HashSet<>(genreRepository.findAllById(request.getGenreIds())));
		movie.getActors().addAll(new HashSet<>(personRepository.findAllById(request.getActorIds())));
		movie.getDirectors().addAll(new HashSet<>(personRepository.findAllById(request.getDirectorIds())));
		movie.getScreenwriters().addAll(new HashSet<>(personRepository.findAllById(request.getScreenwriterIds())));
	}

	private void handlePosterUpload(MultipartFile posterFile, Movie movie) {
		if (posterFile != null && !posterFile.isEmpty()) {
			String posterFileName = posterService.uploadPoster(posterFile);
			movie.setPosterFileName(posterFileName);
			log.debug("Poster uploaded: {}", posterFileName);
		}
	}

	private void handlePosterUpdate(Movie movie, MultipartFile posterFile, Boolean removePoster) {
		if (posterFile != null && !posterFile.isEmpty()) {
			if (movie.getPosterFileName() != null) {
				posterService.deletePoster(movie.getPosterFileName());
			}
			String newPosterFileName = posterService.uploadPoster(posterFile);
			movie.setPosterFileName(newPosterFileName);
			log.debug("Poster updated: {}", newPosterFileName);
		} else if (Boolean.TRUE.equals(removePoster) && movie.getPosterFileName() != null) {
			posterService.deletePoster(movie.getPosterFileName());
			movie.setPosterFileName(null);
			log.debug("Poster removed");
		}
	}
}