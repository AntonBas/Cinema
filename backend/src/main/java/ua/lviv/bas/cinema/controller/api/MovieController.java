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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.service.cinema.MovieService;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

	private final MovieService movieService;

	@GetMapping("/{id}")
	public ResponseEntity<MovieDetailResponse> getMovieById(@PathVariable Long id) {
		log.info("GET /api/movies/{} - Getting movie by id", id);
		MovieDetailResponse movie = movieService.getMovieById(id);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/slug/{slug}")
	public ResponseEntity<MovieDetailResponse> getMovieBySlug(@PathVariable String slug) {
		log.info("GET /api/movies/slug/{} - Getting movie by slug", slug);
		MovieDetailResponse movie = movieService.getMovieBySlug(slug);
		return ResponseEntity.ok(movie);
	}

	@GetMapping
	public ResponseEntity<Page<MovieCardResponse>> getMoviesPaginated(
			@PageableDefault(size = 12, sort = "title") Pageable pageable) {
		log.info("GET /api/movies - Getting paginated movies");
		Page<MovieCardResponse> response = movieService.getCurrentlyShowingPage(pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/{id}/poster")
	public ResponseEntity<byte[]> getMoviePoster(@PathVariable Long id) {
		log.info("GET /api/movies/{}/poster - Getting movie poster", id);
		return movieService.getMoviePoster(id);
	}

	@GetMapping("/status/current")
	public ResponseEntity<List<MovieCardResponse>> getCurrentlyShowingMovies() {
		log.info("GET /api/movies/status/current - Getting currently showing movies");
		List<MovieCardResponse> movies = movieService.getCurrentlyShowing(10);
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/status/upcoming")
	public ResponseEntity<List<MovieCardResponse>> getUpcomingMovies() {
		log.info("GET /api/movies/status/upcoming - Getting upcoming movies");
		List<MovieCardResponse> movies = movieService.getUpcoming(10);
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/status/current/paginated")
	public ResponseEntity<Page<MovieCardResponse>> getCurrentlyShowingPage(
			@PageableDefault(size = 12, sort = "title") Pageable pageable) {
		log.info("GET /api/movies/status/current/paginated - Getting currently showing movies with pagination");
		Page<MovieCardResponse> response = movieService.getCurrentlyShowingPage(pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/status/upcoming/paginated")
	public ResponseEntity<Page<MovieCardResponse>> getUpcomingPage(
			@PageableDefault(size = 12, sort = "title") Pageable pageable) {
		log.info("GET /api/movies/status/upcoming/paginated - Getting upcoming movies with pagination");
		Page<MovieCardResponse> response = movieService.getUpcomingPage(pageable);
		return ResponseEntity.ok(response);
	}

	@GetMapping("/filtered")
	public ResponseEntity<Page<MovieCardResponse>> findFilteredMovies(@RequestParam(required = false) String search,
			@RequestParam(required = false) MovieStatus status,
			@PageableDefault(size = 20, sort = "title") Pageable pageable) {

		log.info("GET /api/movies/filtered - Filtering movies: search='{}', status={}", search, status);

		if (search != null && !search.isBlank()) {
			Page<MovieCardResponse> response = movieService.searchMoviesByTitle(search, status, pageable);
			return ResponseEntity.ok(response);
		} else if (status != null) {
			Page<MovieCardResponse> response = movieService.getMoviesByStatus(status, pageable);
			return ResponseEntity.ok(response);
		} else {
			Page<MovieCardResponse> response = movieService.getCurrentlyShowingPage(pageable);
			return ResponseEntity.ok(response);
		}
	}
}