package ua.lviv.bas.cinema.controller.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.service.common.PersonService;

@ExtendWith(MockitoExtension.class)
class PersonControllerTest {

	@Mock
	private PersonService personService;

	@InjectMocks
	private PersonController personController;

	private static final Long PERSON_ID = 1L;
	private static final String PERSON_NAME = "Anton Bas";

	private PersonResponse createPersonResponse(Long id, String name, PersonRole role) {
		return PersonResponse.builder().id(id).name(name).role(role).build();
	}

	private PersonRequest createPersonRequest(String name, PersonRole role) {
		return PersonRequest.builder().name(name).role(role).build();
	}

	private QuickCreatePersonRequest createQuickCreatePersonRequest(String name, PersonRole role) {
		return QuickCreatePersonRequest.builder().name(name).role(role).build();
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

		assertEquals(PERSON_ID, responseBody.getId());
		assertEquals(PERSON_NAME, responseBody.getName());
		assertEquals(PersonRole.ACTOR, responseBody.getRole());
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
	void updatePerson_ShouldReturnUpdatedPerson() {
		PersonRequest request = createPersonRequest("Updated Name", PersonRole.DIRECTOR);
		PersonResponse updatedDto = createPersonResponse(PERSON_ID, "Updated Name", PersonRole.DIRECTOR);

		when(personService.updatePerson(eq(PERSON_ID), any(PersonRequest.class))).thenReturn(updatedDto);

		ResponseEntity<PersonResponse> response = personController.updatePerson(PERSON_ID, request);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PersonResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(PERSON_ID, responseBody.getId());
		assertEquals("Updated Name", responseBody.getName());
		assertEquals(PersonRole.DIRECTOR, responseBody.getRole());
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
	void quickCreate_ShouldReturnCreatedPerson() {
		QuickCreatePersonRequest request = createQuickCreatePersonRequest(PERSON_NAME, PersonRole.ACTOR);
		PersonResponse responseDto = createPersonResponse(PERSON_ID, PERSON_NAME, PersonRole.ACTOR);

		when(personService.quickCreate(any(QuickCreatePersonRequest.class))).thenReturn(responseDto);

		ResponseEntity<PersonResponse> response = personController.quickCreate(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		PersonResponse responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(PERSON_ID, responseBody.getId());
		assertEquals(PERSON_NAME, responseBody.getName());
		assertEquals(PersonRole.ACTOR, responseBody.getRole());
		verify(personService).quickCreate(request);
	}

	@Test
	void quickCreate_WhenDuplicateName_ShouldThrowException() {
		QuickCreatePersonRequest request = createQuickCreatePersonRequest("Existing Person", PersonRole.DIRECTOR);

		when(personService.quickCreate(any(QuickCreatePersonRequest.class)))
				.thenThrow(new DuplicateEntityException("Person", "Existing Person"));

		assertThrows(DuplicateEntityException.class, () -> personController.quickCreate(request));
	}
}