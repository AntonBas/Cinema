package ua.lviv.bas.cinema.controller.api;

import java.util.List;

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
	@Operation(summary = "Get all movies", description = "Retrieve complete list of all movies")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<List<MovieDetailResponse>> getAllMovies() {
		log.info("GET /api/movies - Getting all movies");
		List<MovieDetailResponse> movies = movieService.getAllMovies();
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/paginated")
	@Operation(summary = "Get movies with pagination", description = "Retrieve paginated list of movies")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<PageResponse<MovieDetailResponse>> getMoviesPaginated(
			@Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

			@Parameter(description = "Number of items per page (max 50)", example = "12") @RequestParam(defaultValue = "12") int size) {

		size = Math.min(size, 50);
		log.info("GET /api/movies/paginated - page: {}, size: {}", page, size);
		PageResponse<MovieDetailResponse> response = movieService.getMoviesPaginatedResponse(page, size);
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

	@GetMapping("/status/archived")
	@Operation(summary = "Get archived movies", description = "Retrieve movies that are archived")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<List<MovieCardResponse>> getArchivedMovies() {
		log.info("GET /api/movies/status/archived - Getting archived movies");
		List<MovieCardResponse> movies = movieService.getArchivedMovies();
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/search")
	@Operation(summary = "Search movies", description = "Search movies by title with pagination support")
	@ApiResponse(responseCode = "200", description = "Movies retrieved successfully")
	public ResponseEntity<PageResponse<MovieCardResponse>> searchMovies(
			@Parameter(description = "Search query for movie title (case-insensitive)", example = "inception") @RequestParam(required = false) String search,

			@Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

			@Parameter(description = "Number of items per page (max 50)", example = "12") @RequestParam(defaultValue = "12") int size) {

		size = Math.min(size, 50);
		log.info("GET /api/movies/search - search: {}, page: {}, size: {}", search, page, size);
		PageResponse<MovieCardResponse> response = movieService.searchMoviesResponse(search, page, size);
		return ResponseEntity.ok(response);
	}
}