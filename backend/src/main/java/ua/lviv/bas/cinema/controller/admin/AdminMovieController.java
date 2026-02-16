package ua.lviv.bas.cinema.controller.admin;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieFilterRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.service.cinema.MovieService;

@Slf4j
@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
@Tag(name = "Admin Movies", description = "Admin APIs for managing movies")
public class AdminMovieController {

	private final MovieService movieService;
	private final ObjectMapper objectMapper;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Create a new movie", description = "Creates a new movie with poster")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Movie created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "409", description = "Movie with same slug already exists") })
	public ResponseEntity<MovieDetailResponse> createMovie(
			@Parameter(description = "Movie data in JSON format", required = true) @RequestPart("movieData") String movieDataJson,
			@Parameter(description = "Movie poster image file (JPG, PNG)") @RequestPart(value = "posterFile", required = false) MultipartFile posterFile) {

		log.info("POST /api/admin/movies - Creating new movie");

		try {
			MovieCreateRequest request = objectMapper.readValue(movieDataJson, MovieCreateRequest.class);
			request.setPosterFile(posterFile);
			MovieDetailResponse createdMovie = movieService.createMovie(request);
			return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
		} catch (Exception e) {
			log.error("Error parsing movie data", e);
			throw new IllegalArgumentException("Invalid movie data format", e);
		}
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Update an existing movie", description = "Updates movie with new data")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "404", description = "Movie not found"),
			@ApiResponse(responseCode = "409", description = "Movie with same slug already exists") })
	public ResponseEntity<MovieDetailResponse> updateMovie(
			@Parameter(description = "ID of the movie to update", required = true, example = "1") @PathVariable Long id,
			@Parameter(description = "Updated movie data in JSON format", required = true) @RequestPart("movieData") String movieDataJson,
			@Parameter(description = "New movie poster image file (JPG, PNG)") @RequestPart(value = "posterFile", required = false) MultipartFile posterFile) {

		log.info("PUT /api/admin/movies/{} - Updating movie", id);

		try {
			MovieUpdateRequest request = objectMapper.readValue(movieDataJson, MovieUpdateRequest.class);
			request.setPosterFile(posterFile);
			MovieDetailResponse updatedMovie = movieService.updateMovie(id, request);
			return ResponseEntity.ok(updatedMovie);
		} catch (Exception e) {
			log.error("Error parsing movie data", e);
			throw new IllegalArgumentException("Invalid movie data format", e);
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete a movie", description = "Deletes movie by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Movie deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<Void> deleteMovie(
			@Parameter(description = "ID of the movie to delete", required = true, example = "1") @PathVariable Long id) {

		log.info("DELETE /api/admin/movies/{} - Deleting movie", id);
		movieService.deleteMovie(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@Operation(summary = "Get filtered movies", description = "Retrieves paginated list of movies with filters")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public ResponseEntity<PageResponse<MovieCardResponse>> getMovies(
			@Parameter(description = "Filter criteria") @ModelAttribute @Valid MovieFilterRequest filter,
			@Parameter(description = "Pagination parameters (page, size, sort)") @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/admin/movies - filter: {}", filter);
		var result = movieService.getFilteredMovies(filter, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get movie by ID", description = "Retrieves detailed movie information by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie found successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> getMovieById(
			@Parameter(description = "ID of the movie", required = true, example = "1") @PathVariable Long id) {

		log.info("GET /api/admin/movies/{} - Getting movie by id", id);
		MovieDetailResponse movie = movieService.getMovieById(id);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/by-slug/{slug}")
	@Operation(summary = "Get movie by slug", description = "Retrieves detailed movie information by slug")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie found successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> getMovieBySlug(
			@Parameter(description = "URL-friendly slug of the movie", required = true, example = "inception-2010") @PathVariable String slug) {

		log.info("GET /api/admin/movies/by-slug/{} - Getting movie by slug", slug);
		MovieDetailResponse movie = movieService.getMovieBySlug(slug);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/search/session")
	@Operation(summary = "Search movies for session", description = "Search active movies for session creation")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public ResponseEntity<List<MovieSessionSearchResponse>> searchMoviesForSession(
			@Parameter(description = "Search term for movie title", example = "Avengers") @RequestParam(required = false) String search) {

		log.info("GET /api/admin/movies/search/session - search: '{}'", search);
		var movies = movieService.searchMoviesForSession(search);
		return ResponseEntity.ok(movies);
	}
}