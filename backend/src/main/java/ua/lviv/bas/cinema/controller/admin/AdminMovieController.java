package ua.lviv.bas.cinema.controller.admin;

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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieFilterRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.service.cinema.MovieService;

@Slf4j
@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminMovieController {

	private final MovieService movieService;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<MovieDetailResponse> createMovie(@RequestPart("movieData") MovieCreateRequest request,
			@RequestPart(value = "posterFile", required = false) MultipartFile posterFile) {

		request.setPosterFile(posterFile);
		log.info("POST /api/admin/movies - Creating new movie: {}", request.getTitle());
		MovieDetailResponse createdMovie = movieService.createMovie(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdMovie);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<MovieDetailResponse> updateMovie(@PathVariable Long id,
			@RequestPart("movieData") MovieUpdateRequest request,
			@RequestPart(value = "posterFile", required = false) MultipartFile posterFile) {

		log.info("PUT /api/admin/movies/{} - Updating movie", id);
		request.setPosterFile(posterFile);
		MovieDetailResponse updatedMovie = movieService.updateMovie(id, request);
		return ResponseEntity.ok(updatedMovie);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
		log.info("DELETE /api/admin/movies/{} - Deleting movie", id);
		movieService.deleteMovie(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<PageResponse<MovieCardResponse>> searchMovies(@ModelAttribute MovieFilterRequest filter,
			@PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/admin/movies - filter: {}", filter);
		var result = movieService.searchMovies(filter, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/{id}")
	public ResponseEntity<MovieDetailResponse> getMovieById(@PathVariable Long id) {
		log.info("GET /api/admin/movies/{} - Getting movie by id", id);
		MovieDetailResponse movie = movieService.getMovieById(id);
		return ResponseEntity.ok(movie);
	}

	@GetMapping("/search/session")
	public ResponseEntity<?> searchMoviesForSessionCreation(@RequestParam(required = false) String search,
			@RequestParam(required = false) java.time.LocalDate sessionDate) {

		log.info("GET /api/admin/movies/search/session - search: '{}', date: {}", search, sessionDate);
		var movies = movieService.searchMoviesForSessionCreation(search, sessionDate);
		return ResponseEntity.ok(movies);
	}
}