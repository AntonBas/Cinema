package ua.lviv.bas.cinema.service.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {

	@Mock
	private PersonRepository personRepository;
	@Mock
	private MovieRepository movieRepository;
	@Mock
	private PersonMapper personMapper;

	@InjectMocks
	private PersonService personService;

	private final Long PERSON_ID = 1L;
	private final String PERSON_NAME = "John Doe";
	private final PersonRole PERSON_ROLE = PersonRole.ACTOR;
	private Person person;
	private PersonResponse response;
	private PersonRequest request;

	@BeforeEach
	void setUp() {
		person = new Person();
		person.setId(PERSON_ID);
		person.setName(PERSON_NAME);
		person.setRole(PERSON_ROLE);

		response = PersonResponse.builder().id(PERSON_ID).name(PERSON_NAME).role(PERSON_ROLE).build();

		request = PersonRequest.builder().name(PERSON_NAME).role(PERSON_ROLE).build();
	}

	@Test
	void createPerson_Success() {
		when(personRepository.existsByNameAndRole(PERSON_NAME, PERSON_ROLE)).thenReturn(false);
		when(personMapper.toPerson(request)).thenReturn(person);
		when(personRepository.save(person)).thenReturn(person);
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		PersonResponse result = personService.createPerson(request);

		assertThat(result).isEqualTo(response);
		verify(personRepository).save(person);
	}

	@Test
	void createPerson_Duplicate_ThrowsException() {
		when(personRepository.existsByNameAndRole(PERSON_NAME, PERSON_ROLE)).thenReturn(true);

		assertThatThrownBy(() -> personService.createPerson(request)).isInstanceOf(DuplicateEntityException.class);

		verify(personRepository, never()).save(any());
	}

	@Test
	void quickCreatePerson_Success() {
		QuickCreatePersonRequest quickRequest = QuickCreatePersonRequest.builder().name(PERSON_NAME).role(PERSON_ROLE)
				.build();

		when(personRepository.existsByNameAndRole(PERSON_NAME, PERSON_ROLE)).thenReturn(false);
		when(personRepository.save(any(Person.class))).thenReturn(person);
		when(personMapper.toPersonResponse(person)).thenReturn(response);

		PersonResponse result = personService.quickCreatePerson(quickRequest);

		assertThat(result).isEqualTo(response);
	}

	@Test
	void getPersonById_Success() {
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
		Person existing = new Person();
		existing.setId(PERSON_ID);
		existing.setName("Old Name");
		existing.setRole(PERSON_ROLE);

		when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(existing));
		when(personRepository.existsByNameAndRoleAndIdNot(PERSON_NAME, PERSON_ROLE, PERSON_ID)).thenReturn(false);
		when(personRepository.save(existing)).thenReturn(existing);
		when(personMapper.toPersonResponse(existing)).thenReturn(response);

		PersonResponse result = personService.updatePerson(PERSON_ID, request);

		assertThat(result).isEqualTo(response);
		verify(personMapper).updatePersonFromRequest(request, existing);
	}

	@Test
	void updatePerson_Duplicate_ThrowsException() {
		when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(person));
		when(personRepository.existsByNameAndRoleAndIdNot(PERSON_NAME, PERSON_ROLE, PERSON_ID)).thenReturn(true);

		assertThatThrownBy(() -> personService.updatePerson(PERSON_ID, request))
				.isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void deletePerson_Success() {
		when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(person));
		when(movieRepository.countMovieUsageByPersonId(PERSON_ID)).thenReturn(0L);

		personService.deletePerson(PERSON_ID);

		verify(personRepository).delete(person);
	}

	@Test
	void deletePerson_HasMovies_ThrowsException() {
		when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(person));
		when(movieRepository.countMovieUsageByPersonId(PERSON_ID)).thenReturn(3L);

		assertThatThrownBy(() -> personService.deletePerson(PERSON_ID)).isInstanceOf(PersonHasMoviesException.class);

		verify(personRepository, never()).delete(any());
	}

	@Test
	void searchPersons_ReturnsPage() {
		Pageable pageable = PageRequest.of(0, 10);
		PersonProjection projection = createProjection();
		Page<PersonProjection> projectionPage = new PageImpl<>(List.of(projection));

		when(personRepository.findProjectionsByFilters(PERSON_NAME, PERSON_ROLE, pageable)).thenReturn(projectionPage);
		when(personMapper.toPersonResponse(projection)).thenReturn(response);

		Page<PersonResponse> result = personService.searchPersons(PERSON_NAME, PERSON_ROLE, pageable);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0)).isEqualTo(response);
	}

	@Test
	void getPersonsByIds_ReturnsList() {
		List<Long> ids = List.of(1L, 2L);
		when(personRepository.findAllById(ids)).thenReturn(List.of(person));
		when(personMapper.toPersonResponseList(List.of(person))).thenReturn(List.of(response));

		List<PersonResponse> result = personService.getPersonsByIds(ids);

		assertThat(result).hasSize(1);
		assertThat(result.get(0)).isEqualTo(response);
	}

	@Test
	void getPersonsByIds_EmptyList_ReturnsEmptyList() {
		List<PersonResponse> result = personService.getPersonsByIds(List.of());
		assertThat(result).isEmpty();
	}

	@Test
	void existsByNameAndRole_ReturnsTrue() {
		when(personRepository.existsByNameAndRole(PERSON_NAME, PERSON_ROLE)).thenReturn(true);

		boolean result = personService.existsByNameAndRole(PERSON_NAME, PERSON_ROLE);

		assertThat(result).isTrue();
	}

	private PersonProjection createProjection() {
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