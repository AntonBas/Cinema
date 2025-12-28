package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
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
	private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 12, Sort.by("name").ascending());

	private PersonResponse createPersonResponse(Long id, String name, PersonRole role) {
		return PersonResponse.builder().id(id).name(name).role(role).build();
	}

	private PageResponse<PersonResponse> createPageResponse(List<PersonResponse> content, int currentPage,
			int totalPages, long totalElements, int pageSize) {
		return PageResponse.<PersonResponse>builder().content(content).currentPage(currentPage).totalPages(totalPages)
				.totalElements(totalElements).pageSize(pageSize).first(currentPage == 0)
				.last(currentPage == totalPages - 1 || totalPages == 0).empty(content == null || content.isEmpty())
				.build();
	}

	@Test
	void getPersonById_ShouldReturnPerson() {
		PersonResponse personResponse = createPersonResponse(PERSON_ID, PERSON_NAME, PersonRole.ACTOR);

		when(personService.getPersonById(PERSON_ID)).thenReturn(personResponse);

		ResponseEntity<PersonResponse> response = personController.getPersonById(PERSON_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PersonResponse responseBody = response.getBody();
		assertNotNull(responseBody);

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
	void getPersonById_WhenIdIsZero_ShouldThrowException() {
		when(personService.getPersonById(0L)).thenThrow(new PersonNotFoundException(0L));

		assertThrows(PersonNotFoundException.class, () -> personController.getPersonById(0L));
	}

	@Test
	void searchPersons_ShouldReturnPagedResponse() {
		PersonResponse person1 = createPersonResponse(1L, "Anton Bas", PersonRole.ACTOR);
		List<PersonResponse> content = List.of(person1);
		PageResponse<PersonResponse> pageResponse = createPageResponse(content, 0, 1, 1, 10);

		when(personService.searchPersons(eq("Anton"), eq(PersonRole.ACTOR), any(Pageable.class)))
				.thenReturn(pageResponse);

		ResponseEntity<PageResponse<PersonResponse>> response = personController.searchPersons("Anton",
				PersonRole.ACTOR, DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<PersonResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.getContent().size());
		assertEquals("Anton Bas", responseBody.getContent().get(0).getName());
		assertEquals(PersonRole.ACTOR, responseBody.getContent().get(0).getRole());
		verify(personService).searchPersons(eq("Anton"), eq(PersonRole.ACTOR), any(Pageable.class));
	}

	@Test
	void searchPersons_WithNullQueryAndRole_ShouldReturnPagedResponse() {
		PersonResponse person1 = createPersonResponse(1L, "Person 1", PersonRole.ACTOR);
		PersonResponse person2 = createPersonResponse(2L, "Person 2", PersonRole.DIRECTOR);
		List<PersonResponse> content = List.of(person1, person2);
		PageResponse<PersonResponse> pageResponse = createPageResponse(content, 0, 1, 2, 10);

		when(personService.searchPersons(isNull(), isNull(), any(Pageable.class))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<PersonResponse>> response = personController.searchPersons(null, null,
				DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<PersonResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.getContent().size());
		verify(personService).searchPersons(isNull(), isNull(), any(Pageable.class));
	}

	@Test
	void searchPersons_WithSpecificRole_ShouldReturnPagedResponse() {
		PersonResponse person1 = createPersonResponse(1L, "Director 1", PersonRole.DIRECTOR);
		List<PersonResponse> content = List.of(person1);
		PageResponse<PersonResponse> pageResponse = createPageResponse(content, 0, 1, 1, 10);

		when(personService.searchPersons(isNull(), eq(PersonRole.DIRECTOR), any(Pageable.class)))
				.thenReturn(pageResponse);

		ResponseEntity<PageResponse<PersonResponse>> response = personController.searchPersons(null,
				PersonRole.DIRECTOR, DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<PersonResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(1, responseBody.getContent().size());
		assertEquals(PersonRole.DIRECTOR, responseBody.getContent().get(0).getRole());
		verify(personService).searchPersons(isNull(), eq(PersonRole.DIRECTOR), any(Pageable.class));
	}

	@Test
	void getPersonsByRole_ShouldReturnPagedResponse() {
		PersonResponse person1 = createPersonResponse(1L, "Actor 1", PersonRole.ACTOR);
		PersonResponse person2 = createPersonResponse(2L, "Actor 2", PersonRole.ACTOR);
		List<PersonResponse> content = List.of(person1, person2);
		PageResponse<PersonResponse> pageResponse = createPageResponse(content, 0, 1, 2, 10);

		when(personService.getPersonsByRole(eq(PersonRole.ACTOR), any(Pageable.class))).thenReturn(pageResponse);

		ResponseEntity<PageResponse<PersonResponse>> response = personController.getPersonsByRole(PersonRole.ACTOR,
				DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());

		PageResponse<PersonResponse> responseBody = response.getBody();
		assertNotNull(responseBody);

		assertEquals(2, responseBody.getContent().size());
		assertEquals(PersonRole.ACTOR, responseBody.getContent().get(0).getRole());
		assertEquals(PersonRole.ACTOR, responseBody.getContent().get(1).getRole());
		verify(personService).getPersonsByRole(eq(PersonRole.ACTOR), any(Pageable.class));
	}
}