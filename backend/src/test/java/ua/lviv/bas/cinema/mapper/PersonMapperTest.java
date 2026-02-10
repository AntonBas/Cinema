package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.Mockito;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;

public class PersonMapperTest {

	private PersonMapper mapper = Mappers.getMapper(PersonMapper.class);

	@Test
	void toPersonResponseFromEntity() {
		Person person = Person.builder().id(1L).name("John Doe").role(PersonRole.ACTOR).build();

		PersonResponse response = mapper.toPersonResponse(person);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getName()).isEqualTo("John Doe");
		assertThat(response.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void toPersonResponseFromProjection() {
		var projection = Mockito.mock(ua.lviv.bas.cinema.domain.projection.PersonProjection.class);
		Mockito.when(projection.getId()).thenReturn(1L);
		Mockito.when(projection.getName()).thenReturn("Jane Doe");
		Mockito.when(projection.getRole()).thenReturn(PersonRole.DIRECTOR);
		Mockito.when(projection.getMovieCount()).thenReturn(5);

		PersonResponse response = mapper.toPersonResponse(projection);

		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getName()).isEqualTo("Jane Doe");
		assertThat(response.getRole()).isEqualTo(PersonRole.DIRECTOR);
		assertThat(response.getMovieCount()).isEqualTo(5);
	}

	@Test
	void toPersonResponseList() {
		List<Person> persons = Arrays.asList(Person.builder().id(1L).name("Actor 1").build(),
				Person.builder().id(2L).name("Actor 2").build());

		List<PersonResponse> responses = mapper.toPersonResponseList(persons);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).getName()).isEqualTo("Actor 1");
		assertThat(responses.get(1).getName()).isEqualTo("Actor 2");
	}

	@Test
	void toPerson() {
		PersonRequest request = PersonRequest.builder().name("New Person").role(PersonRole.SCREENWRITER).build();

		Person person = mapper.toPerson(request);

		assertThat(person.getName()).isEqualTo("New Person");
		assertThat(person.getRole()).isEqualTo(PersonRole.SCREENWRITER);
	}

	@Test
	void updatePersonFromRequest() {
		Person person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();

		PersonRequest request = PersonRequest.builder().name("New Name").role(PersonRole.DIRECTOR).build();

		mapper.updatePersonFromRequest(request, person);

		assertThat(person.getName()).isEqualTo("New Name");
		assertThat(person.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void toPersonResponseFromNullEntity() {
		PersonResponse response = mapper.toPersonResponse((Person) null);
		assertThat(response).isNull();
	}

	@Test
	void toPersonFromNull() {
		Person person = mapper.toPerson(null);
		assertThat(person).isNull();
	}

	@Test
	void toPersonResponseListFromNull() {
		List<PersonResponse> responses = mapper.toPersonResponseList(null);
		assertThat(responses).isNull();
	}

	@Test
	void updatePersonFromRequestWithNull() {
		Person person = Person.builder().id(1L).name("Original").role(PersonRole.ACTOR).build();

		PersonRequest request = PersonRequest.builder().build();

		mapper.updatePersonFromRequest(request, person);

		assertThat(person.getName()).isEqualTo("Original");
		assertThat(person.getRole()).isEqualTo(PersonRole.ACTOR);
	}
}