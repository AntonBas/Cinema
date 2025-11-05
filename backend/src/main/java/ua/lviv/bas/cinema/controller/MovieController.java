package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.movie.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.MovieDto;
import ua.lviv.bas.cinema.dto.movie.MovieResponse;
import ua.lviv.bas.cinema.dto.movie.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.MovieService;

@Slf4j
@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Validated
public class MovieController {

	private final MovieService movieService;
	private static final String DEFAULT_PAGE = "0";
	private static final String DEFAULT_SIZE = "10";
	private static final int MAX_PAGE_SIZE = 50;

	@GetMapping("/{id}")
	public ResponseEntity<MovieDto> getById(@PathVariable Long id) {
		log.info("GET /api/movies/{} - Getting movie by id", id);
		MovieDto movie = movieService.getById(id);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/slug/{slug}")
	public ResponseEntity<MovieDto> getBySlug(@PathVariable String slug) {
		log.info("GET /api/movies/slug/{} - Getting movie by slug", slug);
		MovieDto movie = movieService.getBySlug(slug);
		return ResponseEntity.ok(movie);
	}

	@GetMapping
	public ResponseEntity<List<MovieDto>> getAll() {
		log.info("GET /api/movies - Getting all movies");
		List<MovieDto> movies = movieService.getAll();
		log.debug("Retrieved {} movies", movies.size());
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/paginated")
	public ResponseEntity<PageResponse<MovieDto>> getPaginated(@RequestParam(defaultValue = DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = DEFAULT_SIZE) int size) {

		size = Math.min(size, MAX_PAGE_SIZE);
		log.info("GET /api/movies/paginated - page: {}, size: {}", page, size);

		Page<MovieDto> moviePage = movieService.getPaginated(PageRequest.of(page, size));

		PageResponse<MovieDto> response = new PageResponse<>(moviePage.getContent(), moviePage.getNumber(),
				moviePage.getTotalPages(), moviePage.getTotalElements(), moviePage.getSize());

		return ResponseEntity.ok(response);
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<MovieDto> create(@RequestPart("movieData") @Valid MovieCreateRequest request,
			@RequestPart("posterFile") MultipartFile posterFile) {

		request.setPosterFile(posterFile);
		log.info("POST /api/movies - Creating new movie: {}", request.getTitle());
		MovieDto createdMovie = movieService.create(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<MovieDto> update(@PathVariable Long id,
			@RequestPart("movieData") @Valid MovieUpdateRequest request,
			@RequestPart(value = "posterFile", required = false) MultipartFile posterFile) {

		log.info("PUT /api/movies/{} - Updating movie with file", id);
		request.setPosterFile(posterFile);
		MovieDto updatedMovie = movieService.update(id, request);
		return ResponseEntity.ok(updatedMovie);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		log.info("DELETE /api/movies/{} - Deleting movie", id);
		movieService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/status/current")
	public ResponseEntity<List<MovieResponse>> getCurrentlyShowing() {
		log.info("GET /api/movies/status/current - Getting currently showing movies");
		List<MovieResponse> movies = movieService.getCurrentlyShowing();
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/status/upcoming")
	public ResponseEntity<List<MovieResponse>> getUpcoming() {
		log.info("GET /api/movies/status/upcoming - Getting upcoming movies");
		List<MovieResponse> movies = movieService.getUpcoming();
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/status/archived")
	public ResponseEntity<List<MovieResponse>> getArchived() {
		log.info("GET /api/movies/status/archived - Getting archived movies");
		List<MovieResponse> movies = movieService.getArchived();
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/{id}/poster")
	public ResponseEntity<byte[]> getPoster(@PathVariable Long id) {
		log.info("GET /api/movies/{}/poster - Retrieving movie poster", id);
		return movieService.getPoster(id);
	}

	@GetMapping("/for-sessions")
	public ResponseEntity<List<MovieResponse>> getMoviesForSessions() {
		log.info("GET /api/movies/for-sessions - Getting movies available for sessions");
		List<MovieResponse> movies = movieService.getMoviesForSessions();
		return ResponseEntity.ok(movies);
	}
}