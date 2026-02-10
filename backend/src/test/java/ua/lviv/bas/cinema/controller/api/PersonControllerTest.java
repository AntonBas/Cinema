package ua.lviv.bas.cinema.controller.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.service.cinema.PersonService;

@ExtendWith(MockitoExtension.class)
public class PersonControllerTest {

	@Mock
	private PersonService personService;

	@InjectMocks
	private PersonController personController;

	private static final Long PERSON_ID = 1L;
	private static final String PERSON_NAME = "Anton Bas";

	private PersonResponse createPersonResponse(Long id, String name, PersonRole role) {
		return PersonResponse.builder().id(id).name(name).role(role).build();
	}

	@Test
	void getPersonById_ShouldReturnPerson() {
		PersonResponse personResponse = createPersonResponse(PERSON_ID, PERSON_NAME, PersonRole.ACTOR);

		when(personService.getPersonById(PERSON_ID)).thenReturn(personResponse);

		ResponseEntity<PersonResponse> response = personController.getPersonById(PERSON_ID);

		assertEquals(200, response.getStatusCode().value());
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
}