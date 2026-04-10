package ua.lviv.bas.cinema.controller.admin;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.MovieAdminResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.service.cinema.MovieService;

@Slf4j
@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
@Tag(name = "Admin Movies", description = "Admin APIs for managing movies")
public class AdminMovieController {

	private final MovieService movieService;
	private final ObjectMapper objectMapper;

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create new movie")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Movie created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data") })
	public MovieAdminResponse createMovie(@RequestPart("movieData") String movieDataJson,
			@RequestPart(value = "posterFile") MultipartFile posterFile) {
		log.info("POST /api/admin/movies - Creating new movie");
		var request = parseRequest(movieDataJson, MovieCreateRequest.class);
		request.setPosterFile(posterFile);
		return movieService.createMovie(request);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get movie by ID")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie found"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public MovieAdminResponse getMovie(@PathVariable Long id) {
		log.info("GET /api/admin/movies/{} - Getting movie", id);
		return movieService.getMovie(id);
	}

	@GetMapping
	@Operation(summary = "Get movies")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public PageResponse<MovieCardResponse> getMovies(@RequestParam(required = false) String query,
			@RequestParam(required = false) MovieStatus status,
			@PageableDefault(size = 12, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
		log.info("GET /api/admin/movies - query: '{}', status: {}", query, status);
		return PageResponse.from(movieService.getMovies(query, status, pageable));
	}

	@GetMapping("/search")
	@Operation(summary = "Search movies")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movies retrieved successfully") })
	public List<MovieSessionSearchResponse> searchMovies(@RequestParam(required = false) String query) {
		log.info("GET /api/admin/movies/search - query: '{}'", query);
		return movieService.searchMovies(query);
	}

	@PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "Update movie")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Movie updated successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public MovieAdminResponse updateMovie(@PathVariable Long id, @RequestPart("movieData") String movieDataJson,
			@RequestPart(value = "posterFile", required = false) MultipartFile posterFile) {
		log.info("PUT /api/admin/movies/{} - Updating movie", id);
		var request = parseRequest(movieDataJson, MovieUpdateRequest.class);
		request.setPosterFile(posterFile);
		return movieService.updateMovie(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete movie")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Movie deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Movie not found") })
	public void deleteMovie(@PathVariable Long id) {
		log.info("DELETE /api/admin/movies/{} - Deleting movie", id);
		movieService.deleteMovie(id);
	}

	private <T> T parseRequest(String json, Class<T> clazz) {
		try {
			return objectMapper.readValue(json, clazz);
		} catch (JsonProcessingException e) {
			log.error("Error parsing request data", e);
			throw new IllegalArgumentException("Invalid request data format", e);
		}
	}
}