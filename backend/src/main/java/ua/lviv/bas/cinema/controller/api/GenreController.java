package ua.lviv.bas.cinema.controller.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.service.common.GenreService;

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
	public ResponseEntity<GenreResponse> getGenreById(@PathVariable Long id) {
		log.info("GET /api/genres/{} - Getting genre by id", id);
		GenreResponse genre = genreService.getGenreById(id);
		return ResponseEntity.ok(genre);
	}

	@GetMapping
	@Operation(summary = "Get all genres with pagination", description = "Retrieve paginated list of all movie genres.")
	@ApiResponse(responseCode = "200", description = "Genres retrieved successfully")
	public ResponseEntity<Page<GenreResponse>> getAllGenres(
			@PageableDefault(size = 12, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
		log.info("GET /api/genres - pageable: {}", pageable);
		Page<GenreResponse> result = genreService.getGenresPage(pageable);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/search")
	@Operation(summary = "Search genres", description = "Search movie genres by name with pagination support.")
	@ApiResponse(responseCode = "200", description = "Genres retrieved successfully")
	public ResponseEntity<Page<GenreResponse>> searchGenres(@RequestParam(required = false) String query,
			@PageableDefault(size = 12, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
		log.info("GET /api/genres/search - query: '{}', pageable: {}", query, pageable);
		Page<GenreResponse> result = genreService.searchGenres(query, pageable);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/all")
	@Operation(summary = "Get all genres without pagination", description = "Retrieve complete list of all movie genres (for dropdowns, filters, etc.).")
	@ApiResponse(responseCode = "200", description = "All genres retrieved successfully")
	public ResponseEntity<List<GenreResponse>> getAllGenresWithoutPagination() {
		log.info("GET /api/genres/all - Getting all genres without pagination");
		List<GenreResponse> genres = genreService.getGenres();
		return ResponseEntity.ok(genres);
	}

	@GetMapping("/select")
	@Operation(summary = "Get genres for select dropdown", description = "Retrieve sorted list of genres for dropdown selection.")
	@ApiResponse(responseCode = "200", description = "Genres retrieved successfully")
	public ResponseEntity<List<GenreResponse>> getGenresForSelect() {
		log.info("GET /api/genres/select - Getting genres for select dropdown");
		List<GenreResponse> genres = genreService.getGenresSorted();
		return ResponseEntity.ok(genres);
	}
}