package ua.lviv.bas.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.service.cinema.GenreService;

@Slf4j
@RestController
@RequestMapping("/api/admin/genres")
@RequiredArgsConstructor
@Validated
@Tag(name = "Admin Genre Management", description = "Admin endpoints for managing movie genres")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminGenreController {

	private final GenreService genreService;

	@PostMapping
	@Operation(summary = "Create new genre")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Genre created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data or genre name already exists"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<GenreResponse> createGenre(@RequestBody @Valid GenreRequest request) {
		log.info("POST /api/admin/genres - Creating new genre: {}", request.getName());
		GenreResponse createdGenre = genreService.createGenre(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdGenre);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get genre by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Genre found"),
			@ApiResponse(responseCode = "404", description = "Genre not found") })
	public ResponseEntity<GenreResponse> getGenreById(@PathVariable Long id) {
		log.info("GET /api/admin/genres/{} - Getting genre by id", id);
		GenreResponse genre = genreService.getGenreById(id);
		return ResponseEntity.ok(genre);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update genre")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Genre updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data or genre name already exists"),
			@ApiResponse(responseCode = "404", description = "Genre not found") })
	public ResponseEntity<GenreResponse> updateGenre(@PathVariable Long id, @RequestBody @Valid GenreRequest request) {
		log.info("PUT /api/admin/genres/{} - Updating genre", id);
		GenreResponse updatedGenre = genreService.updateGenre(id, request);
		return ResponseEntity.ok(updatedGenre);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete genre")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Genre deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Genre not found") })
	public ResponseEntity<Void> deleteGenre(@PathVariable Long id) {
		log.info("DELETE /api/admin/genres/{} - Deleting genre", id);
		genreService.deleteGenre(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@Operation(summary = "Get genres list sorted by popularity")
	public ResponseEntity<PageResponse<GenreResponse>> getGenres(@RequestParam(required = false) String search,
			@Parameter(hidden = true) @PageableDefault(size = 10) Pageable pageable) {
		log.info("GET /api/admin/genres - search: '{}'", search);
		var result = genreService.searchGenres(search, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}
}