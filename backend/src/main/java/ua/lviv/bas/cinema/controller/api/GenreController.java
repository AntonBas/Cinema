package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.service.cinema.GenreService;

@Slf4j
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
@Validated
@Tag(name = "Genre API", description = "Public endpoints for viewing movie genres")
public class GenreController {

	private final GenreService genreService;

	@GetMapping("/{id}")
	@Operation(summary = "Get genre by ID", description = "Retrieve genre information by its unique identifier.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Genre found"),
			@ApiResponse(responseCode = "404", description = "Genre not found") })
	public ResponseEntity<GenreResponse> getGenreById(
			@Parameter(description = "ID of the genre", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/genres/{} - Getting genre by id", id);
		GenreResponse genre = genreService.getGenreById(id);
		return ResponseEntity.ok(genre);
	}

	@GetMapping("/popular")
	@Operation(summary = "Get popular genres", description = "Get list of popular genres sorted by movie count. Used for genre selection.")
	@ApiResponse(responseCode = "200", description = "Genres retrieved successfully")
	public ResponseEntity<List<GenreResponse>> getPopularGenres(@RequestParam(required = false) String query,
			@RequestParam(defaultValue = "10") int limit) {
		log.info("GET /api/genres/popular - query: '{}', limit: {}", query, limit);
		List<GenreResponse> genres = genreService.searchPopularGenres(query, limit);
		return ResponseEntity.ok(genres);
	}

	@GetMapping("/by-ids")
	@Operation(summary = "Get genres by IDs", description = "Get multiple genres by their IDs. Used for displaying movie genres.")
	@ApiResponse(responseCode = "200", description = "Genres retrieved successfully")
	public ResponseEntity<List<GenreResponse>> getGenresByIds(
			@Parameter(description = "Comma-separated list of genre IDs", example = "1,2,3") @RequestParam List<Long> ids) {
		log.info("GET /api/genres/by-ids - ids: {}", ids);
		List<GenreResponse> genres = genreService.getGenresByIds(ids);
		return ResponseEntity.ok(genres);
	}
}