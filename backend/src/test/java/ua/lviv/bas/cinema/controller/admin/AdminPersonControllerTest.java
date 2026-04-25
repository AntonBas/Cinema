package ua.lviv.bas.cinema.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import ua.lviv.bas.cinema.domain.cinema.enums.PersonRole;
import ua.lviv.bas.cinema.dto.common.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonListResponse;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.service.cinema.PersonService;

@ExtendWith(MockitoExtension.class)
public class AdminPersonControllerTest {

	@Mock
	private PersonService personService;

	@InjectMocks
	private AdminPersonController personController;

	private static final Long PERSON_ID = 1L;
	private static final String PERSON_NAME = "Anton Bas";

	private PersonResponse createPersonResponse(Long id, String name, PersonRole role) {
		return new PersonResponse(id, name, role);
	}

	private PersonListResponse createPersonListResponse(Long id, String name, PersonRole role) {
		return new PersonListResponse(id, name, role, 0);
	}

	private PersonRequest createPersonRequest(String name, PersonRole role) {
		return new PersonRequest(name, role);
	}

	@Test
	void createPersonShouldReturnCreatedPerson() {
		PersonRequest request = createPersonRequest(PERSON_NAME, PersonRole.ACTOR);
		PersonResponse responseDto = createPersonResponse(PERSON_ID, PERSON_NAME, PersonRole.ACTOR);

		when(personService.createPerson(any(PersonRequest.class))).thenReturn(responseDto);

		PersonResponse response = personController.createPerson(request);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(PERSON_ID);
		assertThat(response.name()).isEqualTo(PERSON_NAME);
		assertThat(response.role()).isEqualTo(PersonRole.ACTOR);
		verify(personService).createPerson(request);
	}

	@Test
	void createPersonWhenDuplicateNameShouldThrowException() {
		PersonRequest request = createPersonRequest("Existing Person", PersonRole.ACTOR);

		when(personService.createPerson(any(PersonRequest.class)))
				.thenThrow(new DuplicateEntityException("Person", "Existing Person"));

		assertThrows(DuplicateEntityException.class, () -> personController.createPerson(request));
	}

	@Test
	void getPersonsShouldReturnPageOfPersons() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
		List<PersonListResponse> persons = List.of(createPersonListResponse(1L, "John Doe", PersonRole.ACTOR),
				createPersonListResponse(2L, "Jane Smith", PersonRole.DIRECTOR));
		Page<PersonListResponse> personPage = new PageImpl<>(persons, pageable, 2);

		when(personService.getPersons(null, null, pageable)).thenReturn(personPage);

		PageResponse<PersonListResponse> response = personController.getPersons(null, null, pageable);

		assertThat(response).isNotNull();
		assertThat(response.content()).hasSize(2);
		assertThat(response.number()).isZero();
		assertThat(response.size()).isEqualTo(20);
		assertThat(response.totalElements()).isEqualTo(2);

		verify(personService).getPersons(null, null, pageable);
	}

	@Test
	void getPersonsWithQueryShouldReturnFilteredPersons() {
		String query = "John";
		Pageable pageable = PageRequest.of(0, 20);
		List<PersonListResponse> persons = List.of(createPersonListResponse(1L, "John Doe", PersonRole.ACTOR));
		Page<PersonListResponse> personPage = new PageImpl<>(persons, pageable, 1);

		when(personService.getPersons(eq(query), eq(null), eq(pageable))).thenReturn(personPage);

		PageResponse<PersonListResponse> response = personController.getPersons(query, null, pageable);

		assertThat(response).isNotNull();
		assertThat(response.content()).hasSize(1);
		assertThat(response.content().get(0).name()).isEqualTo("John Doe");

		verify(personService).getPersons(query, null, pageable);
	}

	@Test
	void getPersonsWithRoleShouldReturnFilteredPersons() {
		PersonRole role = PersonRole.ACTOR;
		Pageable pageable = PageRequest.of(0, 20);
		List<PersonListResponse> persons = List.of(createPersonListResponse(1L, "John Doe", PersonRole.ACTOR));
		Page<PersonListResponse> personPage = new PageImpl<>(persons, pageable, 1);

		when(personService.getPersons(eq(null), eq(role), eq(pageable))).thenReturn(personPage);

		PageResponse<PersonListResponse> response = personController.getPersons(null, role, pageable);

		assertThat(response).isNotNull();
		assertThat(response.content()).hasSize(1);
		assertThat(response.content().get(0).role()).isEqualTo(PersonRole.ACTOR);

		verify(personService).getPersons(null, role, pageable);
	}

	@Test
	void updatePersonShouldReturnUpdatedPerson() {
		PersonRequest request = createPersonRequest("Updated Name", PersonRole.DIRECTOR);
		PersonResponse updatedDto = createPersonResponse(PERSON_ID, "Updated Name", PersonRole.DIRECTOR);

		when(personService.updatePerson(eq(PERSON_ID), any(PersonRequest.class))).thenReturn(updatedDto);

		PersonResponse response = personController.updatePerson(PERSON_ID, request);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(PERSON_ID);
		assertThat(response.name()).isEqualTo("Updated Name");
		assertThat(response.role()).isEqualTo(PersonRole.DIRECTOR);
		verify(personService).updatePerson(PERSON_ID, request);
	}

	@Test
	void updatePersonWhenNotFoundShouldThrowException() {
		PersonRequest request = createPersonRequest("Updated Person", PersonRole.ACTOR);

		when(personService.updatePerson(eq(999L), any(PersonRequest.class)))
				.thenThrow(new PersonNotFoundException(999L));

		assertThrows(PersonNotFoundException.class, () -> personController.updatePerson(999L, request));
	}

	@Test
	void deletePersonShouldCallService() {
		personController.deletePerson(PERSON_ID);

		verify(personService).deletePerson(PERSON_ID);
	}

	@Test
	void deletePersonWhenNotFoundShouldThrowException() {
		Long nonExistentId = 999L;

		doThrow(new PersonNotFoundException(nonExistentId)).when(personService).deletePerson(nonExistentId);

		assertThrows(PersonNotFoundException.class, () -> personController.deletePerson(nonExistentId));
	}
}