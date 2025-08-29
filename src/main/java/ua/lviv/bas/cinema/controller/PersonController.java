package ua.lviv.bas.cinema.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.dto.PersonDTO;
import ua.lviv.bas.cinema.mapper.PersonMapper;
import ua.lviv.bas.cinema.service.PersonService;

@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PersonController {

	private final PersonService personService;
	private final PersonMapper personMapper;

	@GetMapping
	public ResponseEntity<List<PersonDTO>> getAll() {
		return ResponseEntity.ok(personService.getAllPersons());
	}

	@GetMapping("/{id}")
	public ResponseEntity<PersonDTO> getById(@PathVariable Long id) {
		return personService.getPersonById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public ResponseEntity<PersonDTO> create(@RequestBody @Valid PersonDTO personDTO) {
		Person personToSave = personMapper.toEntity(personDTO);
		PersonDTO savedPerson = personService.savePerson(personToSave);
		return ResponseEntity.status(HttpStatus.CREATED).body(savedPerson);
	}

	@PutMapping("/{id}")
	public ResponseEntity<PersonDTO> update(@PathVariable Long id, @RequestBody @Valid PersonDTO personDTO) {
		personDTO.setId(id);

		Optional<Person> existingPersonOpt = personService.findEntityById(id);
		if (existingPersonOpt.isEmpty()) {
			return ResponseEntity.notFound().build();
		}

		Person existingPerson = existingPersonOpt.get();
		personMapper.updateEntityFromDto(personDTO, existingPerson);

		PersonDTO updatedPerson = personService.savePerson(existingPerson);
		return ResponseEntity.ok(updatedPerson);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		if (personService.findEntityById(id).isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		personService.deletePerson(id);
		return ResponseEntity.noContent().build();
	}
}