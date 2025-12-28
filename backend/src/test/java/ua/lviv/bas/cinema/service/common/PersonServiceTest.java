package ua.lviv.bas.cinema.service.common;

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

import com.querydsl.core.BooleanBuilder;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonHasMoviesException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.mapper.PersonMapper;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;

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
		person.setName("Anton Bas");
		person.setRole(PersonRole.ACTOR);
		Person savedPerson = new Person();
		savedPerson.setId(1L);
		savedPerson.setName("Anton Bas");
		savedPerson.setRole(PersonRole.ACTOR);
		PersonResponse dto = new PersonResponse();
		dto.setId(1L);
		dto.setName("Anton Bas");
		dto.setRole(PersonRole.ACTOR);

		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(false);
		when(personMapper.toEntity(request)).thenReturn(person);
		when(personRepository.save(person)).thenReturn(savedPerson);
		when(personMapper.toDto(savedPerson)).thenReturn(dto);

		PersonResponse result = personService.createPerson(request);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Anton Bas");
		assertThat(result.getRole()).isEqualTo(PersonRole.ACTOR);
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
		saved.setName("Anton Bas");
		saved.setRole(PersonRole.ACTOR);
		PersonResponse mappedDto = new PersonResponse();
		mappedDto.setId(1L);
		mappedDto.setName("Anton Bas");
		mappedDto.setRole(PersonRole.ACTOR);

		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(false);
		when(personRepository.save(any(Person.class))).thenReturn(saved);
		when(personMapper.toDto(saved)).thenReturn(mappedDto);

		PersonResponse result = personService.quickCreatePerson(request);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Anton Bas");
		assertThat(result.getRole()).isEqualTo(PersonRole.ACTOR);
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
		person.setName("Anton Bas");
		PersonResponse dto = new PersonResponse();
		dto.setId(1L);
		dto.setName("Anton Bas");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(personMapper.toDto(person)).thenReturn(dto);

		PersonResponse result = personService.getPersonById(1L);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Anton Bas");
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
		existing.setName("Old Name");
		existing.setRole(PersonRole.ACTOR);
		Person updated = new Person();
		updated.setId(1L);
		updated.setName("Updated Name");
		updated.setRole(PersonRole.DIRECTOR);
		PersonResponse dto = new PersonResponse();
		dto.setId(1L);
		dto.setName("Updated Name");
		dto.setRole(PersonRole.DIRECTOR);

		when(personRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(personRepository.existsByNameAndRoleAndIdNot("Updated Name", PersonRole.DIRECTOR, 1L)).thenReturn(false);
		when(personRepository.save(existing)).thenReturn(updated);
		when(personMapper.toDto(updated)).thenReturn(dto);

		PersonResponse result = personService.updatePerson(1L, request);

		assertThat(result.getName()).isEqualTo("Updated Name");
		assertThat(result.getRole()).isEqualTo(PersonRole.DIRECTOR);
		verify(personMapper).updatePersonFromRequest(request, existing);
	}

	@Test
	void updatePerson_WhenDuplicate_ShouldThrowException() {
		PersonRequest request = new PersonRequest("Duplicate Name", PersonRole.ACTOR);
		Person existing = new Person();
		existing.setId(1L);
		existing.setName("Old Name");
		existing.setRole(PersonRole.ACTOR);

		when(personRepository.findById(1L)).thenReturn(Optional.of(existing));
		when(personRepository.existsByNameAndRoleAndIdNot("Duplicate Name", PersonRole.ACTOR, 1L)).thenReturn(true);

		assertThatThrownBy(() -> personService.updatePerson(1L, request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void deletePerson_WhenExistsAndNoMovies_ShouldDelete() {
		Person person = new Person();
		person.setId(1L);
		person.setName("Test Person");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.exists(any(BooleanBuilder.class))).thenReturn(false);

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
		person.setName("Test Actor");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.exists(any(BooleanBuilder.class))).thenReturn(true);

		assertThatThrownBy(() -> personService.deletePerson(1L)).isInstanceOf(PersonHasMoviesException.class);

		verify(personRepository, never()).delete(any());
	}

	@Test
	void getPersonsByRole_ShouldReturnPageResponse() {
		PersonRole role = PersonRole.ACTOR;
		Pageable pageable = Pageable.ofSize(10);
		Person person = new Person();
		person.setId(1L);
		person.setName("Anton Bas");
		person.setRole(PersonRole.ACTOR);
		Page<Person> personPage = new PageImpl<>(List.of(person));
		PersonResponse dto = new PersonResponse();
		dto.setId(1L);
		dto.setName("Anton Bas");
		dto.setRole(PersonRole.ACTOR);

		when(personRepository.findByRole(role, pageable)).thenReturn(personPage);
		when(personMapper.toDto(person)).thenReturn(dto);

		PageResponse<PersonResponse> result = personService.getPersonsByRole(role, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getName()).isEqualTo("Anton Bas");
	}

	@Test
	void searchPersons_ShouldReturnPagedResponse() {
		String query = "Anton";
		PersonRole role = PersonRole.ACTOR;
		Pageable pageable = Pageable.ofSize(10);
		Person person = new Person();
		person.setId(1L);
		person.setName("Anton Bas");
		person.setRole(PersonRole.ACTOR);
		Page<Person> personPage = new PageImpl<>(List.of(person));
		PersonResponse dto = new PersonResponse();
		dto.setId(1L);
		dto.setName("Anton Bas");
		dto.setRole(PersonRole.ACTOR);

		when(personRepository.findAll(any(BooleanBuilder.class), any(Pageable.class))).thenReturn(personPage);
		when(personMapper.toDto(person)).thenReturn(dto);

		PageResponse<PersonResponse> result = personService.searchPersons(query, role, pageable);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		verify(personRepository).findAll(any(BooleanBuilder.class), any(Pageable.class));
	}

	@Test
	void getPersonsByIds_ShouldReturnListOfDtos() {
		List<Long> ids = List.of(1L, 2L);
		Person person1 = new Person();
		person1.setId(1L);
		person1.setName("Person 1");
		Person person2 = new Person();
		person2.setId(2L);
		person2.setName("Person 2");
		PersonResponse dto1 = new PersonResponse();
		dto1.setId(1L);
		dto1.setName("Person 1");
		PersonResponse dto2 = new PersonResponse();
		dto2.setId(2L);
		dto2.setName("Person 2");

		when(personRepository.findAllById(ids)).thenReturn(List.of(person1, person2));
		when(personMapper.toDtoList(List.of(person1, person2))).thenReturn(List.of(dto1, dto2));

		List<PersonResponse> result = personService.getPersonsByIds(ids);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getName()).isEqualTo("Person 1");
		assertThat(result.get(1).getName()).isEqualTo("Person 2");
	}

	@Test
	void getPersonsByIds_WhenEmptyList_ShouldReturnEmptyList() {
		List<Long> ids = Collections.emptyList();

		List<PersonResponse> result = personService.getPersonsByIds(ids);

		assertThat(result).isEmpty();
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
	void countByRole_ShouldReturnCount() {
		PersonRole role = PersonRole.ACTOR;
		when(personRepository.countByRole(role)).thenReturn(5L);

		long result = personService.countByRole(role);

		assertThat(result).isEqualTo(5L);
	}

	@Test
	void getPopularActors_ShouldReturnListOfActors() {
		int limit = 3;
		Pageable pageable = Pageable.ofSize(limit);
		Person person1 = new Person();
		person1.setId(1L);
		person1.setName("Actor 1");
		person1.setRole(PersonRole.ACTOR);
		Person person2 = new Person();
		person2.setId(2L);
		person2.setName("Actor 2");
		person2.setRole(PersonRole.ACTOR);
		Page<Person> personPage = new PageImpl<>(List.of(person1, person2));
		PersonResponse dto1 = new PersonResponse();
		dto1.setId(1L);
		dto1.setName("Actor 1");
		PersonResponse dto2 = new PersonResponse();
		dto2.setId(2L);
		dto2.setName("Actor 2");

		when(personRepository.findByRole(PersonRole.ACTOR, pageable)).thenReturn(personPage);
		when(personMapper.toDtoList(List.of(person1, person2))).thenReturn(List.of(dto1, dto2));

		List<PersonResponse> result = personService.getPopularActors(limit);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).getName()).isEqualTo("Actor 1");
		assertThat(result.get(1).getName()).isEqualTo("Actor 2");
	}

	@Test
	void getPopularActors_WhenNoActors_ShouldReturnEmptyList() {
		int limit = 3;
		Pageable pageable = Pageable.ofSize(limit);
		Page<Person> personPage = new PageImpl<>(Collections.emptyList());

		when(personRepository.findByRole(PersonRole.ACTOR, pageable)).thenReturn(personPage);
		when(personMapper.toDtoList(Collections.emptyList())).thenReturn(Collections.emptyList());

		List<PersonResponse> result = personService.getPopularActors(limit);

		assertThat(result).isEmpty();
	}
}