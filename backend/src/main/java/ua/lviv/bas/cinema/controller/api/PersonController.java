package ua.lviv.bas.cinema.controller.api;

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
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.common.PersonService;

@Slf4j
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@Validated
@Tag(name = "Person API", description = "Public endpoints for viewing movie persons")
public class PersonController {

	private final PersonService personService;

	@GetMapping("/{id}")
	@Operation(summary = "Get person by ID", description = "Retrieve detailed information about a person (actor, director, screenwriter) by ID.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Person found"),
			@ApiResponse(responseCode = "404", description = "Person not found") })
	public ResponseEntity<PersonResponse> getPersonById(
			@Parameter(description = "ID of the person", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/persons/{} - Getting person by id", id);
		PersonResponse person = personService.getPersonById(id);
		return ResponseEntity.ok(person);
	}

	@GetMapping("/search")
	@Operation(summary = "Search persons with pagination", description = "Search persons by name and/or role with pagination support.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Persons retrieved successfully") })
	public ResponseEntity<PageResponse<PersonResponse>> searchPersons(
			@Parameter(description = "Search query for person name (case-insensitive)", example = "Leonardo") @RequestParam(required = false) String query,
			@Parameter(description = "Filter by person role", example = "ACTOR") @RequestParam(required = false) PersonRole role,
			@Parameter(hidden = true) @PageableDefault(size = 12, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/persons/search - query: '{}', role: {}, pageable: {}", query, role, pageable);
		PageResponse<PersonResponse> result = personService.searchPersons(query, role, pageable);
		return ResponseEntity.ok(result);
	}

	@GetMapping("/role/{role}")
	@Operation(summary = "Get persons by role", description = "Get paginated list of persons filtered by role.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Persons retrieved successfully") })
	public ResponseEntity<PageResponse<PersonResponse>> getPersonsByRole(
			@Parameter(description = "Role to filter by", required = true, example = "ACTOR") @PathVariable PersonRole role,
			@Parameter(hidden = true) @PageableDefault(size = 12, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/persons/role/{} - Getting persons by role", role);
		PageResponse<PersonResponse> result = personService.getPersonsByRole(role, pageable);
		return ResponseEntity.ok(result);
	}
}