package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.service.PersonService;

@RestController
@RequestMapping("/api/persons")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class PersonController {

	private final PersonService personService;

	@GetMapping
	public List<Person> getAll() {
		return personService.getAllPersons();
	}

	@GetMapping("/{id}")
	public Person getById(@PathVariable Long id) {
		return personService.getPersonById(id).orElse(null);
	}

	@PostMapping
	public Person create(@RequestBody Person person) {
		return personService.savePerson(person);
	}

	@PutMapping("/{id}")
	public Person update(@PathVariable Long id, @RequestBody Person person) {
		person.setId(id);
		return personService.savePerson(person);
	}

	@DeleteMapping("/{id}")
	public void delete(@PathVariable Long id) {
		personService.deletePerson(id);
	}
}
