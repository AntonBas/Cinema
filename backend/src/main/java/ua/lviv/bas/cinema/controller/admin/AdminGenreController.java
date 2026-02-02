package ua.lviv.bas.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import ua.lviv.bas.cinema.domain.projection.GenreProjection;
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
	@Operation(summary = "Create new genre", description = "Create a new movie genre.")
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
	@Operation(summary = "Get genre by ID (admin)", description = "Retrieve genre details (admin version).")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Genre found"),
			@ApiResponse(responseCode = "404", description = "Genre not found"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<GenreResponse> getGenreById(
			@Parameter(description = "ID of the genre", required = true, example = "1") @PathVariable Long id) {

		log.info("GET /api/admin/genres/{} - Getting genre by id (admin)", id);
		GenreResponse genre = genreService.getGenreById(id);
		return ResponseEntity.ok(genre);
	}

	@GetMapping("/{id}/stats")
	@Operation(summary = "Get genre statistics", description = "Retrieve genre with movie count statistics.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Genre statistics found"),
			@ApiResponse(responseCode = "404", description = "Genre not found"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<GenreProjection> getGenreStats(
			@Parameter(description = "ID of the genre", required = true, example = "1") @PathVariable Long id) {

		log.info("GET /api/admin/genres/{}/stats - Getting genre statistics", id);
		GenreProjection projection = genreService.getGenreProjectionById(id);
		return ResponseEntity.ok(projection);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update genre", description = "Update existing movie genre information.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Genre updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data or genre name already exists"),
			@ApiResponse(responseCode = "404", description = "Genre not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<GenreResponse> updateGenre(
			@Parameter(description = "ID of the genre to update", required = true, example = "1") @PathVariable Long id,
			@RequestBody @Valid GenreRequest request) {

		log.info("PUT /api/admin/genres/{} - Updating genre", id);
		GenreResponse updatedGenre = genreService.updateGenre(id, request);
		return ResponseEntity.ok(updatedGenre);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete genre", description = "Delete a movie genre by its ID.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Genre deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Genre not found"),
			@ApiResponse(responseCode = "409", description = "Cannot delete genre that is associated with movies"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<Void> deleteGenre(
			@Parameter(description = "ID of the genre to delete", required = true, example = "1") @PathVariable Long id) {

		log.info("DELETE /api/admin/genres/{} - Deleting genre", id);
		genreService.deleteGenre(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@Operation(summary = "Get all genres with statistics", description = "Retrieve paginated list of all genres with movie counts.")
	@ApiResponse(responseCode = "200", description = "Genres retrieved successfully")
	public ResponseEntity<PageResponse<GenreProjection>> getAllGenresWithStats(
			@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/admin/genres - Getting all genres with statistics");
		var result = genreService.getGenreProjections(pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/search")
	@Operation(summary = "Search genres with statistics", description = "Search genres by name with movie count statistics.")
	@ApiResponse(responseCode = "200", description = "Genres retrieved successfully")
	public ResponseEntity<PageResponse<GenreProjection>> searchGenresWithStats(
			@RequestParam(required = false) String query,
			@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/admin/genres/search - query: '{}'", query);
		var result = genreService.searchGenreProjections(query, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}
}