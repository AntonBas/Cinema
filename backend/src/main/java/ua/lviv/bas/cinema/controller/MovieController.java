package ua.lviv.bas.cinema.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.MovieService;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Movie Management", description = "Endpoints for managing movies")
@SecurityRequirement(name = "bearerAuth")
public class MovieController {

	private final MovieService movieService;

	@GetMapping("/{id}")
	@Operation(summary = "Get movie by ID", description = "Retrieve detailed movie information by its unique identifier")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movie found", content = @Content(schema = @Schema(implementation = MovieDetailResponse.class))),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> getMovieById(
			@Parameter(description = "ID of the movie", example = "1") @PathVariable Long id) {
		log.info("GET /api/movies/{} - Getting movie by id", id);
		MovieDetailResponse movie = movieService.getMovieById(id);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/slug/{slug}")
	@Operation(summary = "Get movie by slug", description = "Retrieve detailed movie information by its URL-friendly slug")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movie found", content = @Content(schema = @Schema(implementation = MovieDetailResponse.class))),
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

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Create new movie", description = "Create a new movie with poster image. Requires ADMIN or CONTENT_MANAGER role")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Movie created successfully", content = @Content(schema = @Schema(implementation = MovieDetailResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or file"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<MovieDetailResponse> createMovie(
			@Parameter(description = "Movie creation data as JSON", content = @Content(schema = @Schema(implementation = MovieCreateRequest.class))) @RequestPart("movieData") @Valid MovieCreateRequest request,

			@Parameter(description = "Movie poster image file (JPG, PNG, max 5MB)") @RequestPart("posterFile") MultipartFile posterFile) {

		request.setPosterFile(posterFile);
		log.info("POST /api/movies - Creating new movie: {}", request.getTitle());
		MovieDetailResponse createdMovie = movieService.createMovie(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Update movie", description = "Update existing movie information with optional poster update. Requires ADMIN or CONTENT_MANAGER role")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Movie updated successfully", content = @Content(schema = @Schema(implementation = MovieDetailResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or file"),
			@ApiResponse(responseCode = "404", description = "Movie not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<MovieDetailResponse> updateMovie(
			@Parameter(description = "ID of the movie to update", example = "1") @PathVariable Long id,

			@Parameter(description = "Updated movie data as JSON", content = @Content(schema = @Schema(implementation = MovieUpdateRequest.class))) @RequestPart("movieData") @Valid MovieUpdateRequest request,

			@Parameter(description = "New poster image file (optional, JPG, PNG, max 5MB)") @RequestPart(value = "posterFile", required = false) MultipartFile posterFile) {

		log.info("PUT /api/movies/{} - Updating movie with file", id);
		request.setPosterFile(posterFile);
		MovieDetailResponse updatedMovie = movieService.updateMovie(id, request);
		return ResponseEntity.ok(updatedMovie);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Delete movie", description = "Delete a movie by its ID. Requires ADMIN or CONTENT_MANAGER role")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Movie deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found"),
			@ApiResponse(responseCode = "409", description = "Cannot delete movie with active sessions"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<Void> deleteMovie(
			@Parameter(description = "ID of the movie to delete", example = "1") @PathVariable Long id) {
		log.info("DELETE /api/movies/{} - Deleting movie", id);
		movieService.deleteMovie(id);
		return ResponseEntity.noContent().build();
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

	@GetMapping("/search/for-session")
	@Operation(summary = "Search movies for session creation", description = "Search movies available for session creation on a specific date")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid date format") })
	public ResponseEntity<List<MovieSessionSearchResponse>> searchMoviesForSessionCreation(
			@Parameter(description = "Date for which to check movie availability", example = "2024-01-15") @RequestParam LocalDate sessionDate,

			@Parameter(description = "Search query for movie title (optional)", example = "action") @RequestParam(required = false) String search) {

		log.info("GET /api/movies/search/for-session - sessionDate: {}, search: {}", sessionDate, search);
		List<MovieSessionSearchResponse> movies = movieService.searchMoviesForSessionCreation(search, sessionDate);
		return ResponseEntity.ok(movies);
	}
}