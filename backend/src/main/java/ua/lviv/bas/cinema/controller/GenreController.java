package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.GenreService;

@Slf4j
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

	private final GenreService genreService;
	private static final String DEFAULT_PAGE = "0";
	private static final String DEFAULT_SIZE = "10";
	private static final int MAX_PAGE_SIZE = 50;

	@GetMapping("/{id}")
	public ResponseEntity<GenreResponse> getGenreById(@PathVariable Long id) {
		log.info("GET /api/genres/{} - Getting genre by id", id);
		GenreResponse genre = genreService.getGenreById(id);
		return ResponseEntity.ok(genre);
	}

	@PostMapping
	public ResponseEntity<GenreResponse> createGenre(@RequestBody @Valid GenreRequest request) {
		log.info("POST /api/genres - Creating new genre: {}", request.getName());
		GenreResponse createdGenre = genreService.createGenre(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdGenre);
	}

	@PutMapping("/{id}")
	public ResponseEntity<GenreResponse> updateGenre(@PathVariable Long id, @RequestBody @Valid GenreRequest request) {
		log.info("PUT /api/genres/{} - Updating genre", id);
		GenreResponse updatedGenre = genreService.updateGenre(id, request);
		return ResponseEntity.ok(updatedGenre);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
		log.info("DELETE /api/genres/{} - Deleting genre", id);
		genreService.deleteGenre(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<PageResponse<GenreResponse>> searchGenres(@RequestParam(required = false) String query,
			@RequestParam(defaultValue = DEFAULT_PAGE) int page, @RequestParam(defaultValue = DEFAULT_SIZE) int size) {
		size = Math.min(size, MAX_PAGE_SIZE);
		log.info("GET /api/genres - query: '{}', page: {}, size: {}", query, page, size);
		PageResponse<GenreResponse> result = genreService.searchGenres(query, page, size);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/all")
	public ResponseEntity<List<GenreResponse>> getAllGenres() {
		log.info("GET /api/genres/all - Getting all genres");
		List<GenreResponse> genres = genreService.getAllGenres();
		return ResponseEntity.ok(genres);
	}
}
