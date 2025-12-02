package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;

public class PersonMapperTest {

	private final PersonMapper mapper = Mappers.getMapper(PersonMapper.class);

	@Test
	void toDto_ShouldMapAllFields() {
		Person person = Person.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		PersonResponse dto = mapper.toDto(person);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getName()).isEqualTo("Anton Bas");
		assertThat(dto.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void toDto_WithNull_ShouldReturnNull() {
		assertThat(mapper.toDto(null)).isNull();
	}

	@Test
	void toDtoList_ShouldMapListOfPersons() {
		List<Person> persons = List.of(Person.builder().id(1L).name("Person 1").role(PersonRole.ACTOR).build(),
				Person.builder().id(2L).name("Person 2").role(PersonRole.DIRECTOR).build());

		List<PersonResponse> dtos = mapper.toDtoList(persons);

		assertThat(dtos).isNotNull();
		assertThat(dtos).hasSize(2);
		assertThat(dtos.get(0).getId()).isEqualTo(1L);
		assertThat(dtos.get(0).getName()).isEqualTo("Person 1");
		assertThat(dtos.get(0).getRole()).isEqualTo(PersonRole.ACTOR);
		assertThat(dtos.get(1).getId()).isEqualTo(2L);
		assertThat(dtos.get(1).getName()).isEqualTo("Person 2");
		assertThat(dtos.get(1).getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void toDtoList_WithEmptyList_ShouldReturnEmptyList() {
		List<PersonResponse> dtos = mapper.toDtoList(List.of());

		assertThat(dtos).isNotNull();
		assertThat(dtos).isEmpty();
	}

	@Test
	void toDtoList_WithNull_ShouldReturnNull() {
		assertThat(mapper.toDtoList(null)).isNull();
	}

	@Test
	void toEntity_FromPersonRequest_ShouldIgnoreIdAndMapFields() {
		PersonRequest request = PersonRequest.builder().name("New Person").role(PersonRole.DIRECTOR).build();

		Person person = mapper.toEntity(request);

		assertThat(person).isNotNull();
		assertThat(person.getId()).isNull();
		assertThat(person.getName()).isEqualTo("New Person");
		assertThat(person.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void toEntity_FromPersonRequest_WithNull_ShouldReturnNull() {
		assertThat(mapper.toEntity((PersonRequest) null)).isNull();
	}

	@Test
	void updatePersonFromRequest_ShouldUpdateFields() {
		Person existing = Person.builder().id(10L).name("Old Name").role(PersonRole.ACTOR).build();

		PersonRequest request = PersonRequest.builder().name("Updated Name").role(PersonRole.DIRECTOR).build();

		mapper.updatePersonFromRequest(request, existing);

		assertThat(existing.getId()).isEqualTo(10L);
		assertThat(existing.getName()).isEqualTo("Updated Name");
		assertThat(existing.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void updatePersonFromRequest_WithNullRequest_ShouldNotUpdate() {
		Person existing = Person.builder().id(10L).name("Original Name").role(PersonRole.ACTOR).build();

		mapper.updatePersonFromRequest(null, existing);

		assertThat(existing.getId()).isEqualTo(10L);
		assertThat(existing.getName()).isEqualTo("Original Name");
		assertThat(existing.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void toEntity_FromQuickCreatePersonDto_ShouldMapFieldsAndIgnoreId() {
		QuickCreatePersonRequest quickDto = QuickCreatePersonRequest.builder().name("Quick Person")
				.role(PersonRole.ACTOR).build();

		Person person = mapper.toEntity(quickDto);

		assertThat(person).isNotNull();
		assertThat(person.getId()).isNull();
		assertThat(person.getName()).isEqualTo("Quick Person");
		assertThat(person.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void toEntity_FromQuickCreatePersonDto_WithNull_ShouldReturnNull() {
		assertThat(mapper.toEntity((QuickCreatePersonRequest) null)).isNull();
	}

	@Test
	void toPersonRequest_FromQuickCreatePersonDto_ShouldMapAllFields() {
		QuickCreatePersonRequest quickDto = QuickCreatePersonRequest.builder().name("Quick To Request")
				.role(PersonRole.DIRECTOR).build();

		PersonRequest request = mapper.toPersonRequest(quickDto);

		assertThat(request).isNotNull();
		assertThat(request.getName()).isEqualTo("Quick To Request");
		assertThat(request.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void toPersonRequest_FromQuickCreatePersonDto_WithNull_ShouldReturnNull() {
		assertThat(mapper.toPersonRequest(null)).isNull();
	}
}