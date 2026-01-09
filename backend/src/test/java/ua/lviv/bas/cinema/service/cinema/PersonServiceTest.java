package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import ua.lviv.bas.cinema.service.cinema.PersonService;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

	@Mock
	private PersonRepository personRepository;

	@Mock
	private PersonMapper personMapper;

	@Mock
	private MovieRepository movieRepository;

	@InjectMocks
	private PersonService personService;

	@Test
	void createPerson_ShouldReturnSavedDto() {
		PersonRequest request = new PersonRequest("Anton Bas", PersonRole.ACTOR);
		Person person = new Person();
		Person savedPerson = new Person();
		savedPerson.setId(1L);
		PersonResponse dto = new PersonResponse();
		dto.setId(1L);
		dto.setName("Anton Bas");

		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(false);
		when(personMapper.toEntity(request)).thenReturn(person);
		when(personRepository.save(person)).thenReturn(savedPerson);
		when(personMapper.toDto(savedPerson)).thenReturn(dto);

		PersonResponse result = personService.createPerson(request);
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void createPerson_ShouldTrimName() {
		PersonRequest request = new PersonRequest("  Anton Bas  ", PersonRole.ACTOR);
		Person person = new Person();
		Person savedPerson = new Person();
		savedPerson.setId(1L);

		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(false);
		when(personMapper.toEntity(any(PersonRequest.class))).thenReturn(person);
		when(personRepository.save(person)).thenReturn(savedPerson);

		personService.createPerson(request);
		verify(personRepository).existsByNameAndRole("Anton Bas", PersonRole.ACTOR);
	}

	@Test
	void createPerson_WhenDuplicate_ShouldThrowException() {
		PersonRequest request = new PersonRequest("Anton Bas", PersonRole.ACTOR);
		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(true);
		assertThatThrownBy(() -> personService.createPerson(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void quickCreatePerson_ShouldReturnSavedDto() {
		QuickCreatePersonRequest request = new QuickCreatePersonRequest("Anton Bas", PersonRole.ACTOR);
		Person saved = new Person();
		saved.setId(1L);
		PersonResponse mappedDto = new PersonResponse();
		mappedDto.setId(1L);

		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(false);
		when(personRepository.save(any(Person.class))).thenReturn(saved);
		when(personMapper.toDto(saved)).thenReturn(mappedDto);

		PersonResponse result = personService.quickCreatePerson(request);
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void quickCreatePerson_ShouldTrimName() {
		QuickCreatePersonRequest request = new QuickCreatePersonRequest("  Anton Bas  ", PersonRole.ACTOR);
		Person saved = new Person();
		saved.setId(1L);

		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(false);
		when(personRepository.save(any(Person.class))).thenReturn(saved);

		personService.quickCreatePerson(request);
		verify(personRepository).existsByNameAndRole("Anton Bas", PersonRole.ACTOR);
	}

	@Test
	void quickCreatePerson_WhenDuplicate_ShouldThrowException() {
		QuickCreatePersonRequest request = new QuickCreatePersonRequest("Anton Bas", PersonRole.ACTOR);
		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(true);
		assertThatThrownBy(() -> personService.quickCreatePerson(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void getPersonById_WhenExists_ShouldReturnDto() {
		Person person = new Person();
		person.setId(1L);
		PersonResponse dto = new PersonResponse();
		dto.setId(1L);

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(personMapper.toDto(person)).thenReturn(dto);

		PersonResponse result = personService.getPersonById(1L);
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void getPersonById_WhenNotFound_ShouldThrowException() {
		when(personRepository.findById(1L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> personService.getPersonById(1L)).isInstanceOf(PersonNotFoundException.class);
	}

	@Test
	void updatePerson_ShouldUpdateAndReturnDto() {
		PersonRequest request = new PersonRequest("Updated Name", PersonRole.DIRECTOR);
		Person existing = new Person();
		existing.setId(1L);
		Person updated = new Person();
		updated.setId(1L);
		PersonResponse dto = new PersonResponse();
		dto.setId(1L);

		when(personRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(personRepository.existsByNameAndRoleAndIdNot("Updated Name", PersonRole.DIRECTOR, 1L)).thenReturn(false);
		when(personRepository.save(existing)).thenReturn(updated);
		when(personMapper.toDto(updated)).thenReturn(dto);

		PersonResponse result = personService.updatePerson(1L, request);
		assertThat(result.getId()).isEqualTo(1L);
	}

	@Test
	void updatePerson_ShouldTrimName() {
		PersonRequest request = new PersonRequest("  Updated Name  ", PersonRole.DIRECTOR);
		Person existing = new Person();
		existing.setId(1L);

		when(personRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(personRepository.existsByNameAndRoleAndIdNot("Updated Name", PersonRole.DIRECTOR, 1L)).thenReturn(false);
		when(personRepository.save(existing)).thenReturn(existing);

		personService.updatePerson(1L, request);
		verify(personRepository).existsByNameAndRoleAndIdNot("Updated Name", PersonRole.DIRECTOR, 1L);
	}

	@Test
	void updatePerson_WhenDuplicate_ShouldThrowException() {
		PersonRequest request = new PersonRequest("Duplicate Name", PersonRole.ACTOR);
		Person existing = new Person();
		existing.setId(1L);

		when(personRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(personRepository.existsByNameAndRoleAndIdNot("Duplicate Name", PersonRole.ACTOR, 1L)).thenReturn(true);
		assertThatThrownBy(() -> personService.updatePerson(1L, request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void deletePerson_WhenExistsAndNoMovies_ShouldDelete() {
		Person person = new Person();
		person.setId(1L);

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.countByActorsId(1L)).thenReturn(0L);
		when(movieRepository.countByDirectorsId(1L)).thenReturn(0L);
		when(movieRepository.countByScreenwritersId(1L)).thenReturn(0L);

		personService.deletePerson(1L);
		verify(personRepository).delete(person);
	}

	@Test
	void deletePerson_WhenNotFound_ShouldThrowException() {
		when(personRepository.findById(1L)).thenReturn(Optional.empty());
		assertThatThrownBy(() -> personService.deletePerson(1L)).isInstanceOf(PersonNotFoundException.class);
		verify(personRepository, never()).delete(any());
	}

	@Test
	void deletePerson_WhenUsedInMovies_ShouldThrowException() {
		Person person = new Person();
		person.setId(1L);
		person.setName("Test Person");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.countByActorsId(1L)).thenReturn(1L);
		when(movieRepository.countByDirectorsId(1L)).thenReturn(0L);
		when(movieRepository.countByScreenwritersId(1L)).thenReturn(0L);

		assertThatThrownBy(() -> personService.deletePerson(1L)).isInstanceOf(PersonHasMoviesException.class);
		verify(personRepository, never()).delete(any());
	}

	@Test
	void getPersonsByRole_ShouldReturnPage() {
		PersonRole role = PersonRole.ACTOR;
		Pageable pageable = Pageable.ofSize(10);
		Person person = new Person();
		person.setId(1L);
		Page<Person> personPage = new PageImpl<>(List.of(person));
		PersonResponse dto = new PersonResponse();
		dto.setId(1L);

		when(personRepository.findByRole(role, pageable)).thenReturn(personPage);
		when(personMapper.toDto(person)).thenReturn(dto);

		Page<PersonResponse> result = personService.getPersonsByRole(role, pageable);
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void searchPersons_ShouldReturnPagedResponse() {
		String query = "Anton";
		PersonRole role = PersonRole.ACTOR;
		Pageable pageable = Pageable.ofSize(10);
		Person person = new Person();
		person.setId(1L);
		Page<Person> personPage = new PageImpl<>(List.of(person));
		PersonResponse dto = new PersonResponse();
		dto.setId(1L);

		when(personRepository.searchByNameAndRole(query, role, pageable)).thenReturn(personPage);
		when(personMapper.toDto(person)).thenReturn(dto);

		Page<PersonResponse> result = personService.searchPersons(query, role, pageable);
		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
	}

	@Test
	void searchPersons_WithNullQuery_ShouldReturnAll() {
		PersonRole role = PersonRole.ACTOR;
		Pageable pageable = Pageable.ofSize(10);
		Page<Person> personPage = new PageImpl<>(List.of(new Person()));

		when(personRepository.searchByNameAndRole(null, role, pageable)).thenReturn(personPage);
		personService.searchPersons(null, role, pageable);
		verify(personRepository).searchByNameAndRole(null, role, pageable);
	}

	@Test
	void searchPersons_WithEmptyQuery_ShouldReturnAll() {
		PersonRole role = PersonRole.ACTOR;
		Pageable pageable = Pageable.ofSize(10);
		Page<Person> personPage = new PageImpl<>(List.of(new Person()));

		when(personRepository.searchByNameAndRole(null, role, pageable)).thenReturn(personPage);
		personService.searchPersons("", role, pageable);
		verify(personRepository).searchByNameAndRole(null, role, pageable);
	}

	@Test
	void getPersonsByIds_ShouldReturnListOfDtos() {
		List<Long> ids = List.of(1L, 2L);
		Person person1 = new Person();
		person1.setId(1L);
		Person person2 = new Person();
		person2.setId(2L);
		PersonResponse dto1 = new PersonResponse();
		dto1.setId(1L);
		PersonResponse dto2 = new PersonResponse();
		dto2.setId(2L);

		when(personRepository.findAllById(ids)).thenReturn(List.of(person1, person2));
		when(personMapper.toDtoList(List.of(person1, person2))).thenReturn(List.of(dto1, dto2));

		List<PersonResponse> result = personService.getPersonsByIds(ids);
		assertThat(result).hasSize(2);
	}

	@Test
	void getPersonsByIds_WhenEmptyList_ShouldReturnEmptyList() {
		List<Long> ids = Collections.emptyList();
		List<PersonResponse> result = personService.getPersonsByIds(ids);
		assertThat(result).isEmpty();
		verify(personRepository, never()).findAllById(any());
	}

	@Test
	void getPersonsByIds_WhenNullList_ShouldReturnEmptyList() {
		List<PersonResponse> result = personService.getPersonsByIds(null);
		assertThat(result).isEmpty();
		verify(personRepository, never()).findAllById(any());
	}

	@Test
	void getPersons_ShouldReturnAllPersons() {
		Person person1 = new Person();
		person1.setId(1L);
		Person person2 = new Person();
		person2.setId(2L);
		List<Person> persons = List.of(person1, person2);

		PersonResponse dto1 = new PersonResponse();
		dto1.setId(1L);
		PersonResponse dto2 = new PersonResponse();
		dto2.setId(2L);
		List<PersonResponse> dtos = List.of(dto1, dto2);

		when(personRepository.findAll()).thenReturn(persons);
		when(personMapper.toDtoList(persons)).thenReturn(dtos);

		List<PersonResponse> result = personService.getPersons();
		assertThat(result).hasSize(2);
	}

	@Test
	void existsById_ShouldReturnTrueWhenExists() {
		when(personRepository.existsById(1L)).thenReturn(true);
		boolean result = personService.existsById(1L);
		assertThat(result).isTrue();
	}

	@Test
	void existsById_ShouldReturnFalseWhenNotExists() {
		when(personRepository.existsById(1L)).thenReturn(false);
		boolean result = personService.existsById(1L);
		assertThat(result).isFalse();
	}

	@Test
	void existsByNameAndRole_ShouldReturnTrueWhenExists() {
		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(true);
		boolean result = personService.existsByNameAndRole("Anton Bas", PersonRole.ACTOR);
		assertThat(result).isTrue();
	}

	@Test
	void existsByNameAndRole_ShouldTrimName() {
		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(true);
		boolean result = personService.existsByNameAndRole("  Anton Bas  ", PersonRole.ACTOR);
		assertThat(result).isTrue();
		verify(personRepository).existsByNameAndRole("Anton Bas", PersonRole.ACTOR);
	}

	@Test
	void countByRole_ShouldReturnCount() {
		PersonRole role = PersonRole.ACTOR;
		when(personRepository.countByRole(role)).thenReturn(5L);
		long result = personService.countByRole(role);
		assertThat(result).isEqualTo(5L);
	}

	@Test
	void deletePerson_WhenUsedAsActorDirectorAndScreenwriter_ShouldThrowException() {
		Person person = new Person();
		person.setId(1L);
		person.setName("Test Person");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.countByActorsId(1L)).thenReturn(2L);
		when(movieRepository.countByDirectorsId(1L)).thenReturn(1L);
		when(movieRepository.countByScreenwritersId(1L)).thenReturn(3L);

		assertThatThrownBy(() -> personService.deletePerson(1L)).isInstanceOf(PersonHasMoviesException.class);
		verify(personRepository, never()).delete(any());
	}

	@Test
	void searchPersons_WithSpacesInQuery_ShouldTrim() {
		String query = "  Anton  ";
		PersonRole role = PersonRole.ACTOR;
		Pageable pageable = Pageable.ofSize(10);
		Page<Person> personPage = new PageImpl<>(List.of(new Person()));

		when(personRepository.searchByNameAndRole("Anton", role, pageable)).thenReturn(personPage);
		personService.searchPersons(query, role, pageable);
		verify(personRepository).searchByNameAndRole("Anton", role, pageable);
	}
}