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
	@Operation(summary = "Create new movie")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Movie created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data") })
	public ResponseEntity<MovieDetailResponse> createMovie(@RequestPart("movieData") String movieDataJson,
			@RequestPart(value = "posterFile", required = false) MultipartFile posterFile) {

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
	@Operation(summary = "Update movie")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie updated successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> updateMovie(@PathVariable Long id,
			@RequestPart("movieData") String movieDataJson,
			@RequestPart(value = "posterFile", required = false) MultipartFile posterFile) {

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
	@Operation(summary = "Delete movie")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Movie deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
		log.info("DELETE /api/admin/movies/{} - Deleting movie", id);
		movieService.deleteMovie(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@Operation(summary = "Get filtered movies")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public ResponseEntity<PageResponse<MovieCardResponse>> getMovies(@ModelAttribute @Valid MovieFilterRequest filter,
			@PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/admin/movies - filter: {}", filter);
		var result = movieService.getFilteredMovies(filter, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get admin movie by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie found"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> getAdminMovieById(@PathVariable Long id) {
		log.info("GET /api/admin/movies/{} - Getting admin movie by id", id);
		MovieDetailResponse movie = movieService.getAdminMovieById(id);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/by-slug/{slug}")
	@Operation(summary = "Get admin movie by slug")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie found"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> getAdminMovieBySlug(@PathVariable String slug) {
		log.info("GET /api/admin/movies/by-slug/{} - Getting admin movie by slug", slug);
		MovieDetailResponse movie = movieService.getAdminMovieBySlug(slug);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/search/session")
	@Operation(summary = "Search movies for session")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public ResponseEntity<List<MovieSessionSearchResponse>> searchMoviesForSession(
			@RequestParam(required = false) String search) {

		log.info("GET /api/admin/movies/search/session - search: '{}'", search);
		var movies = movieService.searchMoviesForSession(search);
		return ResponseEntity.ok(movies);
	}
}