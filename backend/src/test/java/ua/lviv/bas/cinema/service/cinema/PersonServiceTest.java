package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import ua.lviv.bas.cinema.domain.projection.PersonProjection;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonHasMoviesException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.mapper.PersonMapper;
import ua.lviv.bas.cinema.repository.PersonRepository;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

	@Mock
	private PersonRepository personRepository;

	@Mock
	private PersonMapper personMapper;

	@InjectMocks
	private PersonService personService;

	private final Long PERSON_ID = 1L;
	private final String PERSON_NAME = "John Doe";
	private final PersonRole PERSON_ROLE = PersonRole.ACTOR;

	@Test
	void createPerson_Success() {
		PersonRequest request = PersonRequest.builder().name(PERSON_NAME).role(PERSON_ROLE).build();

		Person person = createPerson();
		PersonResponse response = createPersonResponse();

		when(personRepository.existsByNameAndRole(PERSON_NAME, PERSON_ROLE)).thenReturn(false);
		when(personMapper.toPerson(request)).thenReturn(person);
		when(personRepository.save(person)).thenReturn(person);
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		PersonResponse result = personService.createPerson(request);

		assertThat(result).isEqualTo(response);
		verify(personRepository).save(person);
	}

	@Test
	void createPerson_DuplicateName_ThrowsException() {
		PersonRequest request = PersonRequest.builder().name(PERSON_NAME).role(PERSON_ROLE).build();

		when(personRepository.existsByNameAndRole(PERSON_NAME, PERSON_ROLE)).thenReturn(true);

		assertThatThrownBy(() -> personService.createPerson(request)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void quickCreatePerson_Success() {
		QuickCreatePersonRequest request = QuickCreatePersonRequest.builder().name(PERSON_NAME).role(PERSON_ROLE)
				.build();

		Person person = createPerson();
		PersonResponse response = createPersonResponse();

		when(personRepository.existsByNameAndRole(PERSON_NAME, PERSON_ROLE)).thenReturn(false);
		when(personRepository.save(any(Person.class))).thenReturn(person);
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		PersonResponse result = personService.quickCreatePerson(request);

		assertThat(result).isEqualTo(response);
		verify(personRepository).save(any(Person.class));
	}

	@Test
	void getPersonById_Success() {
		Person person = createPerson();
		PersonResponse response = createPersonResponse();

		when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(person));
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		PersonResponse result = personService.getPersonById(PERSON_ID);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getPersonById_NotFound_ThrowsException() {
		when(personRepository.findById(PERSON_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> personService.getPersonById(PERSON_ID)).isInstanceOf(PersonNotFoundException.class);
	}

	@Test
	void updatePerson_Success() {
		Person existingPerson = createPerson();
		existingPerson.setName("Old Name");

		PersonRequest request = PersonRequest.builder().name(PERSON_NAME).role(PERSON_ROLE).build();

		PersonResponse response = createPersonResponse();

		when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(existingPerson));
		when(personRepository.existsByNameAndRoleAndIdNot(PERSON_NAME, PERSON_ROLE, PERSON_ID)).thenReturn(false);
		when(personRepository.save(existingPerson)).thenReturn(existingPerson);
		when(personMapper.toPersonResponse(existingPerson)).thenReturn(response);

		PersonResponse result = personService.updatePerson(PERSON_ID, request);

		assertThat(result).isEqualTo(response);
		verify(personMapper).updatePersonFromRequest(request, existingPerson);
		assertThat(existingPerson.getName()).isEqualTo(PERSON_NAME);
	}

	@Test
	void deletePerson_Success() {
		Person person = createPerson();

		when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(person));
		when(personRepository.countMovieUsage(PERSON_ID)).thenReturn(0L);

		personService.deletePerson(PERSON_ID);

		verify(personRepository).delete(person);
	}

	@Test
	void deletePerson_HasMovies_ThrowsException() {
		Person person = createPerson();

		when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(person));
		when(personRepository.countMovieUsage(PERSON_ID)).thenReturn(3L);

		assertThatThrownBy(() -> personService.deletePerson(PERSON_ID)).isInstanceOf(PersonHasMoviesException.class);
	}

	@Test
	void searchPersons_Success() {
		String searchName = "John";
		Pageable pageable = Pageable.unpaged();
		PersonProjection projection = createPersonProjection();
		Page<PersonProjection> projectionPage = new PageImpl<>(List.of(projection));
		PersonResponse response = createPersonResponse();

		when(personRepository.findProjectionsByFilters(searchName, PERSON_ROLE, pageable)).thenReturn(projectionPage);
		when(personMapper.toPersonResponse(projection)).thenReturn(response);

		Page<PersonResponse> result = personService.searchPersons(searchName, PERSON_ROLE, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(response);
	}

	@Test
	void getPersonsByIds_Success() {
		List<Long> ids = List.of(1L, 2L);
		Person person = createPerson();
		PersonResponse response = createPersonResponse();

		when(personRepository.findAllById(ids)).thenReturn(List.of(person));
		when(personMapper.toPersonResponseList(List.of(person))).thenReturn(List.of(response));

		List<PersonResponse> result = personService.getPersonsByIds(ids);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void existsByNameAndRole_Success() {
		when(personRepository.existsByNameAndRole(PERSON_NAME, PERSON_ROLE)).thenReturn(true);

		boolean result = personService.existsByNameAndRole(PERSON_NAME, PERSON_ROLE);

		assertThat(result).isTrue();
	}

	private Person createPerson() {
		Person person = new Person();
		person.setId(PERSON_ID);
		person.setName(PERSON_NAME);
		person.setRole(PERSON_ROLE);
		return person;
	}

	private PersonResponse createPersonResponse() {
		return PersonResponse.builder().id(PERSON_ID).name(PERSON_NAME).role(PERSON_ROLE).build();
	}

	private PersonProjection createPersonProjection() {
		return new PersonProjection() {
			@Override
			public Long getId() {
				return PERSON_ID;
			}

			@Override
			public String getName() {
				return PERSON_NAME;
			}

			@Override
			public PersonRole getRole() {
				return PERSON_ROLE;
			}

			@Override
			public Integer getMovieCount() {
				return 5;
			}
		};
	}
}