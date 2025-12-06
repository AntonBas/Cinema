package ua.lviv.bas.cinema.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.dto.shared.ApiResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.MovieService;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Validated
public class MovieController {

	private final MovieService movieService;

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<MovieDetailResponse>> getMovieById(@PathVariable Long id) {
		log.info("GET /api/movies/{} - Getting movie by id", id);
		MovieDetailResponse movie = movieService.getMovieById(id);
		return ResponseEntity.ok(ApiResponse.success(movie));
	}

	@GetMapping("/slug/{slug}")
	public ResponseEntity<ApiResponse<MovieDetailResponse>> getMovieBySlug(@PathVariable String slug) {
		log.info("GET /api/movies/slug/{} - Getting movie by slug", slug);
		MovieDetailResponse movie = movieService.getMovieBySlug(slug);
		return ResponseEntity.ok(ApiResponse.success(movie));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<MovieDetailResponse>>> getAllMovies() {
		log.info("GET /api/movies - Getting all movies");
		List<MovieDetailResponse> movies = movieService.getAllMovies();
		return ResponseEntity.ok(ApiResponse.success(movies));
	}

	@GetMapping("/paginated")
	public ResponseEntity<ApiResponse<PageResponse<MovieDetailResponse>>> getMoviesPaginated(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "12") int size) {

		log.info("GET /api/movies/paginated - page: {}, size: {}", page, size);
		PageResponse<MovieDetailResponse> response = movieService.getMoviesPaginatedResponse(page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	public ResponseEntity<ApiResponse<MovieDetailResponse>> createMovie(
			@RequestPart("movieData") @Valid MovieCreateRequest request,
			@RequestPart("posterFile") MultipartFile posterFile) {

		request.setPosterFile(posterFile);
		log.info("POST /api/movies - Creating new movie: {}", request.getTitle());
		MovieDetailResponse createdMovie = movieService.createMovie(request);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success(createdMovie, "Movie created successfully"));
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	public ResponseEntity<ApiResponse<MovieDetailResponse>> updateMovie(@PathVariable Long id,
			@RequestPart("movieData") @Valid MovieUpdateRequest request,
			@RequestPart(value = "posterFile", required = false) MultipartFile posterFile) {

		log.info("PUT /api/movies/{} - Updating movie with file", id);
		request.setPosterFile(posterFile);
		MovieDetailResponse updatedMovie = movieService.updateMovie(id, request);
		return ResponseEntity.ok(ApiResponse.success(updatedMovie, "Movie updated successfully"));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable Long id) {
		log.info("DELETE /api/movies/{} - Deleting movie", id);
		movieService.deleteMovie(id);
		return ResponseEntity.ok(ApiResponse.success(null, "Movie deleted successfully"));
	}

	@GetMapping("/search")
	public ResponseEntity<ApiResponse<PageResponse<MovieCardResponse>>> searchMovies(
			@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "12") int size) {

		log.info("GET /api/movies/search - search: {}, page: {}, size: {}", search, page, size);
		PageResponse<MovieCardResponse> response = movieService.searchMoviesResponse(search, page, size);
		return ResponseEntity.ok(ApiResponse.success(response));
	}

	@GetMapping("/search/for-session")
	public ResponseEntity<ApiResponse<List<MovieSessionSearchResponse>>> searchMoviesForSessionCreation(
			@RequestParam LocalDate sessionDate, @RequestParam(required = false) String search) {

		log.info("GET /api/movies/search/for-session - sessionDate: {}, search: {}", sessionDate, search);
		List<MovieSessionSearchResponse> movies = movieService.searchMoviesForSessionCreation(search, sessionDate);
		return ResponseEntity.ok(ApiResponse.success(movies));
	}
}