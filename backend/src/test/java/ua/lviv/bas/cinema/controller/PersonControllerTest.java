package ua.lviv.bas.cinema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.service.PersonService;

@WebMvcTest(PersonController.class)
@AutoConfigureMockMvc(addFilters = false)
class PersonControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private PersonService personService;

	@Test
	void getAll_ShouldReturnPersons() throws Exception {
		PersonResponse dto = PersonResponse.builder().id(1L).name("Anton Bas").build();
		when(personService.getAllPersons()).thenReturn(List.of(dto));

		mockMvc.perform(get("/api/persons")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].name").value("Anton Bas"));
	}

	@Test
	void getById_ShouldReturnPerson() throws Exception {
		PersonResponse dto = PersonResponse.builder().id(1L).name("Anton Bas").build();
		when(personService.getPersonById(1L)).thenReturn(dto);

		mockMvc.perform(get("/api/persons/1")).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Anton Bas"));
	}

	@Test
	void create_ShouldReturnCreatedPerson() throws Exception {
		PersonRequest request = PersonRequest.builder().name("Anton Bas").role(PersonRole.ACTOR).build();

		PersonResponse response = PersonResponse.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		when(personService.createPerson(any(PersonRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/persons").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("Anton Bas"))
				.andExpect(jsonPath("$.role").value("ACTOR"));
	}

	@Test
	void update_ShouldReturnUpdatedPerson() throws Exception {
		PersonRequest request = PersonRequest.builder().name("Anton Bas Updated").role(PersonRole.DIRECTOR).build();

		PersonResponse response = PersonResponse.builder().id(1L).name("Anton Bas Updated").role(PersonRole.DIRECTOR).build();

		when(personService.updatePerson(eq(1L), any(PersonRequest.class))).thenReturn(response);

		mockMvc.perform(put("/api/persons/1").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Anton Bas Updated"))
				.andExpect(jsonPath("$.role").value("DIRECTOR"));
	}

	@Test
	void delete_ShouldReturnNoContent() throws Exception {
		doNothing().when(personService).deletePerson(1L);

		mockMvc.perform(delete("/api/persons/1")).andExpect(status().isNoContent());
	}

	@Test
	void quickCreate_ShouldReturnCreatedPerson() throws Exception {
		QuickCreatePersonRequest request = QuickCreatePersonRequest.builder().name("Anton Bas").role(PersonRole.ACTOR).build();

		PersonResponse response = PersonResponse.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		when(personService.quickCreate(any(QuickCreatePersonRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/persons/quick-create").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.name").value("Anton Bas"))
				.andExpect(jsonPath("$.role").value("ACTOR"));
	}

	@Test
	void search_ShouldReturnPagedResponse() throws Exception {
		PersonResponse dto = PersonResponse.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();
		PageResponse<PersonResponse> page = new PageResponse<>(List.of(dto), 0, 1, 1, 10);

		when(personService.searchPersons(eq("Anton"), eq(PersonRole.ACTOR), eq(0), eq(10))).thenReturn(page);

		mockMvc.perform(get("/api/persons/search").param("query", "Anton").param("role", "ACTOR").param("page", "0")
				.param("size", "10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].name").value("Anton Bas"))
				.andExpect(jsonPath("$.totalElements").value(1));
	}
}
