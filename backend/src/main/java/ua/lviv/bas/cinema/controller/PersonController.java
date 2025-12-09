package ua.lviv.bas.cinema.controller;

import java.util.List;

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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.PersonService;

@Slf4j
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
@Validated
@Tag(name = "Person Management", description = "Endpoints for managing movie persons (actors, directors, screenwriters)")
@SecurityRequirement(name = "bearerAuth")
public class PersonController {

	private final PersonService personService;
	private static final String DEFAULT_PAGE = "0";
	private static final String DEFAULT_SIZE = "12";
	private static final int MAX_PAGE_SIZE = 50;

	@GetMapping("/{id}")
	@Operation(summary = "Get person by ID", description = "Retrieve detailed information about a person (actor, director, screenwriter) by ID.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Person found", content = @Content(schema = @Schema(implementation = PersonResponse.class))),
			@ApiResponse(responseCode = "404", description = "Person not found") })
	public ResponseEntity<PersonResponse> getPersonById(
			@Parameter(description = "ID of the person", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/persons/{} - Getting person by id", id);
		PersonResponse person = personService.getPersonById(id);
		return ResponseEntity.ok(person);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Create new person", description = "Create a new person (actor, director, or screenwriter). Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Person created successfully", content = @Content(schema = @Schema(implementation = PersonResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or person with same name and role already exists"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PersonResponse> createPerson(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Person creation request", required = true, content = @Content(schema = @Schema(implementation = PersonRequest.class))) @RequestBody @Valid PersonRequest request) {
		log.info("POST /api/persons - Creating new person: {}", request.getName());
		PersonResponse createdPerson = personService.createPerson(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Update person", description = "Update existing person information. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Person updated successfully", content = @Content(schema = @Schema(implementation = PersonResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data or person with same name and role already exists"),
			@ApiResponse(responseCode = "404", description = "Person not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PersonResponse> updatePerson(
			@Parameter(description = "ID of the person to update", required = true, example = "1") @PathVariable Long id,

			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated person data", required = true, content = @Content(schema = @Schema(implementation = PersonRequest.class))) @RequestBody @Valid PersonRequest request) {
		log.info("PUT /api/persons/{} - Updating person", id);
		PersonResponse updatedPerson = personService.updatePerson(id, request);
		return ResponseEntity.ok(updatedPerson);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Delete person", description = "Delete a person by ID. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Person deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Person not found"),
			@ApiResponse(responseCode = "409", description = "Cannot delete person who is associated with movies"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<Void> deletePerson(
			@Parameter(description = "ID of the person to delete", required = true, example = "1") @PathVariable Long id) {
		log.info("DELETE /api/persons/{} - Deleting person", id);
		personService.deletePerson(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/quick-create")
	@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
	@Operation(summary = "Quick create person", description = "Quickly create a person when creating/updating movies. Requires ADMIN or CONTENT_MANAGER role.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "Person created successfully", content = @Content(schema = @Schema(implementation = PersonResponse.class))),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PersonResponse> quickCreate(
			@io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Quick person creation request", required = true, content = @Content(schema = @Schema(implementation = QuickCreatePersonRequest.class))) @RequestBody @Valid QuickCreatePersonRequest request) {
		log.info("POST /api/persons/quick-create - Quick creating person: {} with role: {}", request.getName(),
				request.getRole());
		PersonResponse createdPerson = personService.quickCreate(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
	}

	@GetMapping
	@Operation(summary = "Get all persons", description = "Retrieve complete list of all persons (actors, directors, screenwriters).")
	@ApiResponse(responseCode = "200", description = "All persons retrieved successfully")
	public ResponseEntity<List<PersonResponse>> getAll() {
		log.info("GET /api/persons - Getting all persons");
		List<PersonResponse> persons = personService.getAllPersons();
		log.debug("Retrieved {} persons", persons.size());
		return ResponseEntity.ok(persons);
	}

	@GetMapping("/search")
	@Operation(summary = "Search persons with pagination", description = "Search persons by name and/or role with pagination support.")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Persons retrieved successfully", content = @Content(schema = @Schema(implementation = PageResponse.class))) })
	public ResponseEntity<PageResponse<PersonResponse>> searchPersons(
			@Parameter(description = "Search query for person name (case-insensitive)", example = "Leonardo") @RequestParam(required = false) String query,

			@Parameter(description = "Filter by person role", example = "ACTOR") @RequestParam(required = false) PersonRole role,

			@Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = DEFAULT_PAGE) int page,

			@Parameter(description = "Number of items per page (max 50)", example = "12") @RequestParam(defaultValue = DEFAULT_SIZE) int size) {

		size = Math.min(size, MAX_PAGE_SIZE);
		log.info("GET /api/persons/search - query: '{}', role: {}, page: {}, size: {}", query, role, page, size);
		PageResponse<PersonResponse> result = personService.searchPersons(query, role, page, size);
		return ResponseEntity.ok(result);
	}
}