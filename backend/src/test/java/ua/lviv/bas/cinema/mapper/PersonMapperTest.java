package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.cinema.Person;
import ua.lviv.bas.cinema.domain.cinema.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.mapper.cinema.PersonMapper;

public class PersonMapperTest {

	private PersonMapper mapper = Mappers.getMapper(PersonMapper.class);

	@Test
	void toPersonResponseFromEntity() {
		Person person = Person.builder().id(1L).name("John Doe").role(PersonRole.ACTOR).build();

		PersonResponse response = mapper.toPersonResponse(person);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("John Doe");
		assertThat(response.role()).isEqualTo(PersonRole.ACTOR);
		assertThat(response.movieCount()).isNull();
	}

	@Test
	void toPersonResponseList() {
		List<Person> persons = Arrays.asList(Person.builder().id(1L).name("Actor 1").role(PersonRole.ACTOR).build(),
				Person.builder().id(2L).name("Director 1").role(PersonRole.DIRECTOR).build());

		List<PersonResponse> responses = mapper.toPersonResponseList(persons);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).id()).isEqualTo(1L);
		assertThat(responses.get(0).name()).isEqualTo("Actor 1");
		assertThat(responses.get(0).role()).isEqualTo(PersonRole.ACTOR);
		assertThat(responses.get(1).id()).isEqualTo(2L);
		assertThat(responses.get(1).name()).isEqualTo("Director 1");
		assertThat(responses.get(1).role()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void toPersonResponseListFromEmptyList() {
		List<PersonResponse> responses = mapper.toPersonResponseList(Collections.emptyList());
		assertThat(responses).isEmpty();
	}

	@Test
	void toPerson() {
		PersonRequest request = new PersonRequest("New Person", PersonRole.SCREENWRITER);

		Person person = mapper.toPerson(request);

		assertThat(person.getId()).isNull();
		assertThat(person.getName()).isEqualTo("New Person");
		assertThat(person.getRole()).isEqualTo(PersonRole.SCREENWRITER);
	}

	@Test
	void updatePersonFromRequest() {
		Person person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();

		PersonRequest request = new PersonRequest("New Name", PersonRole.DIRECTOR);

		mapper.updatePersonFromRequest(request, person);

		assertThat(person.getId()).isEqualTo(1L);
		assertThat(person.getName()).isEqualTo("New Name");
		assertThat(person.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void updatePersonFromRequestWithNullFields() {
		Person person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();

		PersonRequest request = new PersonRequest(null, null);

		mapper.updatePersonFromRequest(request, person);

		assertThat(person.getId()).isEqualTo(1L);
		assertThat(person.getName()).isEqualTo("Old Name");
		assertThat(person.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void updatePersonFromRequestWithNullRequest() {
		Person person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();

		mapper.updatePersonFromRequest(null, person);

		assertThat(person.getId()).isEqualTo(1L);
		assertThat(person.getName()).isEqualTo("Old Name");
		assertThat(person.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void nullHandling() {
		assertThat(mapper.toPersonResponse((Person) null)).isNull();
		assertThat(mapper.toPersonResponse((ua.lviv.bas.cinema.repository.cinema.projection.PersonProjection) null)).isNull();
		assertThat(mapper.toPerson(null)).isNull();
		assertThat(mapper.toPersonResponseList(null)).isNull();
	}
}