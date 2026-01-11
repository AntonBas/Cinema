package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;

public class PersonMapperTest {

	private PersonMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(PersonMapper.class);
	}

	@Test
	void toPersonResponse_ShouldMapAllFields() {
		Person person = Person.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		PersonResponse response = mapper.toPersonResponse(person);

		assertThat(response).isNotNull()
				.extracting(PersonResponse::getId, PersonResponse::getName, PersonResponse::getRole)
				.containsExactly(1L, "Anton Bas", PersonRole.ACTOR);
	}

	@Test
	void toPersonResponse_ShouldReturnNull_WhenInputIsNull() {
		PersonResponse response = mapper.toPersonResponse(null);

		assertThat(response).isNull();
	}

	@Test
	void toPersonResponse_ShouldMapPersonWithoutBuilder() {
		Person person = new Person();
		person.setId(2L);
		person.setName("John Doe");
		person.setRole(PersonRole.DIRECTOR);

		PersonResponse response = mapper.toPersonResponse(person);

		assertThat(response).extracting(PersonResponse::getId, PersonResponse::getName, PersonResponse::getRole)
				.containsExactly(2L, "John Doe", PersonRole.DIRECTOR);
	}

	@Test
	void toPersonResponse_ShouldHandleSpecialCharactersInName() {
		Person person = Person.builder().id(3L).name("O'Connor, John Jr.").role(PersonRole.SCREENWRITER).build();

		PersonResponse response = mapper.toPersonResponse(person);

		assertThat(response.getName()).isEqualTo("O'Connor, John Jr.");
	}

	@ParameterizedTest
	@EnumSource(PersonRole.class)
	void toPersonResponse_ShouldMapAllPersonRoles(PersonRole role) {
		Person person = Person.builder().id(4L).name("Test Person").role(role).build();

		PersonResponse response = mapper.toPersonResponse(person);

		assertThat(response.getRole()).isEqualTo(role);
	}

	@Test
	void toPersonResponseList_ShouldMapListOfPersons() {
		List<Person> persons = Arrays.asList(Person.builder().id(1L).name("Person 1").role(PersonRole.ACTOR).build(),
				Person.builder().id(2L).name("Person 2").role(PersonRole.DIRECTOR).build(),
				Person.builder().id(3L).name("Person 3").role(PersonRole.SCREENWRITER).build());

		List<PersonResponse> responses = mapper.toPersonResponseList(persons);

		assertThat(responses).isNotNull().hasSize(3).extracting(PersonResponse::getName).containsExactly("Person 1",
				"Person 2", "Person 3");
	}

	@Test
	void toPersonResponseList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<PersonResponse> responses = mapper.toPersonResponseList(Collections.emptyList());

		assertThat(responses).isNotNull().isEmpty();
	}

	@Test
	void toPersonResponseList_ShouldReturnNull_WhenInputIsNull() {
		List<PersonResponse> responses = mapper.toPersonResponseList(null);

		assertThat(responses).isNull();
	}

	@Test
	void toPersonResponseList_ShouldHandleListWithNullElements() {
		List<Person> persons = Arrays.asList(Person.builder().id(1L).name("Person 1").build(), null,
				Person.builder().id(2L).name("Person 2").build());

		List<PersonResponse> responses = mapper.toPersonResponseList(persons);

		assertThat(responses).hasSize(3);
		assertThat(responses.get(0)).isNotNull();
		assertThat(responses.get(1)).isNull();
		assertThat(responses.get(2)).isNotNull();
	}

	@Test
	void toPerson_ShouldIgnoreIdAndMapFields() {
		PersonRequest request = PersonRequest.builder().name("New Person").role(PersonRole.DIRECTOR).build();

		Person person = mapper.toPerson(request);

		assertThat(person).isNotNull().extracting(Person::getId, Person::getName, Person::getRole).containsExactly(null,
				"New Person", PersonRole.DIRECTOR);
	}

	@Test
	void toPerson_ShouldReturnNull_WhenInputIsNull() {
		Person person = mapper.toPerson(null);

		assertThat(person).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "\t", "\n" })
	void toPerson_ShouldHandleEmptyOrBlankName(String name) {
		PersonRequest request = PersonRequest.builder().name(name).role(PersonRole.ACTOR).build();

		Person person = mapper.toPerson(request);

		assertThat(person.getName()).isEqualTo(name);
	}

	@Test
	void updatePersonFromRequest_ShouldUpdateNameAndRole() {
		Person existing = Person.builder().id(10L).name("Old Name").role(PersonRole.ACTOR).build();

		PersonRequest request = PersonRequest.builder().name("Updated Name").role(PersonRole.DIRECTOR).build();

		mapper.updatePersonFromRequest(request, existing);

		assertThat(existing).extracting(Person::getId, Person::getName, Person::getRole).containsExactly(10L,
				"Updated Name", PersonRole.DIRECTOR);
	}

	@Test
	void updatePersonFromRequest_ShouldNotUpdate_WhenRequestIsNull() {
		Person existing = Person.builder().id(10L).name("Original Name").role(PersonRole.ACTOR).build();

		mapper.updatePersonFromRequest(null, existing);

		assertThat(existing).extracting(Person::getId, Person::getName, Person::getRole).containsExactly(10L,
				"Original Name", PersonRole.ACTOR);
	}

	@Test
	void updatePersonFromRequest_ShouldHandleEmptyName() {
		Person existing = Person.builder().id(1L).name("Original Name").role(PersonRole.ACTOR).build();

		PersonRequest request = PersonRequest.builder().name("").role(PersonRole.DIRECTOR).build();

		mapper.updatePersonFromRequest(request, existing);

		assertThat(existing.getName()).isEmpty();
		assertThat(existing.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void updatePersonFromRequest_ShouldNotUpdateId() {
		Person existing = Person.builder().id(999L).name("Test").role(PersonRole.ACTOR).build();

		PersonRequest request = PersonRequest.builder().name("Updated").role(PersonRole.DIRECTOR).build();

		mapper.updatePersonFromRequest(request, existing);

		assertThat(existing.getId()).isEqualTo(999L);
	}

	@Test
	void updatePersonFromRequest_ShouldThrowException_WhenTargetIsNull() {
		PersonRequest request = PersonRequest.builder().name("Test").role(PersonRole.ACTOR).build();

		assertThatThrownBy(() -> mapper.updatePersonFromRequest(request, null))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void consistencyCheck_ToPersonAndToPersonResponse_ShouldReturnSameValues() {
		PersonRequest request = PersonRequest.builder().name("Consistency Test").role(PersonRole.SCREENWRITER).build();

		Person entity = mapper.toPerson(request);
		PersonResponse response = mapper.toPersonResponse(entity);

		assertThat(response.getName()).isEqualTo("Consistency Test");
		assertThat(response.getRole()).isEqualTo(PersonRole.SCREENWRITER);
	}

	@Test
	void updatePersonFromRequestThenToPersonResponse_ShouldReflectChanges() {
		Person person = Person.builder().id(1L).name("Before").role(PersonRole.ACTOR).build();

		PersonRequest update = PersonRequest.builder().name("After").role(PersonRole.DIRECTOR).build();

		mapper.updatePersonFromRequest(update, person);
		PersonResponse response = mapper.toPersonResponse(person);

		assertThat(response.getName()).isEqualTo("After");
		assertThat(response.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void toPersonResponse_ShouldMapMaxLengthName() {
		String longName = "A".repeat(50);
		Person person = Person.builder().id(1L).name(longName).role(PersonRole.ACTOR).build();

		PersonResponse response = mapper.toPersonResponse(person);

		assertThat(response.getName()).hasSize(50);
	}

	@Test
	void toPersonResponse_ShouldHandlePersonWithNullRole() {
		Person person = Person.builder().id(1L).name("Test Person").role(null).build();

		PersonResponse response = mapper.toPersonResponse(person);

		assertThat(response.getRole()).isNull();
	}
}