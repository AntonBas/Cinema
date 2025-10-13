package ua.lviv.bas.cinema.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.GenreDto;
import ua.lviv.bas.cinema.dto.GenreRequest;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.service.GenreService;

@Slf4j
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

	private final GenreService genreService;

	@GetMapping("/{id}")
	public ResponseEntity<GenreDto> getGenreById(@PathVariable Long id) {
		log.info("GET /api/genres/{} - Getting genre by id", id);
		GenreDto genre = genreService.getGenreById(id);
		return ResponseEntity.ok(genre);
	}

	@PostMapping
	public ResponseEntity<GenreDto> createGenre(@RequestBody @Valid GenreRequest request) {
		log.info("POST /api/genres - Creating new genre: {}", request.getName());
		GenreDto createdGenre = genreService.createGenre(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdGenre);
	}

	@PutMapping("/{id}")
	public ResponseEntity<GenreDto> updateGenre(@PathVariable Long id, @RequestBody @Valid GenreRequest request) {
		log.info("PUT /api/genres/{} - Updating genre", id);
		GenreDto updated = genreService.updateGenre(id, request);
		return ResponseEntity.ok(updated);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
		log.info("DELETE /api/genres/{} - Deleting genre", id);
		genreService.deleteGenre(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	public ResponseEntity<PageResponse<GenreDto>> searchGenres(@RequestParam(required = false) String query,
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
		log.info("GET /api/genres - query: '{}', page: {}, size: {}", query, page, size);
		PageResponse<GenreDto> result = genreService.searchGenres(query, page, size);
		return ResponseEntity.ok(result);
	}
}
