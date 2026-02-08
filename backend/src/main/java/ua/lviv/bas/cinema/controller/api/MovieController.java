package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.MovieFilterRequest;
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
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie found successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> getMovieById(
			@Parameter(description = "ID of the movie to retrieve", required = true, example = "1") @PathVariable Long id) {

		log.info("GET /api/movies/{} - Getting movie by id", id);
		MovieDetailResponse movie = movieService.getMovieById(id);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/slug/{slug}")
	@Operation(summary = "Get movie by slug", description = "Retrieves detailed information about a specific movie by its URL-friendly slug")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie found successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> getMovieBySlug(
			@Parameter(description = "URL-friendly slug of the movie", required = true, example = "the-matrix-1999") @PathVariable String slug) {

		log.info("GET /api/movies/slug/{} - Getting movie by slug", slug);
		MovieDetailResponse movie = movieService.getMovieBySlug(slug);
		return ResponseEntity.ok(movie);
	}

	@GetMapping
	@Operation(summary = "Search movies", description = "Search and filter movies with pagination")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public ResponseEntity<PageResponse<MovieCardResponse>> getMovies(
			@Parameter(description = "Filter criteria") @ModelAttribute MovieFilterRequest filter,
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 12, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/movies - filter: {}", filter);
		var result = movieService.getMovieCards(filter, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/{id}/poster")
	@Operation(summary = "Get movie poster", description = "Retrieves the poster image for a specific movie")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Poster retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Movie or poster not found") })
	public ResponseEntity<byte[]> getMoviePoster(
			@Parameter(description = "ID of the movie", required = true, example = "1") @PathVariable Long id) {

		log.info("GET /api/movies/{}/poster - Getting movie poster", id);
		return movieService.getMoviePoster(id);
	}

	@GetMapping("/currently-showing")
	@Operation(summary = "Get currently showing movies", description = "Retrieves paginated list of currently showing movies")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public ResponseEntity<PageResponse<MovieCardResponse>> getCurrentlyShowingMovies(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 12, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable) {

		log.info("GET /api/movies/currently-showing - Getting currently showing movies");
		var filter = MovieFilterRequest.builder().currentlyShowing(true).build();
		var result = movieService.getMovieCards(filter, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/upcoming")
	@Operation(summary = "Get upcoming movies", description = "Retrieves paginated list of upcoming movies")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public ResponseEntity<PageResponse<MovieCardResponse>> getUpcomingMovies(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 12, sort = "releaseDate", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/movies/upcoming - Getting upcoming movies");
		var filter = MovieFilterRequest.builder().upcoming(true).build();
		var result = movieService.getMovieCards(filter, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/search/session")
	@Operation(summary = "Search movies for sessions", description = "Search active movies for session creation")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public ResponseEntity<List<MovieSessionSearchResponse>> searchMoviesForSession(
			@Parameter(description = "Search term for movie title", example = "Avengers") @RequestParam(required = false) String search) {

		log.info("GET /api/movies/search/session - Searching movies for session: '{}'", search);
		List<MovieSessionSearchResponse> movies = movieService.searchMoviesForSession(search);
		return ResponseEntity.ok(movies);
	}
}