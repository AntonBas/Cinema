package ua.lviv.bas.cinema.mapper.cinema;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.cinema.Person;
import ua.lviv.bas.cinema.domain.cinema.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.PersonProjection;

class PersonMapperTest {

	private final PersonMapper mapper = Mappers.getMapper(PersonMapper.class);

	@Test
	void toPersonResponseFromEntity_ShouldMapAllFields() {
		Person person = Person.builder().id(1L).name("John Doe").role(PersonRole.ACTOR).build();

		PersonResponse response = mapper.toPersonResponse(person);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("John Doe");
		assertThat(response.role()).isEqualTo(PersonRole.ACTOR);
		assertThat(response.movieCount()).isZero();
	}

	@Test
	void toPersonResponseFromProjection_ShouldMapAllFields() {
		PersonProjection projection = new PersonProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getName() {
				return "John Doe";
			}

			@Override
			public PersonRole getRole() {
				return PersonRole.ACTOR;
			}

			@Override
			public Integer getMovieCount() {
				return 15;
			}
		};

		PersonResponse response = mapper.toPersonResponse(projection);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("John Doe");
		assertThat(response.role()).isEqualTo(PersonRole.ACTOR);
		assertThat(response.movieCount()).isEqualTo(15);
	}

	@Test
	void toPersonResponseFromProjection_WithNullMovieCount_ShouldMapNull() {
		PersonProjection projection = new PersonProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getName() {
				return "John Doe";
			}

			@Override
			public PersonRole getRole() {
				return PersonRole.ACTOR;
			}

			@Override
			public Integer getMovieCount() {
				return null;
			}
		};

		PersonResponse response = mapper.toPersonResponse(projection);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("John Doe");
		assertThat(response.role()).isEqualTo(PersonRole.ACTOR);
		assertThat(response.movieCount()).isNull();
	}

	@Test
	void toPerson_FromPersonRequest_ShouldMapRequestToEntity() {
		PersonRequest request = new PersonRequest("New Person", PersonRole.SCREENWRITER);
		Person person = mapper.toPerson(request);

		assertThat(person).isNotNull();
		assertThat(person.getId()).isNull();
		assertThat(person.getName()).isEqualTo("New Person");
		assertThat(person.getRole()).isEqualTo(PersonRole.SCREENWRITER);
	}

	@Test
	void toPerson_FromQuickCreatePersonRequest_ShouldMapRequestToEntity() {
		QuickCreatePersonRequest request = new QuickCreatePersonRequest("Quick Person", PersonRole.DIRECTOR);
		Person person = mapper.toPerson(request);

		assertThat(person).isNotNull();
		assertThat(person.getId()).isNull();
		assertThat(person.getName()).isEqualTo("Quick Person");
		assertThat(person.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void updatePersonFromRequest_ShouldUpdateFields() {
		Person person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();

		PersonRequest request = new PersonRequest("New Name", PersonRole.DIRECTOR);
		mapper.updatePersonFromRequest(request, person);

		assertThat(person.getId()).isEqualTo(1L);
		assertThat(person.getName()).isEqualTo("New Name");
		assertThat(person.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void updatePersonFromRequest_WithNullFields_ShouldIgnoreNull() {
		Person person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();

		PersonRequest request = new PersonRequest(null, null);
		mapper.updatePersonFromRequest(request, person);

		assertThat(person.getId()).isEqualTo(1L);
		assertThat(person.getName()).isEqualTo("Old Name");
		assertThat(person.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void updatePersonFromRequest_WithNullRequest_ShouldNotChange() {
		Person person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();

		mapper.updatePersonFromRequest(null, person);

		assertThat(person.getId()).isEqualTo(1L);
		assertThat(person.getName()).isEqualTo("Old Name");
		assertThat(person.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void toPersonResponse_WithNullEntity_ShouldReturnNull() {
		PersonResponse response = mapper.toPersonResponse((Person) null);
		assertThat(response).isNull();
	}

	@Test
	void toPersonResponse_WithNullProjection_ShouldReturnNull() {
		PersonResponse response = mapper.toPersonResponse((PersonProjection) null);
		assertThat(response).isNull();
	}

	@Test
	void toPerson_WithNullRequest_ShouldReturnNull() {
		Person person = mapper.toPerson((PersonRequest) null);
		assertThat(person).isNull();
	}

	@Test
	void toPerson_WithNullQuickCreateRequest_ShouldReturnNull() {
		Person person = mapper.toPerson((QuickCreatePersonRequest) null);
		assertThat(person).isNull();
	}
}