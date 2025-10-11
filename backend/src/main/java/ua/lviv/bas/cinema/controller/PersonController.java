package ua.lviv.bas.cinema.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.PersonDto;
import ua.lviv.bas.cinema.dto.QuickCreatePersonDto;
import ua.lviv.bas.cinema.mapper.PersonMapper;
import ua.lviv.bas.cinema.service.PersonService;

@Slf4j
@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

	private final PersonService personService;
	private final PersonMapper personMapper;

	@GetMapping
	public ResponseEntity<List<PersonDto>> getAll() {
		log.info("GET /api/persons - Getting all persons");
		try {
			List<PersonDto> persons = personService.getAllPersons();
			log.debug("Retrieved {} persons", persons.size());
			return ResponseEntity.ok(persons);
		} catch (Exception e) {
			log.error("Error getting all persons: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@GetMapping("/{id}")
	public ResponseEntity<PersonDto> getById(@PathVariable Long id) {
		log.info("GET /api/persons/{} - Getting person by id", id);
		try {
			Optional<PersonDto> person = personService.getPersonById(id);
			if (person.isPresent()) {
				log.debug("Found person: {}", person.get().getName());
				return ResponseEntity.ok(person.get());
			} else {
				log.warn("Person with id {} not found", id);
				return ResponseEntity.ok(person.get());
			}
		} catch (Exception e) {
			log.error("Error getting person by id {}: {}", id, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping
	public ResponseEntity<PersonDto> create(@RequestBody @Valid PersonDto personDto) {
		log.info("POST /api/persons - Creating new person: {}", personDto.getName());
		try {
			Person personToSave = personMapper.toEntity(personDto);
			PersonDto savedPerson = personService.savePerson(personToSave);
			log.info("Successfully created person with id: {}", savedPerson.getId());
			return ResponseEntity.status(HttpStatus.CREATED).body(savedPerson);
		} catch (Exception e) {
			log.error("Error creating person: {}", e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}

	@PutMapping("/{id}")
	public ResponseEntity<PersonDto> update(@PathVariable Long id, @RequestBody @Valid PersonDto personDto) {
		personDto.setId(id);

		log.info("PUT /api/persons/{} - Updating person", id);
		try {
			personDto.setId(id);

			Optional<Person> existingPersonOpt = personService.findEntityById(id);
			if (existingPersonOpt.isEmpty()) {
				log.warn("Person with id {} not found for update", id);
				return ResponseEntity.notFound().build();
			}

			Person existingPerson = existingPersonOpt.get();
			personMapper.updateEntityFromDto(personDto, existingPerson);

			PersonDto updatedPerson = personService.savePerson(existingPerson);
			log.info("Successfully updated person with id: {}", id);
			return ResponseEntity.ok(updatedPerson);
		} catch (Exception e) {
			log.error("Error updating person with id {}: {}", id, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		log.info("DELETE /api/persons/{} - Deleting person", id);
		try {
			Optional<Person> existingPerson = personService.findEntityById(id);
			if (existingPerson.isEmpty()) {
				log.warn("Person with id {} not found for deletion", id);
				return ResponseEntity.notFound().build();
			}

			personService.deletePerson(id);
			log.info("Successfully deleted person with id: {}", id);
			return ResponseEntity.noContent().build();
		} catch (Exception e) {
			log.error("Error deleting person with id {}: {}", id, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@PostMapping("/quick-create")
	public ResponseEntity<PersonDto> quickCreate(@RequestBody @Valid QuickCreatePersonDto request) {
		log.info("POST /api/persons/quick-create - Quick creating person: {} with role: {}", request.getName(),
				request.getRole());
		try {
			PersonDto createdPerson = personService.quickCreate(request);
			log.info("Successfully quick-created person with id: {} - {}", createdPerson.getId(),
					createdPerson.getName());
			return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson);
		} catch (Exception e) {
			log.error("Error during quick create for {}: {}", request.getName(), e.getMessage());
			return ResponseEntity.status(HttpStatus.CONFLICT).build();
		}
	}

	@GetMapping("/search")
	public ResponseEntity<PageResponse<PersonDto>> searchPersons(@RequestParam(required = false) String query,
			@RequestParam(required = false) PersonRole role, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {

		log.info("GET /api/persons/search - query: '{}', role: {}, page: {}, size: {}", query, role, page, size);
		try {
			PageResponse<PersonDto> result = personService.searchPersons(query, role, page, size);
			log.debug("Search completed - found {} results, page {}/{}", result.getTotalElements(), page,
					result.getTotalPages());
			return ResponseEntity.ok(result);
		} catch (Exception e) {
			log.error("Search error for query '{}': {}", query, e.getMessage(), e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}
}