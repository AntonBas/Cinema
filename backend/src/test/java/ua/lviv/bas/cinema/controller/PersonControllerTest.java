package ua.lviv.bas.cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Objects;

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
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.service.PersonService;

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

	private PageResponse<PersonResponse> createPageResponse(List<PersonResponse> content, int currentPage,
			int totalPages, long totalElements, int pageSize) {
		return new PageResponse<>(content, currentPage, totalPages, totalElements, pageSize);
	}

	@Test
	void getPersonById_ShouldReturnPerson() {
		PersonResponse personResponse = createPersonResponse(PERSON_ID, PERSON_NAME, PersonRole.ACTOR);

		when(personService.getPersonById(PERSON_ID)).thenReturn(personResponse);

		ResponseEntity<PersonResponse> response = personController.getPersonById(PERSON_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PersonResponse responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(PERSON_ID, responseBody.getId());
		assertEquals(PERSON_NAME, responseBody.getName());
		assertEquals(PersonRole.ACTOR, responseBody.getRole());
		verify(personService).getPersonById(PERSON_ID);
	}

	@Test
	void getPersonById_WhenNotFound_ShouldThrowException() {
		when(personService.getPersonById(999L)).thenThrow(new PersonNotFoundException(999L));

		assertThrows(PersonNotFoundException.class, () -> personController.getPersonById(999L));
	}

	@Test
	void createPerson_ShouldReturnCreatedPerson() {
		PersonRequest request = createPersonRequest(PERSON_NAME, PersonRole.ACTOR);
		PersonResponse responseDto = createPersonResponse(PERSON_ID, PERSON_NAME, PersonRole.ACTOR);

		when(personService.createPerson(any(PersonRequest.class))).thenReturn(responseDto);

		ResponseEntity<PersonResponse> response = personController.createPerson(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		PersonResponse responseBody = Objects.requireNonNull(response.getBody());
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
		PersonResponse responseBody = Objects.requireNonNull(response.getBody());
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
		PersonResponse responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(PERSON_ID, responseBody.getId());
		assertEquals(PERSON_NAME, responseBody.getName());
		assertEquals(PersonRole.ACTOR, responseBody.getRole());
		verify(personService).quickCreate(request);
	}

	@Test
	void getAll_ShouldReturnListOfPersons() {
		PersonResponse person1 = createPersonResponse(1L, "Person 1", PersonRole.ACTOR);
		PersonResponse person2 = createPersonResponse(2L, "Person 2", PersonRole.DIRECTOR);
		List<PersonResponse> persons = List.of(person1, person2);

		when(personService.getAllPersons()).thenReturn(persons);

		ResponseEntity<List<PersonResponse>> response = personController.getAll();

		assertEquals(HttpStatus.OK, response.getStatusCode());
		List<PersonResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(2, responseBody.size());
		assertEquals("Person 1", responseBody.get(0).getName());
		assertEquals("Person 2", responseBody.get(1).getName());
		verify(personService).getAllPersons();
	}

	@Test
	void searchPersons_ShouldReturnPagedResponse() {
		PersonResponse person1 = createPersonResponse(1L, "Anton Bas", PersonRole.ACTOR);
		List<PersonResponse> content = List.of(person1);
		PageResponse<PersonResponse> pageResponse = createPageResponse(content, 0, 1, 1, 10);

		when(personService.searchPersons(eq("Anton"), eq(PersonRole.ACTOR), eq(0), eq(10))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<PersonResponse>> response = personController.searchPersons("Anton",
				PersonRole.ACTOR, 0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<PersonResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(1, responseBody.getContent().size());
		assertEquals("Anton Bas", responseBody.getContent().get(0).getName());
		assertEquals(0, responseBody.getCurrentPage());
		assertEquals(1, responseBody.getTotalPages());
		assertEquals(1, responseBody.getTotalElements());
		assertEquals(10, responseBody.getPageSize());
		verify(personService).searchPersons("Anton", PersonRole.ACTOR, 0, 10);
	}

	@Test
	void searchPersons_WithNullQueryAndRole_ShouldReturnPagedResponse() {
		PersonResponse person1 = createPersonResponse(1L, "Person 1", PersonRole.ACTOR);
		PersonResponse person2 = createPersonResponse(2L, "Person 2", PersonRole.DIRECTOR);
		List<PersonResponse> content = List.of(person1, person2);
		PageResponse<PersonResponse> pageResponse = createPageResponse(content, 0, 1, 2, 10);

		when(personService.searchPersons(isNull(), isNull(), eq(0), eq(10))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<PersonResponse>> response = personController.searchPersons(null, null, 0, 10);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<PersonResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(2, responseBody.getContent().size());
		verify(personService).searchPersons(null, null, 0, 10);
	}

	@Test
	void searchPersons_WithLargeSize_ShouldLimitToMaxPageSize() {
		PersonResponse person1 = createPersonResponse(1L, "Person 1", PersonRole.ACTOR);
		List<PersonResponse> content = List.of(person1);
		PageResponse<PersonResponse> pageResponse = createPageResponse(content, 0, 1, 1, 50);

		when(personService.searchPersons(isNull(), isNull(), eq(0), eq(50))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<PersonResponse>> response = personController.searchPersons(null, null, 0, 100);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PageResponse<PersonResponse> responseBody = Objects.requireNonNull(response.getBody());
		assertEquals(50, responseBody.getPageSize());
		verify(personService).searchPersons(null, null, 0, 50);
	}
}