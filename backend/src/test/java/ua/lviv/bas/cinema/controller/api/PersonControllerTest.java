package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.service.cinema.PersonService;

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

	@Test
	void getPersonById_ShouldReturnPerson() {
		PersonResponse personResponse = createPersonResponse(PERSON_ID, PERSON_NAME, PersonRole.ACTOR);

		when(personService.getPersonById(PERSON_ID)).thenReturn(personResponse);

		ResponseEntity<PersonResponse> response = personController.getPersonById(PERSON_ID);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		PersonResponse body = response.getBody();
		assertNotNull(body);
		assertEquals(PERSON_ID, body.getId());
		assertEquals(PERSON_NAME, body.getName());
		assertEquals(PersonRole.ACTOR, body.getRole());
		verify(personService).getPersonById(PERSON_ID);
	}

	@Test
	void getPersonById_WhenNotFound_ShouldThrowException() {
		when(personService.getPersonById(999L)).thenThrow(new PersonNotFoundException(999L));

		assertThrows(PersonNotFoundException.class, () -> personController.getPersonById(999L));
		verify(personService).getPersonById(999L);
	}

	@Test
	void searchPersons_ShouldReturnPagedResponse() {
		PersonResponse person1 = createPersonResponse(1L, "Anton Bas", PersonRole.ACTOR);
		PersonResponse person2 = createPersonResponse(2L, "Another Person", PersonRole.ACTOR);
		List<PersonResponse> content = List.of(person1, person2);
		Page<PersonResponse> page = new PageImpl<>(content, DEFAULT_PAGEABLE, 2);

		when(personService.searchPersons(eq("Anton"), eq(PersonRole.ACTOR), any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<PersonResponse>> response = personController.searchPersons("Anton", PersonRole.ACTOR,
				DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<PersonResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getContent().size());
		assertEquals("Anton Bas", body.getContent().get(0).getName());
		assertEquals(PersonRole.ACTOR, body.getContent().get(0).getRole());
		verify(personService).searchPersons(eq("Anton"), eq(PersonRole.ACTOR), any(Pageable.class));
	}

	@Test
	void searchPersons_WithNullQueryAndRole_ShouldReturnPagedResponse() {
		PersonResponse person1 = createPersonResponse(1L, "Person 1", PersonRole.ACTOR);
		PersonResponse person2 = createPersonResponse(2L, "Person 2", PersonRole.DIRECTOR);
		List<PersonResponse> content = List.of(person1, person2);
		Page<PersonResponse> page = new PageImpl<>(content, DEFAULT_PAGEABLE, 2);

		when(personService.searchPersons(any(), any(), any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<PersonResponse>> response = personController.searchPersons(null, null, DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<PersonResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getContent().size());
		verify(personService).searchPersons(any(), any(), any(Pageable.class));
	}

	@Test
	void searchPersons_WithSpecificRole_ShouldReturnPagedResponse() {
		PersonResponse person1 = createPersonResponse(1L, "Director 1", PersonRole.DIRECTOR);
		List<PersonResponse> content = List.of(person1);
		Page<PersonResponse> page = new PageImpl<>(content, DEFAULT_PAGEABLE, 1);

		when(personService.searchPersons(any(), eq(PersonRole.DIRECTOR), any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<PersonResponse>> response = personController.searchPersons(null, PersonRole.DIRECTOR,
				DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<PersonResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getContent().size());
		assertEquals(PersonRole.DIRECTOR, body.getContent().get(0).getRole());
		verify(personService).searchPersons(any(), eq(PersonRole.DIRECTOR), any(Pageable.class));
	}

	@Test
	void getPersonsByRole_ShouldReturnPagedResponse() {
		PersonResponse person1 = createPersonResponse(1L, "Actor 1", PersonRole.ACTOR);
		PersonResponse person2 = createPersonResponse(2L, "Actor 2", PersonRole.ACTOR);
		List<PersonResponse> content = List.of(person1, person2);
		Page<PersonResponse> page = new PageImpl<>(content, DEFAULT_PAGEABLE, 2);

		when(personService.getPersonsByRole(eq(PersonRole.ACTOR), any(Pageable.class))).thenReturn(page);

		ResponseEntity<Page<PersonResponse>> response = personController.getPersonsByRole(PersonRole.ACTOR,
				DEFAULT_PAGEABLE);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<PersonResponse> body = response.getBody();
		assertNotNull(body);
		assertEquals(2, body.getContent().size());
		assertEquals(PersonRole.ACTOR, body.getContent().get(0).getRole());
		assertEquals(PersonRole.ACTOR, body.getContent().get(1).getRole());
		verify(personService).getPersonsByRole(eq(PersonRole.ACTOR), any(Pageable.class));
	}
}