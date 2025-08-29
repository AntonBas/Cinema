package ua.lviv.bas.cinema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.PersonRepository;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.dto.PersonDTO;
import ua.lviv.bas.cinema.mapper.PersonMapper;

@Service
@RequiredArgsConstructor
public class PersonService {

	private final PersonRepository personRepository;
	private final PersonMapper personMapper;

	public List<PersonDTO> getAllPersons() {
		return personMapper.toDtoList(personRepository.findAll());
	}

	public Optional<PersonDTO> getPersonById(Long id) {
		return personRepository.findById(id).map(personMapper::toDto);
	}

	public PersonDTO savePerson(Person person) {
		Person savedPerson = personRepository.save(person);
		return personMapper.toDto(savedPerson);
	}

	public void deletePerson(Long id) {
		personRepository.deleteById(id);
	}

	public Optional<Person> findEntityById(Long id) {
		return personRepository.findById(id);
	}

}
