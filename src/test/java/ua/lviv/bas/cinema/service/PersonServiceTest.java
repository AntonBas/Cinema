package ua.lviv.bas.cinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.dao.PersonRepository;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.dto.PersonDto;
import ua.lviv.bas.cinema.mapper.PersonMapper;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {

	@Mock
	private PersonRepository personRepository;

	@Mock
	private PersonMapper personMapper;

	@InjectMocks
	private PersonService personService;

	@Test
	void getAllPersons_ShouldReturnList() {
		Person person = Person.builder().id(1L).name("Anton Bas").build();
		PersonDto dto = PersonDto.builder().id(1L).name("Anton Bas").build();

		when(personRepository.findAll()).thenReturn(List.of(person));
		when(personMapper.toDtoList(List.of(person))).thenReturn(List.of(dto));

		List<PersonDto> result = personService.getAllPersons();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Anton Bas");
	}

	@Test
	void getPersonById_WhenExists_ShouldReturnPersonDto() {
		Person person = Person.builder().id(1L).name("Anton Bas").build();
		PersonDto dto = PersonDto.builder().id(1L).name("Anton Bas").build();

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(personMapper.toDto(person)).thenReturn(dto);

		Optional<PersonDto> result = personService.getPersonById(1L);

		assertThat(result).isPresent();
		assertThat(result.get().getName()).isEqualTo("Anton Bas");
	}

	@Test
	void getPersonById_WhenNotExists_ShouldReturnEmpty() {
		when(personRepository.findById(999L)).thenReturn(Optional.empty());

		Optional<PersonDto> result = personService.getPersonById(999L);

		assertThat(result).isEmpty();
	}

	@Test
	void getPerson_ShouldReturnSavedPersonDto() {
		Person person = Person.builder().id(1L).name("Anton Bas").build();
		Person savedPerson = Person.builder().id(1L).name("Anton Bas").build();
		PersonDto dto = PersonDto.builder().id(1L).name("Anton Bas").build();

		when(personRepository.save(person)).thenReturn(savedPerson);
		when(personMapper.toDto(savedPerson)).thenReturn(dto);

		PersonDto result = personService.savePerson(person);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Anton Bas");
	}

	@Test
	void deletePerson_ShouldCallRepository() {
		personService.deletePerson(1L);
		verify(personRepository).deleteById(1L);
	}

	@Test
	void findEntityById_WhenExists_ShouldReturnPerson() {
		Person person = Person.builder().id(1L).name("Anton Bas").build();
		when(personRepository.findById(1L)).thenReturn(Optional.of(person));

		Optional<Person> result = personService.findEntityById(1L);

		assertThat(result).isPresent();
		assertThat(result.get().getName()).isEqualTo("Anton Bas");
	}
}
