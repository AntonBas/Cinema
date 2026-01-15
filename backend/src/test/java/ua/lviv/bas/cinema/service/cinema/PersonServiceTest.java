package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonHasMoviesException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.mapper.PersonMapper;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {

	@Mock
	private PersonRepository personRepository;

	@Mock
	private PersonMapper personMapper;

	@Mock
	private MovieRepository movieRepository;

	@InjectMocks
	private PersonService personService;

	@Test
	void createPerson_Success() {
		PersonRequest request = PersonRequest.builder().name("John Doe").role(PersonRole.ACTOR).build();

		Person person = new Person();
		person.setName("John Doe");

		Person savedPerson = new Person();
		savedPerson.setId(1L);
		savedPerson.setName("John Doe");

		PersonResponse response = PersonResponse.builder().id(1L).name("John Doe").build();

		when(personRepository.existsByNameAndRole("John Doe", PersonRole.ACTOR)).thenReturn(false);
		when(personMapper.toPerson(request)).thenReturn(person);
		when(personRepository.save(person)).thenReturn(savedPerson);
		when(personMapper.toPersonResponse(savedPerson)).thenReturn(response);

		PersonResponse result = personService.createPerson(request);

		assertThat(result.getName()).isEqualTo("John Doe");
	}

	@Test
	void createPerson_WhenNameExists_ShouldThrowException() {
		PersonRequest request = PersonRequest.builder().name("John Doe").role(PersonRole.ACTOR).build();

		when(personRepository.existsByNameAndRole("John Doe", PersonRole.ACTOR)).thenReturn(true);

		assertThatThrownBy(() -> personService.createPerson(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void quickCreatePerson_Success() {
		QuickCreatePersonRequest request = QuickCreatePersonRequest.builder().name("Jane Doe").role(PersonRole.DIRECTOR)
				.build();

		Person savedPerson = new Person();
		savedPerson.setId(1L);
		savedPerson.setName("Jane Doe");

		PersonResponse response = PersonResponse.builder().id(1L).name("Jane Doe").build();

		when(personRepository.existsByNameAndRole("Jane Doe", PersonRole.DIRECTOR)).thenReturn(false);
		when(personRepository.save(any(Person.class))).thenReturn(savedPerson);
		when(personMapper.toPersonResponse(savedPerson)).thenReturn(response);

		PersonResponse result = personService.quickCreatePerson(request);

		assertThat(result.getName()).isEqualTo("Jane Doe");
	}

	@Test
	void getPersonById_Success() {
		Person person = new Person();
		person.setId(1L);
		person.setName("John Doe");

		PersonResponse response = PersonResponse.builder().id(1L).name("John Doe").build();

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		PersonResponse result = personService.getPersonById(1L);

		assertThat(result.getName()).isEqualTo("John Doe");
	}

	@Test
	void getPersonById_WhenNotFound_ShouldThrowException() {
		when(personRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> personService.getPersonById(1L)).isInstanceOf(PersonNotFoundException.class);
	}

	@Test
	void updatePerson_Success() {
		Person person = new Person();
		person.setId(1L);
		person.setName("Old Name");

		PersonRequest request = PersonRequest.builder().name("New Name").role(PersonRole.ACTOR).build();

		PersonResponse response = PersonResponse.builder().id(1L).name("New Name").build();

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(personRepository.existsByNameAndRoleAndIdNot("New Name", PersonRole.ACTOR, 1L)).thenReturn(false);
		when(personRepository.save(person)).thenReturn(person);
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		PersonResponse result = personService.updatePerson(1L, request);

		assertThat(result.getName()).isEqualTo("New Name");
	}

	@Test
	void updatePerson_WhenNameExists_ShouldThrowException() {
		Person person = new Person();
		person.setId(1L);
		person.setName("Old Name");

		PersonRequest request = PersonRequest.builder().name("Existing Name").role(PersonRole.ACTOR).build();

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(personRepository.existsByNameAndRoleAndIdNot("Existing Name", PersonRole.ACTOR, 1L)).thenReturn(true);

		assertThatThrownBy(() -> personService.updatePerson(1L, request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void deletePerson_Success() {
		Person person = new Person();
		person.setId(1L);
		person.setName("John Doe");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.countByActorsId(1L)).thenReturn(0L);
		when(movieRepository.countByDirectorsId(1L)).thenReturn(0L);
		when(movieRepository.countByScreenwritersId(1L)).thenReturn(0L);
		doNothing().when(personRepository).delete(person);

		personService.deletePerson(1L);

		verify(personRepository).delete(person);
	}

	@Test
	void deletePerson_WhenUsedInMovies_ShouldThrowException() {
		Person person = new Person();
		person.setId(1L);
		person.setName("John Doe");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.countByActorsId(1L)).thenReturn(1L);

		assertThatThrownBy(() -> personService.deletePerson(1L)).isInstanceOf(PersonHasMoviesException.class);
	}

	@Test
	void getPersonsByRole_Success() {
		Person person = new Person();
		person.setId(1L);

		PersonResponse response = PersonResponse.builder().id(1L).name("John Doe").build();

		Pageable pageable = Pageable.unpaged();
		Page<Person> page = new PageImpl<>(Collections.singletonList(person));

		when(personRepository.findByRole(PersonRole.ACTOR, pageable)).thenReturn(page);
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		Page<PersonResponse> result = personService.getPersonsByRole(PersonRole.ACTOR, pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void searchPersons_WithQuery() {
		Person person = new Person();
		person.setId(1L);

		PersonResponse response = PersonResponse.builder().id(1L).name("John Doe").build();

		Pageable pageable = Pageable.unpaged();
		Page<Person> page = new PageImpl<>(Collections.singletonList(person));

		// Виправлено: використовуємо enum name як String
		when(personRepository.searchByNameAndRole("John", PersonRole.ACTOR.name(), pageable)).thenReturn(page);
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		Page<PersonResponse> result = personService.searchPersons("John", PersonRole.ACTOR, pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void searchPersons_WithEmptyQuery() {
		Person person = new Person();
		person.setId(1L);

		PersonResponse response = PersonResponse.builder().id(1L).name("John Doe").build();

		Pageable pageable = Pageable.unpaged();
		Page<Person> page = new PageImpl<>(Collections.singletonList(person));

		// Виправлено: null для query і enum name як String для role
		when(personRepository.searchByNameAndRole(null, PersonRole.ACTOR.name(), pageable)).thenReturn(page);
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		Page<PersonResponse> result = personService.searchPersons("", PersonRole.ACTOR, pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void searchPersons_WithNullRole() {
		Person person = new Person();
		person.setId(1L);

		PersonResponse response = PersonResponse.builder().id(1L).name("John Doe").build();

		Pageable pageable = Pageable.unpaged();
		Page<Person> page = new PageImpl<>(Collections.singletonList(person));

		// Виправлено: null для role
		when(personRepository.searchByNameAndRole("John", null, pageable)).thenReturn(page);
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		Page<PersonResponse> result = personService.searchPersons("John", null, pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void searchPersons_WithEmptyQueryAndNullRole() {
		Person person = new Person();
		person.setId(1L);

		PersonResponse response = PersonResponse.builder().id(1L).name("John Doe").build();

		Pageable pageable = Pageable.unpaged();
		Page<Person> page = new PageImpl<>(Collections.singletonList(person));

		// Виправлено: обидва параметри null
		when(personRepository.searchByNameAndRole(null, null, pageable)).thenReturn(page);
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		Page<PersonResponse> result = personService.searchPersons("", null, pageable);

		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void getPersonsByIds_Success() {
		Person person1 = new Person();
		person1.setId(1L);

		Person person2 = new Person();
		person2.setId(2L);

		PersonResponse response1 = PersonResponse.builder().id(1L).build();
		PersonResponse response2 = PersonResponse.builder().id(2L).build();

		when(personRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(person1, person2));
		when(personMapper.toPersonResponseList(Arrays.asList(person1, person2)))
				.thenReturn(Arrays.asList(response1, response2));

		List<PersonResponse> result = personService.getPersonsByIds(Arrays.asList(1L, 2L));

		assertThat(result).hasSize(2);
	}

	@Test
	void getPersonsByIds_WithEmptyList() {
		List<PersonResponse> result = personService.getPersonsByIds(Collections.emptyList());

		assertThat(result).isEmpty();
	}

	@Test
	void getPersons_Success() {
		Person person = new Person();
		person.setId(1L);

		PersonResponse response = PersonResponse.builder().id(1L).name("John Doe").build();

		when(personRepository.findAll()).thenReturn(Collections.singletonList(person));
		when(personMapper.toPersonResponseList(Collections.singletonList(person)))
				.thenReturn(Collections.singletonList(response));

		List<PersonResponse> result = personService.getPersons();

		assertThat(result).hasSize(1);
	}

	@Test
	void existsById_Success() {
		when(personRepository.existsById(1L)).thenReturn(true);

		boolean result = personService.existsById(1L);

		assertThat(result).isTrue();
	}

	@Test
	void existsByNameAndRole_Success() {
		when(personRepository.existsByNameAndRole("John Doe", PersonRole.ACTOR)).thenReturn(true);

		boolean result = personService.existsByNameAndRole("John Doe", PersonRole.ACTOR);

		assertThat(result).isTrue();
	}

	@Test
	void countByRole_Success() {
		when(personRepository.countByRole(PersonRole.ACTOR)).thenReturn(5L);

		long result = personService.countByRole(PersonRole.ACTOR);

		assertThat(result).isEqualTo(5);
	}
}