package ua.lviv.bas.cinema.mapper.cinema;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.lviv.bas.cinema.domain.cinema.Person;
import ua.lviv.bas.cinema.domain.cinema.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.repository.cinema.projection.PersonListProjection;

import static org.assertj.core.api.Assertions.assertThat;

public class PersonMapperTest {

    private final PersonMapper mapper = Mappers.getMapper(PersonMapper.class);

    @Test
    void toPersonListResponseFromProjection() {
        PersonListProjection projection = new PersonListProjection() {
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

        var response = mapper.toPersonListResponse(projection);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.role()).isEqualTo(PersonRole.ACTOR);
        assertThat(response.movieCount()).isEqualTo(15);
    }

    @Test
    void toPersonListResponseFromProjectionWithNullMovieCount() {
        PersonListProjection projection = new PersonListProjection() {
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

        var response = mapper.toPersonListResponse(projection);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.role()).isEqualTo(PersonRole.ACTOR);
        assertThat(response.movieCount()).isNull();
    }

    @Test
    void toPersonResponse() {
        var person = Person.builder().id(1L).name("John Doe").role(PersonRole.ACTOR).build();
        var response = mapper.toPersonResponse(person);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.role()).isEqualTo(PersonRole.ACTOR);
    }

    @Test
    void toPersonFromRequest() {
        var request = new PersonRequest("New Person", PersonRole.SCREENWRITER);
        var person = mapper.toPerson(request);

        assertThat(person).isNotNull();
        assertThat(person.getId()).isNull();
        assertThat(person.getName()).isEqualTo("New Person");
        assertThat(person.getRole()).isEqualTo(PersonRole.SCREENWRITER);
    }

    @Test
    void updatePersonFromRequest() {
        var person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();
        var request = new PersonRequest("New Name", PersonRole.DIRECTOR);
        mapper.updatePersonFromRequest(request, person);

        assertThat(person.getId()).isEqualTo(1L);
        assertThat(person.getName()).isEqualTo("New Name");
        assertThat(person.getRole()).isEqualTo(PersonRole.DIRECTOR);
    }

    @Test
    void updatePersonFromRequestWithNullFields() {
        var person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();
        var request = new PersonRequest(null, null);
        mapper.updatePersonFromRequest(request, person);

        assertThat(person.getId()).isEqualTo(1L);
        assertThat(person.getName()).isEqualTo("Old Name");
        assertThat(person.getRole()).isEqualTo(PersonRole.ACTOR);
    }

    @Test
    void updatePersonFromRequestWithNullRequest() {
        var person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();
        mapper.updatePersonFromRequest(null, person);

        assertThat(person.getId()).isEqualTo(1L);
        assertThat(person.getName()).isEqualTo("Old Name");
        assertThat(person.getRole()).isEqualTo(PersonRole.ACTOR);
    }

    @Test
    void toPersonListResponseWithNullProjection() {
        var response = mapper.toPersonListResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toPersonResponseWithNullEntity() {
        var response = mapper.toPersonResponse(null);
        assertThat(response).isNull();
    }

    @Test
    void toPersonWithNullRequest() {
        var person = mapper.toPerson(null);
        assertThat(person).isNull();
    }
}