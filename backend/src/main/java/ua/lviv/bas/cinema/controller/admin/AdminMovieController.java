package ua.lviv.bas.cinema.controller.admin;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.service.common.MovieService;
import ua.lviv.bas.cinema.service.common.PersonService;

@Slf4j
@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminMovieController {

	private final MovieService movieService;
	private final PersonService personService;

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

		log.info("PUT /api/admin/movies/{} - Updating movie with file", id);
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

	@GetMapping("/status/archived")
	public ResponseEntity<Page<MovieCardResponse>> getArchivedMovies(
			@PageableDefault(size = 12, sort = "title") Pageable pageable) {
		log.info("GET /api/admin/movies/status/archived - Getting archived movies");
		Page<MovieCardResponse> movies = movieService.getArchivedMovies(pageable);
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/status/{status}")
	public ResponseEntity<Page<MovieCardResponse>> getMoviesByStatus(@PathVariable MovieStatus status,
			@PageableDefault(size = 12, sort = "title") Pageable pageable) {
		log.info("GET /api/admin/movies/status/{} - Getting movies by status", status);
		Page<MovieCardResponse> movies = movieService.getMoviesByStatus(status, pageable);
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/search")
	public ResponseEntity<Page<MovieCardResponse>> searchMovies(@RequestParam(required = false) String search,
			@RequestParam(required = false) MovieStatus status,
			@PageableDefault(size = 12, sort = "title") Pageable pageable) {
		log.info("GET /api/admin/movies/search - search: '{}', status: {}", search, status);
		Page<MovieCardResponse> movies = movieService.searchMoviesByTitle(search, status, pageable);
		return ResponseEntity.ok(movies);
	}

	@GetMapping("/search/for-session")
	public ResponseEntity<List<MovieSessionSearchResponse>> searchMoviesForSessionCreation(
			@RequestParam LocalDate sessionDate, @RequestParam(required = false) String search) {

		log.info("GET /api/admin/movies/search/for-session - sessionDate: {}, search: {}", sessionDate, search);
		List<MovieSessionSearchResponse> movies = movieService.searchMoviesForSessionCreation(search, sessionDate);
		return ResponseEntity.ok(movies);
	}

	@PostMapping("/quick-add-person")
	public ResponseEntity<PersonResponse> quickAddPerson(@RequestBody QuickCreatePersonRequest request) {
		log.info("Quick adding person: {} as {}", request.getName(), request.getRole());
		PersonResponse createdPerson = personService.quickCreatePerson(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
	}

	@GetMapping("/persons/search")
	public ResponseEntity<Page<PersonResponse>> searchPersonsForMovie(@RequestParam(required = false) String query,
			@RequestParam(required = false) PersonRole role,
			@PageableDefault(size = 10, sort = "name") Pageable pageable) {

		log.info("Searching persons for movie: query='{}', role={}", query, role);
		Page<PersonResponse> result = personService.searchPersons(query, role, pageable);
		return ResponseEntity.ok(result);
	}
}