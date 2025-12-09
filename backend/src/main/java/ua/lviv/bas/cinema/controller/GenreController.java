package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
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
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Genre Management", description = "Endpoints for managing movie genres")
@SecurityRequirement(name = "bearerAuth")
public class GenreController {

	private final GenreService genreService;
	private static final int MAX_PAGE_SIZE = 50;

	@GetMapping("/{id}")
	@Operation(summary = "Get genre by ID", description = "Retrieve genre information by its unique identifier.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Genre found", content = @Content(schema = @Schema(implementation = GenreResponse.class))),
			@ApiResponse(responseCode = "404", description = "Genre not found") })
	public ResponseEntity<GenreResponse> getGenreById(
			@Parameter(description = "ID of the genre", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/genres/{} - Getting genre by id", id);
		GenreResponse genre = genreService.getGenreById(id);
		return ResponseEntity.ok(genre);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Create new genre", description = "Create a new movie genre. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Genre created successfully", content = @Content(schema = @Schema(implementation = GenreResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or genre name already exists"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<GenreResponse> createGenre(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Genre creation request", required = true, content = @Content(schema = @Schema(implementation = GenreRequest.class))) @RequestBody @Valid GenreRequest request) {
		log.info("POST /api/genres - Creating new genre: {}", request.getName());
		GenreResponse createdGenre = genreService.createGenre(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdGenre);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Update genre", description = "Update existing movie genre information. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Genre updated successfully", content = @Content(schema = @Schema(implementation = GenreResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or genre name already exists"),
			@ApiResponse(responseCode = "404", description = "Genre not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<GenreResponse> updateGenre(
			@Parameter(description = "ID of the genre to update", required = true, example = "1") @PathVariable Long id,

			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated genre data", required = true, content = @Content(schema = @Schema(implementation = GenreRequest.class))) @RequestBody @Valid GenreRequest request) {
		log.info("PUT /api/genres/{} - Updating genre", id);
		GenreResponse updatedGenre = genreService.updateGenre(id, request);
		return ResponseEntity.ok(updatedGenre);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Delete genre", description = "Delete a movie genre by its ID. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Genre deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Genre not found"),
			@ApiResponse(responseCode = "409", description = "Cannot delete genre that is associated with movies"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<Void> deleteGenre(
			@Parameter(description = "ID of the genre to delete", required = true, example = "1") @PathVariable Long id) {
		log.info("DELETE /api/genres/{} - Deleting genre", id);
		genreService.deleteGenre(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@Operation(summary = "Search genres with pagination", description = "Search movie genres by name with pagination support.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Genres retrieved successfully", content = @Content(schema = @Schema(implementation = PageResponse.class))) })
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