package ua.lviv.bas.cinema.controller.api;

import java.util.List;

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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.service.cinema.PersonService;

@Slf4j
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@Validated
@Tag(name = "Person API", description = "Public endpoints for viewing movie persons")
public class PersonController {

	private final PersonService personService;

	@GetMapping("/{id}")
	@Operation(summary = "Get person by ID", description = "Retrieve person information by ID.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Person found"),
			@ApiResponse(responseCode = "404", description = "Person not found") })
	public ResponseEntity<PersonResponse> getPersonById(
			@Parameter(description = "ID of the person", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/persons/{} - Getting person by id", id);
		PersonResponse person = personService.getPersonById(id);
		return ResponseEntity.ok(person);
	}

	@GetMapping("/search")
	@Operation(summary = "Search persons", description = "Search persons by name and/or role.")
	@ApiResponse(responseCode = "200", description = "Persons retrieved successfully")
	public ResponseEntity<PageResponse<PersonResponse>> searchPersons(@RequestParam(required = false) String name,
			@RequestParam(required = false) PersonRole role,
			@Parameter(hidden = true) @PageableDefault(size = 12, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/persons/search - name: '{}', role: {}", name, role);
		var result = personService.searchPersons(name, role, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/popular")
	@Operation(summary = "Get popular persons", description = "Get list of popular persons sorted by movie count. Used for person selection.")
	@ApiResponse(responseCode = "200", description = "Persons retrieved successfully")
	public ResponseEntity<List<PersonResponse>> getPopularPersons(@RequestParam(required = false) String name,
			@RequestParam(required = false) PersonRole role, @RequestParam(defaultValue = "10") int limit) {

		log.info("GET /api/persons/popular - name: '{}', role: {}, limit: {}", name, role, limit);
		List<PersonResponse> persons = personService.searchPopularPersons(name, role, limit);
		return ResponseEntity.ok(persons);
	}

	@GetMapping("/by-ids")
	@Operation(summary = "Get persons by IDs", description = "Get multiple persons by their IDs. Used for displaying movie cast/crew.")
	@ApiResponse(responseCode = "200", description = "Persons retrieved successfully")
	public ResponseEntity<List<PersonResponse>> getPersonsByIds(
			@Parameter(description = "Comma-separated list of person IDs", example = "1,2,3") @RequestParam List<Long> ids) {

		log.info("GET /api/persons/by-ids - ids: {}", ids);
		List<PersonResponse> persons = personService.getPersonsByIds(ids);
		return ResponseEntity.ok(persons);
	}
}