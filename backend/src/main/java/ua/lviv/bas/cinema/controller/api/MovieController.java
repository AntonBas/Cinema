package ua.lviv.bas.cinema.controller.api;

import java.util.concurrent.TimeUnit;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.MovieNotFoundException;
import ua.lviv.bas.cinema.service.cinema.MovieService;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movies", description = "Public APIs for accessing movie information")
public class MovieController {

	private final MovieService movieService;

	@GetMapping("/slug/{slug}")
	@Operation(summary = "Get movie by slug", description = "Retrieves detailed information about a specific movie by its URL-friendly slug")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie found successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public ResponseEntity<MovieDetailResponse> getMovieBySlug(
			@Parameter(description = "URL-friendly slug of the movie", required = true, example = "inception") @PathVariable String slug) {

		log.info("GET /api/movies/slug/{} - Getting movie by slug", slug);
		MovieDetailResponse movie = movieService.getMovieBySlug(slug);

		if (movie.status() == MovieStatus.ARCHIVED) {
			log.warn("Movie with slug {} is archived and not available publicly", slug);
			throw new MovieNotFoundException(slug);
		}

		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS)).body(movie);
	}

	@GetMapping("/currently-showing")
	@Operation(summary = "Get currently showing movies", description = "Retrieves paginated list of currently showing movies")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public ResponseEntity<PageResponse<MovieCardResponse>> getCurrentlyShowingMovies(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 12, sort = "releaseDate", direction = Sort.Direction.DESC) Pageable pageable) {

		log.info("GET /api/movies/currently-showing - Getting currently showing movies");
		var result = movieService.getFilteredMovies(null, MovieStatus.CURRENT, pageable);
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
				.body(PageResponse.from(result));
	}

	@GetMapping("/upcoming")
	@Operation(summary = "Get upcoming movies", description = "Retrieves paginated list of upcoming movies")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public ResponseEntity<PageResponse<MovieCardResponse>> getUpcomingMovies(
			@Parameter(description = "Pagination parameters") @PageableDefault(size = 12, sort = "releaseDate", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/movies/upcoming - Getting upcoming movies");
		var result = movieService.getFilteredMovies(null, MovieStatus.UPCOMING, pageable);
		return ResponseEntity.ok().cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
				.body(PageResponse.from(result));
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
}