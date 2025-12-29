package ua.lviv.bas.cinema.controller.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.filter.MovieFilter;
import ua.lviv.bas.cinema.dto.filter.MovieFilter.SortDirection;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.common.MovieService;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Movie API", description = "Public endpoints for viewing movies")
public class MovieController {

	private final MovieService movieService;

	@GetMapping("/{id}")
	@Operation(summary = "Get movie by ID", description = "Retrieve detailed movie information by its unique identifier")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie found"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> getMovieById(
			@Parameter(description = "ID of the movie", example = "1") @PathVariable Long id) {
		log.info("GET /api/movies/{} - Getting movie by id", id);
		MovieDetailResponse movie = movieService.getMovieById(id);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/slug/{slug}")
	@Operation(summary = "Get movie by slug", description = "Retrieve detailed movie information by its URL-friendly slug")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie found"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> getMovieBySlug(
			@Parameter(description = "URL-friendly slug of the movie", example = "inception-2020") @PathVariable String slug) {
		log.info("GET /api/movies/slug/{} - Getting movie by slug", slug);
		MovieDetailResponse movie = movieService.getMovieBySlug(slug);
		return ResponseEntity.ok(movie);
	}

	@GetMapping
	@Operation(summary = "Get all movies with pagination", description = "Retrieve paginated list of all movies")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<PageResponse<MovieDetailResponse>> getMoviesPaginated(
			@PageableDefault(size = 12, sort = "title") Pageable pageable) {
		log.info("GET /api/movies - Getting paginated movies");
		PageResponse<MovieDetailResponse> response = movieService.getMoviesPaginated(pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}/poster")
	@Operation(summary = "Get movie poster", description = "Retrieve movie poster image by movie ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Poster retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Poster not found") })
	public ResponseEntity<byte[]> getMoviePoster(
			@Parameter(description = "ID of the movie", example = "1") @PathVariable Long id) {
		log.info("GET /api/movies/{}/poster - Getting movie poster", id);
		return movieService.getMoviePoster(id);
	}

	@GetMapping("/status/current")
	@Operation(summary = "Get currently showing movies", description = "Retrieve movies that are currently showing")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<List<MovieCardResponse>> getCurrentlyShowingMovies() {
		log.info("GET /api/movies/status/current - Getting currently showing movies");
		List<MovieCardResponse> movies = movieService.getCurrentlyShowingMovies();
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/status/upcoming")
	@Operation(summary = "Get upcoming movies", description = "Retrieve movies that are upcoming")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<List<MovieCardResponse>> getUpcomingMovies() {
		log.info("GET /api/movies/status/upcoming - Getting upcoming movies");
		List<MovieCardResponse> movies = movieService.getUpcomingMovies();
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/status/current/paginated")
	@Operation(summary = "Get currently showing movies with pagination", description = "Retrieve paginated list of currently showing movies")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<PageResponse<MovieCardResponse>> findCurrentlyShowingPaginated(
			@PageableDefault(size = 12, sort = "title") Pageable pageable) {
		log.info("GET /api/movies/status/current/paginated - Getting currently showing movies with pagination");
		PageResponse<MovieCardResponse> response = movieService.findCurrentlyShowingPaginated(pageable, false);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/status/upcoming/paginated")
	@Operation(summary = "Get upcoming movies with pagination", description = "Retrieve paginated list of upcoming movies")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<PageResponse<MovieCardResponse>> findUpcomingPaginated(
			@PageableDefault(size = 12, sort = "title") Pageable pageable) {
		log.info("GET /api/movies/status/upcoming/paginated - Getting upcoming movies with pagination");
		PageResponse<MovieCardResponse> response = movieService.findUpcomingPaginated(pageable, false);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/new-releases")
	@Operation(summary = "Get new releases", description = "Retrieve newly released movies")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<List<MovieCardResponse>> getNewReleases(
			@Parameter(description = "Maximum number of movies to return (default: 5)", example = "5") @RequestParam(defaultValue = "5") int limit) {
		log.info("GET /api/movies/new-releases - Getting new releases with limit: {}", limit);
		List<MovieCardResponse> movies = movieService.getNewReleases(limit);
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/ending-soon")
	@Operation(summary = "Get ending soon movies", description = "Retrieve movies that are ending soon")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<List<MovieCardResponse>> getEndingSoon(
			@Parameter(description = "Maximum number of movies to return (default: 5)", example = "5") @RequestParam(defaultValue = "5") int limit) {
		log.info("GET /api/movies/ending-soon - Getting ending soon movies with limit: {}", limit);
		List<MovieCardResponse> movies = movieService.getEndingSoon(limit);
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/filtered")
	@Operation(summary = "Filter movies", description = "Filter movies with pagination support")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<PageResponse<MovieCardResponse>> findFilteredMovies(
			@Parameter(description = "Search term for movie title or description (optional)", example = "inception") @RequestParam(required = false) String searchTerm,
			@Parameter(description = "Filter by movie status (optional)", example = "CURRENT") @RequestParam(required = false) MovieStatus status,
			@Parameter(description = "Filter by age rating (optional)", example = "PG_13") @RequestParam(required = false) AgeRating ageRating,
			@Parameter(description = "Filter by minimum duration in minutes (optional)", example = "60") @RequestParam(required = false) Integer minDuration,
			@Parameter(description = "Filter by maximum duration in minutes (optional)", example = "180") @RequestParam(required = false) Integer maxDuration,
			@Parameter(description = "Filter by release date from (optional, format: yyyy-MM-dd)", example = "2024-01-01") @RequestParam(required = false) LocalDate releaseDateFrom,
			@Parameter(description = "Filter by release date to (optional, format: yyyy-MM-dd)", example = "2024-12-31") @RequestParam(required = false) LocalDate releaseDateTo,
			@Parameter(description = "Field to sort by (default: title)", example = "title") @RequestParam(defaultValue = "title") String sortBy,
			@Parameter(description = "Sort direction (default: ASC)", example = "ASC") @RequestParam(defaultValue = "ASC") SortDirection sortDirection,
			@Parameter(description = "Page number (0-based, default: 0)", example = "0") @RequestParam(defaultValue = "0") int page,
			@Parameter(description = "Number of items per page (default: 20)", example = "20") @RequestParam(defaultValue = "20") int size) {

		MovieFilter filter = MovieFilter.builder().searchTerm(searchTerm).status(status).ageRating(ageRating)
				.minDuration(minDuration).maxDuration(maxDuration).releaseDateFrom(releaseDateFrom)
				.releaseDateTo(releaseDateTo).sortBy(sortBy).sortDirection(sortDirection).page(page).size(size).build();

		log.info("GET /api/movies/filtered - Filtering movies with filter: {}", filter);
		PageResponse<MovieCardResponse> response = movieService.findFilteredMovies(filter);
		return ResponseEntity.ok(response);
	}
}