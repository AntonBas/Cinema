package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dto.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.MovieDto;
import ua.lviv.bas.cinema.service.MovieService;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

	private final MovieService movieService;

	@GetMapping
	public ResponseEntity<List<MovieDto>> getAllMovies() {
		return ResponseEntity.ok(movieService.getAllMovies());
	}

	@GetMapping("/{id}")
	public ResponseEntity<MovieDto> getMovieById(@PathVariable Long id) {
		return ResponseEntity.ok(movieService.getMovieById(id));
	}

	@GetMapping("/slug/{slug}")
	public ResponseEntity<MovieDto> getMovieBySlug(@PathVariable String slug) {
		return ResponseEntity.ok(movieService.getMovieBySlug(slug));
	}

	@GetMapping("/page")
	public ResponseEntity<Page<MovieDto>> getPaginatedMovies(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(movieService.getPaginatedMovies(pageable));
	}

	@GetMapping("/status/{status}")
	public ResponseEntity<List<MovieDto>> getMoviesByStatus(@PathVariable String status) {
		return ResponseEntity.ok(movieService.getMoviesByStatus(status));
	}

	@GetMapping("/current")
	public ResponseEntity<List<MovieDto>> getCurrentlyShowingMovies() {
		return ResponseEntity.ok(movieService.getCurrentlyShowingMovies());
	}

	@GetMapping("/upcoming")
	public ResponseEntity<List<MovieDto>> getUpcomingMovies() {
		return ResponseEntity.ok(movieService.getUpcomingMovies());
	}

	@GetMapping("/genre/{genreId}")
	public ResponseEntity<List<MovieDto>> getMoviesByGenre(@PathVariable Long genreId) {
		return ResponseEntity.ok(movieService.getMoviesByGenre(genreId));
	}

	@PostMapping(consumes = "multipart/form-data")
	public ResponseEntity<MovieDto> createMovie(@ModelAttribute @Valid MovieCreateRequest request) {
		return ResponseEntity.ok(movieService.createMovie(request));
	}

	@PutMapping("/{id}")
	public ResponseEntity<MovieDto> updateMovie(@PathVariable Long id, @RequestBody @Valid MovieDto movieDto) {
		return ResponseEntity.ok(movieService.updateMovie(id, movieDto));
	}

	@PutMapping(value = "/{id}/poster", consumes = "multipart/form-data")
	public ResponseEntity<MovieDto> updateMovieWithPoster(@PathVariable Long id,
			@ModelAttribute @Valid MovieDto movieDto) {
		return ResponseEntity.ok(movieService.updateMovieWithPoster(id, movieDto, movieDto.getPosterFile()));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
		movieService.deleteMovie(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}/poster")
	public ResponseEntity<byte[]> getMoviePoster(@PathVariable Long id) {
		return movieService.getMoviePoster(id);
	}
}