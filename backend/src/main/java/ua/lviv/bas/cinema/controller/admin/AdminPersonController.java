package ua.lviv.bas.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.service.cinema.PersonService;

@Slf4j
@RestController
@RequestMapping("/api/admin/persons")
@RequiredArgsConstructor
@Tag(name = "Admin Person Management", description = "Admin endpoints for managing movie persons")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CONTENT_MANAGER')")
public class AdminPersonController {

	private final PersonService personService;

	@PostMapping
	@Operation(summary = "Create new person", description = "Create a new person (actor, director, or screenwriter).")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Person created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data or person with same name and role already exists"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PersonResponse> createPerson(@RequestBody @Valid PersonRequest request) {
		log.info("POST /api/admin/persons - Creating new person: {}", request.getName());
		PersonResponse createdPerson = personService.createPerson(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
	}

	@PostMapping("/quick")
	@Operation(summary = "Quick create person", description = "Quickly create a person when creating/updating movies.")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Person created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data or person already exists"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PersonResponse> quickCreatePerson(@RequestBody @Valid QuickCreatePersonRequest request) {
		log.info("POST /api/admin/persons/quick - Quick creating person: {} with role: {}", request.getName(),
				request.getRole());
		PersonResponse createdPerson = personService.quickCreatePerson(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get person by ID", description = "Retrieve person details.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Person found"),
			@ApiResponse(responseCode = "404", description = "Person not found"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PersonResponse> getPersonById(
			@Parameter(description = "ID of the person", required = true, example = "1") @PathVariable Long id) {
		log.info("GET /api/admin/persons/{} - Getting person by id", id);
		PersonResponse person = personService.getPersonById(id);
		return ResponseEntity.ok(person);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update person", description = "Update existing person information.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Person updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data or person with same name and role already exists"),
			@ApiResponse(responseCode = "404", description = "Person not found"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PersonResponse> updatePerson(
			@Parameter(description = "ID of the person to update", required = true, example = "1") @PathVariable Long id,
			@RequestBody @Valid PersonRequest request) {
		log.info("PUT /api/admin/persons/{} - Updating person", id);
		PersonResponse updatedPerson = personService.updatePerson(id, request);
		return ResponseEntity.ok(updatedPerson);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Delete person", description = "Delete a person by ID.")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Person deleted successfully"),
			@ApiResponse(responseCode = "404", description = "Person not found"),
			@ApiResponse(responseCode = "409", description = "Cannot delete person who is associated with movies"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<Void> deletePerson(
			@Parameter(description = "ID of the person to delete", required = true, example = "1") @PathVariable Long id) {
		log.info("DELETE /api/admin/persons/{} - Deleting person", id);
		personService.deletePerson(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping
	@Operation(summary = "Get all persons", description = "Get paginated list of all persons with filters and movie statistics.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Persons retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PageResponse<PersonResponse>> getAllPersons(@RequestParam(required = false) String name,
			@RequestParam(required = false) PersonRole role,
			@Parameter(hidden = true) @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/admin/persons - name: '{}', role: {}", name, role);
		var result = personService.searchPersons(name, role, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/actors")
	@Operation(summary = "Get actors", description = "Get paginated list of actors.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Actors retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PageResponse<PersonResponse>> getActors(@RequestParam(required = false) String name,
			@Parameter(hidden = true) @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/admin/persons/actors - name: '{}'", name);
		var result = personService.searchPersons(name, PersonRole.ACTOR, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/directors")
	@Operation(summary = "Get directors", description = "Get paginated list of directors.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Directors retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PageResponse<PersonResponse>> getDirectors(@RequestParam(required = false) String name,
			@Parameter(hidden = true) @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/admin/persons/directors - name: '{}'", name);
		var result = personService.searchPersons(name, PersonRole.DIRECTOR, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/screenwriters")
	@Operation(summary = "Get screenwriters", description = "Get paginated list of screenwriters.")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Screenwriters retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PageResponse<PersonResponse>> getScreenwriters(@RequestParam(required = false) String name,
			@Parameter(hidden = true) @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {

		log.info("GET /api/admin/persons/screenwriters - name: '{}'", name);
		var result = personService.searchPersons(name, PersonRole.SCREENWRITER, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}

	@GetMapping("/popular")
	@Operation(summary = "Get popular persons", description = "Get popular persons (most movies).")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Persons retrieved successfully"),
			@ApiResponse(responseCode = "401", description = "User not authenticated"),
			@ApiResponse(responseCode = "403", description = "User does not have required role") })
	public ResponseEntity<PageResponse<PersonResponse>> getPopularPersons(@RequestParam(required = false) String name,
			@RequestParam(required = false) PersonRole role,
			@Parameter(hidden = true) @PageableDefault(size = 10, sort = "movieCount", direction = Sort.Direction.DESC) Pageable pageable) {

		log.info("GET /api/admin/persons/popular - name: '{}', role: {}", name, role);
		var result = personService.searchPersons(name, role, pageable);
		return ResponseEntity.ok(PageResponse.from(result));
	}
}