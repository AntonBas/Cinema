package ua.lviv.bas.cinema.movie.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ua.lviv.bas.cinema.movie.domain.Person;
import ua.lviv.bas.cinema.movie.domain.enums.PersonRole;
import ua.lviv.bas.cinema.movie.dto.request.PersonRequest;
import ua.lviv.bas.cinema.movie.repository.projection.PersonListProjection;

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
    void toEntityFromRequest() {
        var request = new PersonRequest("New Person", PersonRole.SCREENWRITER);
        var person = mapper.toEntity(request);

        assertThat(person).isNotNull();
        assertThat(person.getId()).isNull();
        assertThat(person.getName()).isEqualTo("New Person");
        assertThat(person.getRole()).isEqualTo(PersonRole.SCREENWRITER);
    }

    @Test
    void updateEntity() {
        var person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();
        var request = new PersonRequest("New Name", PersonRole.DIRECTOR);
        mapper.updateEntity(request, person);

        assertThat(person.getId()).isEqualTo(1L);
        assertThat(person.getName()).isEqualTo("New Name");
        assertThat(person.getRole()).isEqualTo(PersonRole.DIRECTOR);
    }

    @Test
    void updateEntityWithNullFields() {
        var person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();
        var request = new PersonRequest(null, null);
        mapper.updateEntity(request, person);

        assertThat(person.getId()).isEqualTo(1L);
        assertThat(person.getName()).isEqualTo("Old Name");
        assertThat(person.getRole()).isEqualTo(PersonRole.ACTOR);
    }

    @Test
    void updateEntityWithNullRequest() {
        var person = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();
        mapper.updateEntity(null, person);

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
    void toEntityWithNullRequest() {
        var person = mapper.toEntity(null);
        assertThat(person).isNull();
    }
}