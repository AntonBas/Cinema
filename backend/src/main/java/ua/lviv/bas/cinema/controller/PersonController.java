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
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.PersonDto;
import ua.lviv.bas.cinema.dto.PersonRequest;
import ua.lviv.bas.cinema.dto.QuickCreatePersonDto;
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
	public ResponseEntity<PersonDto> getPersonById(@PathVariable Long id) {
		log.info("GET /api/persons/{} - Getting person by id", id);
		PersonDto person = personService.getPersonById(id);
		return ResponseEntity.ok(person);
	}

	@PostMapping
	public ResponseEntity<PersonDto> createPerson(@RequestBody @Valid PersonRequest request) {
		log.info("POST /api/persons - Creating new person: {}", request.getName());
		PersonDto createdPerson = personService.createPerson(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);

	}

	@PutMapping("/{id}")
	public ResponseEntity<PersonDto> updatePerson(@PathVariable Long id, @RequestBody @Valid PersonRequest request) {
		log.info("PUT /api/persons/{} - Updating person", id);
		PersonDto updatedPerson = personService.updatePerson(id, request);
		return ResponseEntity.ok(updatedPerson);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
		log.info("DELETE /api/persons/{} - Deleting person", id);
		personService.deletePerson(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/quick-create")
	public ResponseEntity<PersonDto> quickCreate(@RequestBody @Valid QuickCreatePersonDto request) {
		log.info("POST /api/persons/quick-create - Quick creating person: {} with role: {}", request.getName(),
				request.getRole());
		PersonDto createdPerson = personService.quickCreate(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
	}

	@GetMapping
	public ResponseEntity<List<PersonDto>> getAll() {
		log.info("GET /api/persons - Getting all persons");
		List<PersonDto> persons = personService.getAllPersons();
		log.debug("Retrieved {} persons", persons.size());
		return ResponseEntity.ok(persons);
	}

	@GetMapping("/search")
	public ResponseEntity<PageResponse<PersonDto>> searchPersons(@RequestParam(required = false) String query,
			@RequestParam(required = false) PersonRole role, @RequestParam(defaultValue = DEFAULT_PAGE) int page,
			@RequestParam(defaultValue = DEFAULT_SIZE) int size) {
		size = Math.min(size, MAX_PAGE_SIZE);
		log.info("GET /api/persons/search - query: '{}', role: {}, page: {}, size: {}", query, role, page, size);
		PageResponse<PersonDto> result = personService.searchPersons(query, role, page, size);
		return ResponseEntity.ok(result);

	}
}