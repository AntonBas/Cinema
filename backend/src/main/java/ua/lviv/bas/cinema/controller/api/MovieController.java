package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.service.cinema.MovieService;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Public APIs for accessing movie information")
public class MovieController {

	private final MovieService movieService;

	@GetMapping("/{id}")
	@Operation(summary = "Get movie by ID", description = "Retrieves detailed information about a specific movie by its ID")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movie found successfully", content = @Content(schema = @Schema(implementation = MovieDetailResponse.class))),
			@ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(implementation = String.class))) })
	public ResponseEntity<MovieDetailResponse> getMovieById(
			@Parameter(description = "ID of the movie to retrieve", required = true, example = "1") @PathVariable Long id) {

		log.info("GET /api/movies/{} - Getting movie by id", id);
		MovieDetailResponse movie = movieService.getMovieById(id);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/slug/{slug}")
	@Operation(summary = "Get movie by slug", description = "Retrieves detailed information about a specific movie by its URL-friendly slug")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movie found successfully", content = @Content(schema = @Schema(implementation = MovieDetailResponse.class))),
			@ApiResponse(responseCode = "404", description = "Movie not found", content = @Content(schema = @Schema(implementation = String.class))) })
	public ResponseEntity<MovieDetailResponse> getMovieBySlug(
			@Parameter(description = "URL-friendly slug of the movie", required = true, example = "the-matrix-1999") @PathVariable String slug) {

		log.info("GET /api/movies/slug/{} - Getting movie by slug", slug);
		MovieDetailResponse movie = movieService.getMovieBySlug(slug);
		return ResponseEntity.ok(movie);
	}

	@GetMapping
	@Operation(summary = "Get paginated movies list", description = "Retrieves a paginated list of currently showing movies")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movies retrieved successfully", content = @Content(schema = @Schema(implementation = MovieCardResponse.class))) })
	public ResponseEntity<Page<MovieCardResponse>> getMoviesPaginated(
			@Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault(size = 12, sort = "title") Pageable pageable) {

		log.info("GET /api/movies - Getting paginated movies");
		Page<MovieCardResponse> response = movieService.getCurrentlyShowingPage(pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}/poster")
	@Operation(summary = "Get movie poster", description = "Retrieves the poster image for a specific movie")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Poster retrieved successfully", content = @Content(mediaType = "image/jpeg")),
			@ApiResponse(responseCode = "404", description = "Movie or poster not found", content = @Content(schema = @Schema(implementation = String.class))) })
	public ResponseEntity<byte[]> getMoviePoster(
			@Parameter(description = "ID of the movie", required = true, example = "1") @PathVariable Long id) {

		log.info("GET /api/movies/{}/poster - Getting movie poster", id);
		return movieService.getMoviePoster(id);
	}

	@GetMapping("/status/current")
	@Operation(summary = "Get currently showing movies", description = "Retrieves a limited list of movies currently showing in cinemas")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movies retrieved successfully", content = @Content(schema = @Schema(implementation = MovieCardResponse.class))) })
	public ResponseEntity<List<MovieCardResponse>> getCurrentlyShowingMovies() {

		log.info("GET /api/movies/status/current - Getting currently showing movies");
		List<MovieCardResponse> movies = movieService.getCurrentlyShowing(10);
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/status/upcoming")
	@Operation(summary = "Get upcoming movies", description = "Retrieves a limited list of upcoming movies that will be shown soon")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movies retrieved successfully", content = @Content(schema = @Schema(implementation = MovieCardResponse.class))) })
	public ResponseEntity<List<MovieCardResponse>> getUpcomingMovies() {

		log.info("GET /api/movies/status/upcoming - Getting upcoming movies");
		List<MovieCardResponse> movies = movieService.getUpcoming(10);
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/search/active")
	@Operation(summary = "Search active movies", description = "Search through currently showing and upcoming movies for session filtering")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movies retrieved successfully", content = @Content(schema = @Schema(implementation = MovieSessionSearchResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = String.class))) })
	public ResponseEntity<List<MovieSessionSearchResponse>> searchActiveMovies(
			@Parameter(description = "Search term for movie title", example = "Avengers") @RequestParam(required = false) String search) {

		log.info("GET /api/movies/search/active - Searching active movies: '{}'", search);
		List<MovieSessionSearchResponse> movies = movieService.searchActiveMovies(search);
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/status/current/paginated")
	@Operation(summary = "Get paginated currently showing movies", description = "Retrieves a paginated list of currently showing movies")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movies retrieved successfully", content = @Content(schema = @Schema(implementation = MovieCardResponse.class))) })
	public ResponseEntity<Page<MovieCardResponse>> getCurrentlyShowingPage(
			@Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault(size = 12, sort = "title") Pageable pageable) {

		log.info("GET /api/movies/status/current/paginated - Getting currently showing movies with pagination");
		Page<MovieCardResponse> response = movieService.getCurrentlyShowingPage(pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/status/upcoming/paginated")
	@Operation(summary = "Get paginated upcoming movies", description = "Retrieves a paginated list of upcoming movies")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movies retrieved successfully", content = @Content(schema = @Schema(implementation = MovieCardResponse.class))) })
	public ResponseEntity<Page<MovieCardResponse>> getUpcomingPage(
			@Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault(size = 12, sort = "title") Pageable pageable) {

		log.info("GET /api/movies/status/upcoming/paginated - Getting upcoming movies with pagination");
		Page<MovieCardResponse> response = movieService.getUpcomingPage(pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/filtered")
	@Operation(summary = "Search and filter movies", description = "Search movies by title and/or filter by status with pagination")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movies retrieved successfully", content = @Content(schema = @Schema(implementation = MovieCardResponse.class))) })
	public ResponseEntity<Page<MovieCardResponse>> findFilteredMovies(
			@Parameter(description = "Search term for movie title", example = "Avengers") @RequestParam(required = false) String search,

			@Parameter(description = "Filter by movie status", example = "NOW_SHOWING") @RequestParam(required = false) MovieStatus status,

			@Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault(size = 20, sort = "title") Pageable pageable) {

		log.info("GET /api/movies/filtered - Filtering movies: search='{}', status={}", search, status);
		Page<MovieCardResponse> response = movieService.findFilteredMovies(search, status, pageable);
		return ResponseEntity.ok(response);
	}
}