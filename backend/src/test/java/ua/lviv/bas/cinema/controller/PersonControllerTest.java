package ua.lviv.bas.cinema.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.config.TestSecurityConfig;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.PersonDto;
import ua.lviv.bas.cinema.mapper.PersonMapper;
import ua.lviv.bas.cinema.service.PersonService;

@WebMvcTest(PersonController.class)
@Import(TestSecurityConfig.class)
public class PersonControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PersonService personService;

	@MockitoBean
	private PersonMapper personMapper;

	@Test
	void getAll_ShouldReturnPersons() throws Exception {
		PersonDto dto = PersonDto.builder().id(1L).name("Anton Bas").build();
		when(personService.getAllPersons()).thenReturn(List.of(dto));

		mockMvc.perform(get("/api/persons")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Anton Bas"));
	}

	@Test
	void getById_WhenExists_ShouldReturnPerson() throws Exception {
		PersonDto dto = PersonDto.builder().id(1L).name("Anton Bas").build();
		when(personService.getPersonById(1L)).thenReturn(Optional.of(dto));

		mockMvc.perform(get("/api/persons/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Anton Bas"));
	}

	@Test
	void getById_WhenNotExists_ShouldReturn404() throws Exception {
		when(personService.getPersonById(999L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/api/persons/999")).andExpect(status().isNotFound());
	}

	@Test
	void create_ShouldReturnCreatedPerson() throws Exception {
		PersonDto requestDto = PersonDto.builder().name("Anton Bas").role(PersonRole.ACTOR).build();
		Person person = Person.builder().name("Anton Bas").role(PersonRole.ACTOR).build();
		PersonDto responseDto = PersonDto.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		when(personMapper.toEntity(requestDto)).thenReturn(person);
		when(personService.savePerson(person)).thenReturn(responseDto);

		mockMvc.perform(post("/api/persons").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("Anton Bas"))
				.andExpect(jsonPath("$.role").value("ACTOR"));
	}

	@Test
	void update_WhenExists_ShouldReturnUpdatedPerson() throws Exception {
		PersonDto requestDto = PersonDto.builder().id(1L).name("Anton Bas Updated").role(PersonRole.DIRECTOR).build();
		Person existingPerson = Person.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();
		PersonDto responseDto = PersonDto.builder().id(1L).name("Anton Bas Updated").role(PersonRole.DIRECTOR).build();

		when(personService.findEntityById(1L)).thenReturn(Optional.of(existingPerson));
		when(personService.savePerson(existingPerson)).thenReturn(responseDto);

		mockMvc.perform(put("/api/persons/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Anton Bas Updated"))
				.andExpect(jsonPath("$.role").value("DIRECTOR"));
	}

	@Test
	void update_WhenNotExists_ShouldReturn404() throws Exception {
		PersonDto requestDto = PersonDto.builder().id(999L).name("Anton Bas").role(PersonRole.ACTOR).build();

		when(personService.findEntityById(999L)).thenReturn(Optional.empty());

		mockMvc.perform(put("/api/persons/999").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(requestDto))).andExpect(status().isNotFound());
	}

	@Test
	void delete_WhenExists_ShouldReturnNoContent() throws Exception {
		Person person = Person.builder().id(1L).name("Anton Bas").build();
		when(personService.findEntityById(1L)).thenReturn(Optional.of(person));

		mockMvc.perform(delete("/api/persons/1")).andExpect(status().isNoContent());
	}

	@Test
	void delete_WhenNotExists_ShouldReturn404() throws Exception {
		when(personService.findEntityById(999L)).thenReturn(Optional.empty());

		mockMvc.perform(delete("/api/persons/999")).andExpect(status().isNotFound());
	}
}