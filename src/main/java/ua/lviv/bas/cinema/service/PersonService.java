package ua.lviv.bas.cinema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.PersonRepository;
import ua.lviv.bas.cinema.domain.Person;

@Service
@RequiredArgsConstructor
public class PersonService {

	private final PersonRepository personRepository;

	public List<Person> getAllPersons() {
		return personRepository.findAll();
	}

	public Optional<Person> getPersonById(Long id) {
		return personRepository.findById(id);
	}

	public Person savePerson(Person person) {
		return personRepository.save(person);
	}

	public void deletePerson(Long id) {
		personRepository.deleteById(id);
	}
}
