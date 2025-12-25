package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
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
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.common.GenreService;

@Slf4j
@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
@Tag(name = "Genre API", description = "Public endpoints for viewing movie genres")
public class GenreController {

	private final GenreService genreService;
	private static final int MAX_PAGE_SIZE = 50;

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

	@GetMapping
	@Operation(summary = "Search genres with pagination", description = "Search movie genres by name with pagination support.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Genres retrieved successfully") })
	public ResponseEntity<PageResponse<GenreResponse>> searchGenres(
			@Parameter(description = "Search query for genre name (case-insensitive)", example = "action") @RequestParam(required = false) String query,

			@Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") int page,

			@Parameter(description = "Number of items per page (max 50)", example = "12") @RequestParam(defaultValue = "12") int size) {

		size = Math.min(size, MAX_PAGE_SIZE);
		log.info("GET /api/genres - query: '{}', page: {}, size: {}", query, page, size);
		PageResponse<GenreResponse> result = genreService.searchGenres(query, page, size);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/all")
	@Operation(summary = "Get all genres", description = "Retrieve complete list of all movie genres without pagination.")
	@ApiResponse(responseCode = "200", description = "All genres retrieved successfully")
	public ResponseEntity<List<GenreResponse>> getAllGenres() {
		log.info("GET /api/genres/all - Getting all genres");
		List<GenreResponse> genres = genreService.getAllGenres();
		return ResponseEntity.ok(genres);
	}
}