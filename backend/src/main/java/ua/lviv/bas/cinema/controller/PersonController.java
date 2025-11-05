package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class PersonController {

	private final PersonService personService;
	private static final String DEFAULT_PAGE = "0";
	private static final String DEFAULT_SIZE = "10";
	private static final int MAX_PAGE_SIZE = 50;

	@GetMapping("/{id}")
	public ResponseEntity<PersonResponse> getPersonById(@PathVariable Long id) {
		log.info("GET /api/persons/{} - Getting person by id", id);
		PersonResponse person = personService.getPersonById(id);
		return ResponseEntity.ok(person);
	}

	@PostMapping
	public ResponseEntity<PersonResponse> createPerson(@RequestBody @Valid PersonRequest request) {
		log.info("POST /api/persons - Creating new person: {}", request.getName());
		PersonResponse createdPerson = personService.createPerson(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);

	}

	@PutMapping("/{id}")
	public ResponseEntity<PersonResponse> updatePerson(@PathVariable Long id, @RequestBody @Valid PersonRequest request) {
		log.info("PUT /api/persons/{} - Updating person", id);
		PersonResponse updatedPerson = personService.updatePerson(id, request);
		return ResponseEntity.ok(updatedPerson);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
		log.info("DELETE /api/persons/{} - Deleting person", id);
		personService.deletePerson(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/quick-create")
	public ResponseEntity<PersonResponse> quickCreate(@RequestBody @Valid QuickCreatePersonRequest request) {
		log.info("POST /api/persons/quick-create - Quick creating person: {} with role: {}", request.getName(),
				request.getRole());
		PersonResponse createdPerson = personService.quickCreate(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
	}

	@GetMapping
	public ResponseEntity<List<PersonResponse>> getAll() {
		log.info("GET /api/persons - Getting all persons");
		List<PersonResponse> persons = personService.getAllPersons();
		log.debug("Retrieved {} persons", persons.size());
		return ResponseEntity.ok(persons);
	}

	@GetMapping("/search")
	public ResponseEntity<PageResponse<PersonResponse>> searchPersons(@RequestParam(required = false) String query,
			@RequestParam(required = false) PersonRole role, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = DEFAULT_SIZE) int size) {
		size = Math.min(size, MAX_PAGE_SIZE);
		log.info("GET /api/persons/search - query: '{}', role: {}, page: {}, size: {}", query, role, page, size);
		PageResponse<PersonResponse> result = personService.searchPersons(query, role, page, size);
		return ResponseEntity.ok(result);

	}
}