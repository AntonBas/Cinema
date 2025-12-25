package ua.lviv.bas.cinema.service.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

import ua.lviv.bas.cinema.domain.Movie;
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
	void updatePerson_ShouldMapAndSave() {
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
		doAnswer(invocation -> {
			PersonRequest req = invocation.getArgument(0);
			Person target = invocation.getArgument(1);
			target.setName(req.getName());
			target.setRole(req.getRole());
			return null;
		}).when(personMapper).updatePersonFromRequest(request, existing);
		when(personRepository.save(existing)).thenReturn(updated);
		when(personMapper.toDto(updated)).thenReturn(dto);

		PersonResponse result = personService.updatePerson(1L, request);

		assertThat(result.getName()).isEqualTo("Updated Name");
		assertThat(result.getRole()).isEqualTo(PersonRole.DIRECTOR);
		verify(personMapper).updatePersonFromRequest(request, existing);
	}

	@Test
	void updatePerson_WhenNotFound_ShouldThrowException() {
		PersonRequest request = new PersonRequest("Test", PersonRole.ACTOR);
		when(personRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> personService.updatePerson(1L, request)).isInstanceOf(PersonNotFoundException.class);
	}

	@Test
	void deletePerson_WhenExistsAndNoMovies_ShouldDelete() {
		Person person = new Person();
		person.setId(1L);
		person.setName("Test Person");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.findByActorsId(1L)).thenReturn(Collections.emptyList());
		when(movieRepository.findByDirectorsId(1L)).thenReturn(Collections.emptyList());
		when(movieRepository.findByScreenwritersId(1L)).thenReturn(Collections.emptyList());

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
	void deletePerson_WhenUsedAsActor_ShouldThrowException() {
		Person person = new Person();
		person.setId(1L);
		person.setName("Test Actor");

		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Test Movie");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.findByActorsId(1L)).thenReturn(List.of(movie));
		when(movieRepository.findByDirectorsId(1L)).thenReturn(Collections.emptyList());
		when(movieRepository.findByScreenwritersId(1L)).thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> personService.deletePerson(1L)).isInstanceOf(PersonHasMoviesException.class);

		verify(personRepository, never()).delete(any());
	}

	@Test
	void deletePerson_WhenUsedAsDirector_ShouldThrowException() {
		Person person = new Person();
		person.setId(1L);
		person.setName("Test Director");

		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Test Movie");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.findByActorsId(1L)).thenReturn(Collections.emptyList());
		when(movieRepository.findByDirectorsId(1L)).thenReturn(List.of(movie));
		when(movieRepository.findByScreenwritersId(1L)).thenReturn(Collections.emptyList());

		assertThatThrownBy(() -> personService.deletePerson(1L)).isInstanceOf(PersonHasMoviesException.class);

		verify(personRepository, never()).delete(any());
	}

	@Test
	void deletePerson_WhenUsedAsScreenwriter_ShouldThrowException() {
		Person person = new Person();
		person.setId(1L);
		person.setName("Test Screenwriter");

		Movie movie = new Movie();
		movie.setId(1L);
		movie.setTitle("Test Movie");

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(movieRepository.findByActorsId(1L)).thenReturn(Collections.emptyList());
		when(movieRepository.findByDirectorsId(1L)).thenReturn(Collections.emptyList());
		when(movieRepository.findByScreenwritersId(1L)).thenReturn(List.of(movie));

		assertThatThrownBy(() -> personService.deletePerson(1L)).isInstanceOf(PersonHasMoviesException.class);

		verify(personRepository, never()).delete(any());
	}

	@Test
	void getAllPersons_ShouldReturnAllPersons() {
		Person person = new Person();
		person.setId(1L);
		person.setName("Test Person");

		PersonResponse dto = new PersonResponse();
		dto.setId(1L);
		dto.setName("Test Person");

		when(personRepository.findAll()).thenReturn(List.of(person));
		when(personMapper.toDtoList(List.of(person))).thenReturn(List.of(dto));

		List<PersonResponse> result = personService.getAllPersons();

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getName()).isEqualTo("Test Person");
	}

	@Test
	void quickCreate_ShouldReturnSavedDto() {
		QuickCreatePersonRequest dto = new QuickCreatePersonRequest("Anton Bas", PersonRole.ACTOR);

		Person person = new Person();
		person.setName("Anton Bas");
		person.setRole(PersonRole.ACTOR);

		Person saved = new Person();
		saved.setId(1L);
		saved.setName("Anton Bas");
		saved.setRole(PersonRole.ACTOR);

		PersonResponse mappedDto = new PersonResponse();
		mappedDto.setId(1L);
		mappedDto.setName("Anton Bas");
		mappedDto.setRole(PersonRole.ACTOR);

		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(false);
		when(personMapper.toEntity(dto)).thenReturn(person);
		when(personRepository.save(person)).thenReturn(saved);
		when(personMapper.toDto(saved)).thenReturn(mappedDto);

		PersonResponse result = personService.quickCreate(dto);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Anton Bas");
	}

	@Test
	void quickCreate_WhenDuplicate_ShouldThrowException() {
		QuickCreatePersonRequest dto = new QuickCreatePersonRequest("Anton Bas", PersonRole.ACTOR);
		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(true);

		assertThatThrownBy(() -> personService.quickCreate(dto)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void searchPersons_ShouldReturnPagedResponse() {
		String query = "Anton";
		PersonRole role = PersonRole.ACTOR;
		int page = 0;
		int size = 10;

		Person person = new Person();
		person.setId(1L);
		person.setName("Anton Bas");
		person.setRole(PersonRole.ACTOR);

		Page<Person> personPage = new PageImpl<>(List.of(person));

		when(personRepository.findAll(any(BooleanBuilder.class), any(Pageable.class))).thenReturn(personPage);

		PageResponse<PersonResponse> result = personService.searchPersons(query, role, page, size);

		assertThat(result).isNotNull();
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getCurrentPage()).isEqualTo(0);
		assertThat(result.getTotalElements()).isEqualTo(1);
		verify(personRepository).findAll(any(BooleanBuilder.class), any(Pageable.class));
	}

	@Test
	void searchPersons_WithNullRole_ShouldSearchWithoutRoleFilter() {
		String query = "Anton";
		PersonRole role = null;
		int page = 0;
		int size = 10;

		Person person = new Person();
		person.setId(1L);
		person.setName("Anton Bas");
		person.setRole(PersonRole.ACTOR);

		Page<Person> personPage = new PageImpl<>(List.of(person));

		when(personRepository.findAll(any(BooleanBuilder.class), any(Pageable.class))).thenReturn(personPage);

		PageResponse<PersonResponse> result = personService.searchPersons(query, role, page, size);

		assertThat(result).isNotNull();
		verify(personRepository).findAll(any(BooleanBuilder.class), any(Pageable.class));
	}

	@Test
	void searchPersons_WithNullQuery_ShouldSearchWithoutQueryFilter() {
		String query = null;
		PersonRole role = PersonRole.ACTOR;
		int page = 0;
		int size = 10;

		Person person = new Person();
		person.setId(1L);
		person.setName("Anton Bas");
		person.setRole(PersonRole.ACTOR);

		Page<Person> personPage = new PageImpl<>(List.of(person));

		when(personRepository.findAll(any(BooleanBuilder.class), any(Pageable.class))).thenReturn(personPage);

		PageResponse<PersonResponse> result = personService.searchPersons(query, role, page, size);

		assertThat(result).isNotNull();
		verify(personRepository).findAll(any(BooleanBuilder.class), any(Pageable.class));
	}

	@Test
	void searchPersons_WithEmptyQuery_ShouldSearchWithoutQueryFilter() {
		String query = "";
		PersonRole role = PersonRole.ACTOR;
		int page = 0;
		int size = 10;

		Person person = new Person();
		person.setId(1L);
		person.setName("Anton Bas");
		person.setRole(PersonRole.ACTOR);

		Page<Person> personPage = new PageImpl<>(List.of(person));

		when(personRepository.findAll(any(BooleanBuilder.class), any(Pageable.class))).thenReturn(personPage);

		PageResponse<PersonResponse> result = personService.searchPersons(query, role, page, size);

		assertThat(result).isNotNull();
		verify(personRepository).findAll(any(BooleanBuilder.class), any(Pageable.class));
	}

	@Test
	void searchPersons_WithBlankQuery_ShouldSearchWithoutQueryFilter() {
		String query = "   ";
		PersonRole role = PersonRole.ACTOR;
		int page = 0;
		int size = 10;

		Person person = new Person();
		person.setId(1L);
		person.setName("Anton Bas");
		person.setRole(PersonRole.ACTOR);

		Page<Person> personPage = new PageImpl<>(List.of(person));

		when(personRepository.findAll(any(BooleanBuilder.class), any(Pageable.class))).thenReturn(personPage);

		PageResponse<PersonResponse> result = personService.searchPersons(query, role, page, size);

		assertThat(result).isNotNull();
		verify(personRepository).findAll(any(BooleanBuilder.class), any(Pageable.class));
	}
}