package ua.lviv.bas.cinema.service.cinema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ua.lviv.bas.cinema.domain.cinema.Person;
import ua.lviv.bas.cinema.domain.cinema.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonListResponse;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonHasMoviesException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.mapper.cinema.PersonMapper;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;
import ua.lviv.bas.cinema.repository.cinema.PersonRepository;
import ua.lviv.bas.cinema.repository.cinema.projection.PersonListProjection;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;
    @Mock
    private MovieRepository movieRepository;
    @Mock
    private PersonMapper personMapper;

    @InjectMocks
    private PersonService personService;

    private final Long PERSON_ID = 1L;
    private final String PERSON_NAME = "John Doe";
    private final PersonRole PERSON_ROLE = PersonRole.ACTOR;
    private Person person;
    private PersonResponse personResponse;
    private PersonListResponse listResponse;
    private PersonRequest request;

    @BeforeEach
    void setUp() {
        person = new Person();
        person.setId(PERSON_ID);
        person.setName(PERSON_NAME);
        person.setRole(PERSON_ROLE);

        personResponse = new PersonResponse(PERSON_ID, PERSON_NAME, PERSON_ROLE);
        listResponse = new PersonListResponse(PERSON_ID, PERSON_NAME, PERSON_ROLE, 0);
        request = new PersonRequest(PERSON_NAME, PERSON_ROLE);
    }

    @Test
    void createPersonShouldSucceed() {
        when(personRepository.existsByNameAndRole(PERSON_NAME, PERSON_ROLE)).thenReturn(false);
        when(personMapper.toPerson(request)).thenReturn(person);
        when(personRepository.save(person)).thenReturn(person);
        when(personMapper.toPersonResponse(person)).thenReturn(personResponse);

        PersonResponse result = personService.createPerson(request);

        assertThat(result).isEqualTo(personResponse);
        verify(personRepository).save(person);
    }

    @Test
    void createPersonWithDuplicateShouldThrowException() {
        when(personRepository.existsByNameAndRole(PERSON_NAME, PERSON_ROLE)).thenReturn(true);

        assertThatThrownBy(() -> personService.createPerson(request)).isInstanceOf(DuplicateEntityException.class);

        verify(personRepository, never()).save(any());
    }

    @Test
    void getPersonsShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        PersonListProjection projection = createProjection();
        Page<PersonListProjection> projectionPage = new PageImpl<>(List.of(projection));

        when(personRepository.findPersonsByFilters(PERSON_NAME, PERSON_ROLE, pageable)).thenReturn(projectionPage);
        when(personMapper.toPersonListResponse(projection)).thenReturn(listResponse);

        Page<PersonListResponse> result = personService.getPersons(PERSON_NAME, PERSON_ROLE, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(listResponse);
    }

    @Test
    void getPersonsWithNullQueryShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        PersonListProjection projection = createProjection();
        Page<PersonListProjection> projectionPage = new PageImpl<>(List.of(projection));

        when(personRepository.findPersonsByFilters(null, PERSON_ROLE, pageable)).thenReturn(projectionPage);
        when(personMapper.toPersonListResponse(projection)).thenReturn(listResponse);

        Page<PersonListResponse> result = personService.getPersons(null, PERSON_ROLE, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void updatePersonShouldSucceed() {
        Person existing = new Person();
        existing.setId(PERSON_ID);
        existing.setName("Old Name");
        existing.setRole(PERSON_ROLE);

        when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(existing));
        when(personRepository.existsByNameAndRoleAndIdNot(PERSON_NAME, PERSON_ROLE, PERSON_ID)).thenReturn(false);
        when(personRepository.save(existing)).thenReturn(existing);
        when(personMapper.toPersonResponse(existing)).thenReturn(personResponse);

        PersonResponse result = personService.updatePerson(PERSON_ID, request);

        assertThat(result).isEqualTo(personResponse);
        verify(personMapper).updatePersonFromRequest(request, existing);
    }

    @Test
    void updatePersonWhenNotFoundShouldThrowException() {
        when(personRepository.findById(PERSON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.updatePerson(PERSON_ID, request))
                .isInstanceOf(PersonNotFoundException.class);
    }

    @Test
    void updatePersonWithDuplicateShouldThrowException() {
        when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(person));
        when(personRepository.existsByNameAndRoleAndIdNot(PERSON_NAME, PERSON_ROLE, PERSON_ID)).thenReturn(true);

        assertThatThrownBy(() -> personService.updatePerson(PERSON_ID, request))
                .isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    void deletePersonShouldSucceed() {
        when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(person));
        when(movieRepository.countMovieUsageByPersonId(PERSON_ID)).thenReturn(0L);

        personService.deletePerson(PERSON_ID);

        verify(personRepository).delete(person);
    }

    @Test
    void deletePersonWhenNotFoundShouldThrowException() {
        when(personRepository.findById(PERSON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.deletePerson(PERSON_ID)).isInstanceOf(PersonNotFoundException.class);
    }

    @Test
    void deletePersonWhenHasMoviesShouldThrowException() {
        when(personRepository.findById(PERSON_ID)).thenReturn(Optional.of(person));
        when(movieRepository.countMovieUsageByPersonId(PERSON_ID)).thenReturn(3L);

        assertThatThrownBy(() -> personService.deletePerson(PERSON_ID)).isInstanceOf(PersonHasMoviesException.class);

        verify(personRepository, never()).delete(any());
    }

    private PersonListProjection createProjection() {
        return new PersonListProjection() {
            @Override
            public Long getId() {
                return PERSON_ID;
            }

            @Override
            public String getName() {
                return PERSON_NAME;
            }

            @Override
            public PersonRole getRole() {
                return PERSON_ROLE;
            }

            @Override
            public Integer getMovieCount() {
                return 5;
            }
        };
    }
}