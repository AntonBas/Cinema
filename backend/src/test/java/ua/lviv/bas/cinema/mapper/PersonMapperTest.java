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
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;

public class PersonMapperTest {

	private PersonMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(PersonMapper.class);
	}

	@Test
	void toDto_ShouldMapAllFields() {
		Person person = Person.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		PersonResponse dto = mapper.toDto(person);

		assertThat(dto).isNotNull().extracting(PersonResponse::getId, PersonResponse::getName, PersonResponse::getRole)
				.containsExactly(1L, "Anton Bas", PersonRole.ACTOR);
	}

	@Test
	void toDto_ShouldReturnNull_WhenInputIsNull() {
		PersonResponse dto = mapper.toDto(null);

		assertThat(dto).isNull();
	}

	@Test
	void toDto_ShouldMapPersonWithoutBuilder() {
		Person person = new Person();
		person.setId(2L);
		person.setName("John Doe");
		person.setRole(PersonRole.DIRECTOR);

		PersonResponse dto = mapper.toDto(person);

		assertThat(dto).extracting(PersonResponse::getId, PersonResponse::getName, PersonResponse::getRole)
				.containsExactly(2L, "John Doe", PersonRole.DIRECTOR);
	}

	@Test
	void toDto_ShouldHandleSpecialCharactersInName() {
		Person person = Person.builder().id(3L).name("O'Connor, John Jr.").role(PersonRole.SCREENWRITER).build();

		PersonResponse dto = mapper.toDto(person);

		assertThat(dto.getName()).isEqualTo("O'Connor, John Jr.");
	}

	@ParameterizedTest
	@EnumSource(PersonRole.class)
	void toDto_ShouldMapAllPersonRoles(PersonRole role) {
		Person person = Person.builder().id(4L).name("Test Person").role(role).build();

		PersonResponse dto = mapper.toDto(person);

		assertThat(dto.getRole()).isEqualTo(role);
	}

	@Test
	void toDtoList_ShouldMapListOfPersons() {
		List<Person> persons = Arrays.asList(Person.builder().id(1L).name("Person 1").role(PersonRole.ACTOR).build(),
				Person.builder().id(2L).name("Person 2").role(PersonRole.DIRECTOR).build(),
				Person.builder().id(3L).name("Person 3").role(PersonRole.SCREENWRITER).build());

		List<PersonResponse> dtos = mapper.toDtoList(persons);

		assertThat(dtos).isNotNull().hasSize(3).extracting(PersonResponse::getName).containsExactly("Person 1",
				"Person 2", "Person 3");
	}

	@Test
	void toDtoList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<PersonResponse> dtos = mapper.toDtoList(Collections.emptyList());

		assertThat(dtos).isNotNull().isEmpty();
	}

	@Test
	void toDtoList_ShouldReturnNull_WhenInputIsNull() {
		List<PersonResponse> dtos = mapper.toDtoList(null);

		assertThat(dtos).isNull();
	}

	@Test
	void toDtoList_ShouldHandleListWithNullElements() {
		List<Person> persons = Arrays.asList(Person.builder().id(1L).name("Person 1").build(), null,
				Person.builder().id(2L).name("Person 2").build());

		List<PersonResponse> dtos = mapper.toDtoList(persons);

		assertThat(dtos).hasSize(3);

		assertThat(dtos.get(0)).isNotNull();
		assertThat(dtos.get(1)).isNull();
		assertThat(dtos.get(2)).isNotNull();
	}

	@Test
	void toEntity_FromPersonRequest_ShouldIgnoreIdAndMapFields() {
		PersonRequest request = PersonRequest.builder().name("New Person").role(PersonRole.DIRECTOR).build();

		Person person = mapper.toEntity(request);

		assertThat(person).isNotNull().extracting(Person::getId, Person::getName, Person::getRole).containsExactly(null,
				"New Person", PersonRole.DIRECTOR);
	}

	@Test
	void toEntity_FromPersonRequest_ShouldReturnNull_WhenInputIsNull() {
		Person person = mapper.toEntity((PersonRequest) null);

		assertThat(person).isNull();
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "\t", "\n" })
	void toEntity_FromPersonRequest_ShouldHandleEmptyOrBlankName(String name) {
		PersonRequest request = PersonRequest.builder().name(name).role(PersonRole.ACTOR).build();

		Person person = mapper.toEntity(request);

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
	void toEntity_FromQuickCreatePersonRequest_ShouldMapFieldsAndIgnoreId() {
		QuickCreatePersonRequest quickDto = QuickCreatePersonRequest.builder().name("Quick Person")
				.role(PersonRole.ACTOR).build();

		Person person = mapper.toEntity(quickDto);

		assertThat(person).isNotNull().extracting(Person::getId, Person::getName, Person::getRole).containsExactly(null,
				"Quick Person", PersonRole.ACTOR);
	}

	@Test
	void toEntity_FromQuickCreatePersonRequest_ShouldReturnNull_WhenInputIsNull() {
		Person person = mapper.toEntity((QuickCreatePersonRequest) null);

		assertThat(person).isNull();
	}

	@Test
	void toEntity_FromQuickCreatePersonRequest_ShouldHandleSameValuesAsPersonRequest() {
		QuickCreatePersonRequest quickDto = QuickCreatePersonRequest.builder().name("Same Person")
				.role(PersonRole.DIRECTOR).build();

		PersonRequest regularRequest = PersonRequest.builder().name("Same Person").role(PersonRole.DIRECTOR).build();

		Person fromQuick = mapper.toEntity(quickDto);
		Person fromRegular = mapper.toEntity(regularRequest);

		assertThat(fromQuick.getName()).isEqualTo(fromRegular.getName());
		assertThat(fromQuick.getRole()).isEqualTo(fromRegular.getRole());
	}

	@Test
	void toPersonRequest_FromQuickCreatePersonRequest_ShouldMapAllFields() {
		QuickCreatePersonRequest quickDto = QuickCreatePersonRequest.builder().name("Quick To Request")
				.role(PersonRole.DIRECTOR).build();

		PersonRequest request = mapper.toPersonRequest(quickDto);

		assertThat(request).isNotNull().extracting(PersonRequest::getName, PersonRequest::getRole)
				.containsExactly("Quick To Request", PersonRole.DIRECTOR);
	}

	@Test
	void toPersonRequest_FromQuickCreatePersonRequest_ShouldReturnNull_WhenInputIsNull() {
		PersonRequest request = mapper.toPersonRequest(null);

		assertThat(request).isNull();
	}

	@Test
	void consistencyCheck_ToEntityAndToDto_ShouldReturnSameValues() {
		PersonRequest request = PersonRequest.builder().name("Consistency Test").role(PersonRole.SCREENWRITER).build();

		Person entity = mapper.toEntity(request);
		PersonResponse dto = mapper.toDto(entity);

		assertThat(dto.getName()).isEqualTo("Consistency Test");
		assertThat(dto.getRole()).isEqualTo(PersonRole.SCREENWRITER);
	}

	@Test
	void consistencyCheck_QuickCreateToEntityToDto() {
		QuickCreatePersonRequest quickDto = QuickCreatePersonRequest.builder().name("Quick Test").role(PersonRole.ACTOR)
				.build();

		Person entity = mapper.toEntity(quickDto);
		PersonResponse dto = mapper.toDto(entity);

		assertThat(dto.getName()).isEqualTo("Quick Test");
		assertThat(dto.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void updateThenToDto_ShouldReflectChanges() {
		Person person = Person.builder().id(1L).name("Before").role(PersonRole.ACTOR).build();

		PersonRequest update = PersonRequest.builder().name("After").role(PersonRole.DIRECTOR).build();

		mapper.updatePersonFromRequest(update, person);
		PersonResponse dto = mapper.toDto(person);

		assertThat(dto.getName()).isEqualTo("After");
		assertThat(dto.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}
}