package ua.lviv.bas.cinema.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.cinema.enums.PersonRole;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonListResponse;
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
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create new person")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Person created successfully") })
	public PersonResponse createPerson(@RequestBody @Valid PersonRequest request) {
		log.info("POST /api/admin/persons - Creating new person: {}", request.name());
		return personService.createPerson(request);
	}

	@GetMapping
	@Operation(summary = "Get all persons with filters")
	public PageResponse<PersonListResponse> getPersons(@RequestParam(required = false) String query,
			@RequestParam(required = false) PersonRole role, @PageableDefault(size = 12) Pageable pageable) {
		log.info("GET /api/admin/persons - query: '{}', role: {}", query, role);
		return PageResponse.from(personService.getPersons(query, role, pageable));
	}

	@PutMapping("/{id}")
	@Operation(summary = "Update person")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Person updated successfully") })
	public PersonResponse updatePerson(@PathVariable Long id, @RequestBody @Valid PersonRequest request) {
		log.info("PUT /api/admin/persons/{} - Updating person", id);
		return personService.updatePerson(id, request);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Delete person")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Person deleted successfully") })
	public void deletePerson(@PathVariable Long id) {
		log.info("DELETE /api/admin/persons/{} - Deleting person", id);
		personService.deletePerson(id);
	}
}