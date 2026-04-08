package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.cinema.enums.PersonRole;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
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

	private QuickCreatePersonRequest createQuickCreatePersonRequest(String name, PersonRole role) {
		return new QuickCreatePersonRequest(name, role);
	}

	@Test
	void createPerson_ShouldReturnCreatedPerson() {
		PersonRequest request = createPersonRequest(PERSON_NAME, PersonRole.ACTOR);
		PersonResponse responseDto = createPersonResponse(PERSON_ID, PERSON_NAME, PersonRole.ACTOR);

		when(personService.createPerson(any(PersonRequest.class))).thenReturn(responseDto);

		ResponseEntity<PersonResponse> response = personController.createPerson(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		PersonResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(PERSON_ID, responseBody.id());
		assertEquals(PERSON_NAME, responseBody.name());
		assertEquals(PersonRole.ACTOR, responseBody.role());
		verify(personService).createPerson(request);
	}

	@Test
	void createPerson_WhenDuplicateName_ShouldThrowException() {
		PersonRequest request = createPersonRequest("Existing Person", PersonRole.ACTOR);

		when(personService.createPerson(any(PersonRequest.class)))
				.thenThrow(new DuplicateEntityException("Person", "Existing Person"));

		assertThrows(DuplicateEntityException.class, () -> personController.createPerson(request));
	}

	@Test
	void quickCreatePerson_ShouldReturnCreatedPerson() {
		QuickCreatePersonRequest request = createQuickCreatePersonRequest(PERSON_NAME, PersonRole.ACTOR);
		PersonResponse responseDto = createPersonResponse(PERSON_ID, PERSON_NAME, PersonRole.ACTOR);

		when(personService.quickCreatePerson(any(QuickCreatePersonRequest.class))).thenReturn(responseDto);

		ResponseEntity<PersonResponse> response = personController.quickCreatePerson(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		PersonResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(PERSON_ID, responseBody.id());
		assertEquals(PERSON_NAME, responseBody.name());
		assertEquals(PersonRole.ACTOR, responseBody.role());
		verify(personService).quickCreatePerson(request);
	}

	@Test
	void quickCreatePerson_WhenDuplicateName_ShouldThrowException() {
		QuickCreatePersonRequest request = createQuickCreatePersonRequest("Existing Person", PersonRole.DIRECTOR);

		when(personService.quickCreatePerson(any(QuickCreatePersonRequest.class)))
				.thenThrow(new DuplicateEntityException("Person", "Existing Person"));

		assertThrows(DuplicateEntityException.class, () -> personController.quickCreatePerson(request));
	}

	@Test
	void getPersonById_ShouldReturnPerson() {
		PersonResponse expectedResponse = createPersonResponse(PERSON_ID, PERSON_NAME, PersonRole.ACTOR);

		when(personService.getPersonById(PERSON_ID)).thenReturn(expectedResponse);

		ResponseEntity<PersonResponse> response = personController.getPersonById(PERSON_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PersonResponse responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(PERSON_ID, responseBody.id());
		assertEquals(PERSON_NAME, responseBody.name());
		assertEquals(PersonRole.ACTOR, responseBody.role());
		verify(personService).getPersonById(PERSON_ID);
	}

	@Test
	void getPersonById_WhenNotFound_ShouldThrowException() {
		Long nonExistentId = 999L;

		when(personService.getPersonById(nonExistentId)).thenThrow(new PersonNotFoundException(nonExistentId));

		assertThrows(PersonNotFoundException.class, () -> personController.getPersonById(nonExistentId));
	}

	@Test
	void updatePerson_ShouldReturnUpdatedPerson() {
		PersonRequest request = createPersonRequest("Updated Name", PersonRole.DIRECTOR);
		PersonResponse updatedDto = createPersonResponse(PERSON_ID, "Updated Name", PersonRole.DIRECTOR);

		when(personService.updatePerson(eq(PERSON_ID), any(PersonRequest.class))).thenReturn(updatedDto);

		ResponseEntity<PersonResponse> response = personController.updatePerson(PERSON_ID, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PersonResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(PERSON_ID, responseBody.id());
		assertEquals("Updated Name", responseBody.name());
		assertEquals(PersonRole.DIRECTOR, responseBody.role());
		verify(personService).updatePerson(PERSON_ID, request);
	}

	@Test
	void updatePerson_WhenNotFound_ShouldThrowException() {
		PersonRequest request = createPersonRequest("Updated Person", PersonRole.ACTOR);

		when(personService.updatePerson(eq(999L), any(PersonRequest.class)))
				.thenThrow(new PersonNotFoundException(999L));

		assertThrows(PersonNotFoundException.class, () -> personController.updatePerson(999L, request));
	}

	@Test
	void deletePerson_ShouldReturnNoContent() {
		ResponseEntity<Void> response = personController.deletePerson(PERSON_ID);

		assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
		verify(personService).deletePerson(PERSON_ID);
	}

	@Test
	void deletePerson_WhenNotFound_ShouldThrowException() {
		Long nonExistentId = 999L;

		doThrow(new PersonNotFoundException(nonExistentId)).when(personService).deletePerson(nonExistentId);

		assertThrows(PersonNotFoundException.class, () -> personController.deletePerson(nonExistentId));
	}

	@Test
	void getAllPersons_ShouldReturnPageOfPersons() {
		Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "name"));
		List<PersonListResponse> persons = List.of(createPersonListResponse(1L, "John Doe", PersonRole.ACTOR),
				createPersonListResponse(2L, "Jane Smith", PersonRole.DIRECTOR));
		Page<PersonListResponse> personPage = new PageImpl<>(persons, pageable, 2);

		when(personService.getPersons(isNull(), isNull(), eq(pageable))).thenReturn(personPage);

		ResponseEntity<PageResponse<PersonListResponse>> response = personController.getAllPersons(null, null,
				pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<PersonListResponse> responseBody = response.getBody();
		assertNotNull(responseBody);
		assertEquals(2, responseBody.content().size());
		assertEquals(0, responseBody.number());
		assertEquals(20, responseBody.size());
		assertEquals(2, responseBody.totalElements());

		verify(personService).getPersons(null, null, pageable);
	}
}