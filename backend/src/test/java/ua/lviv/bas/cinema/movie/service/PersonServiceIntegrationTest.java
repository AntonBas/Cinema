package ua.lviv.bas.cinema.movie.service;

import java.time.LocalDate;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.Person;
import ua.lviv.bas.cinema.movie.domain.enums.AgeRating;
import ua.lviv.bas.cinema.movie.domain.enums.PersonRole;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.movie.repository.PersonRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PersonServiceIntegrationTest {

    @Autowired
    private PersonService personService;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private MovieRepository movieRepository;

    @Test
    void getPersonsShouldCountEachMovieOnceAcrossActorDirectorScreenwriterRoles() {
        var multiRolePerson = personRepository.save(Person.builder().name("ZZTEST Multi Role Person")
                .role(PersonRole.ACTOR).build());
        var singleRolePerson = personRepository.save(Person.builder().name("ZZTEST Single Role Person")
                .role(PersonRole.ACTOR).build());

        var movie1 = movieRepository.save(buildMovie("zztest-person-movie-1"));
        var movie2 = movieRepository.save(buildMovie("zztest-person-movie-2"));

        movie1.setActors(Set.of(multiRolePerson, singleRolePerson));
        movie1.setDirectors(Set.of(multiRolePerson));
        movieRepository.save(movie1);

        movie2.setActors(Set.of(multiRolePerson));
        movieRepository.save(movie2);

        var page = personService.getPersons("ZZTEST", null, PageRequest.of(0, 10));

        var multiRoleResult = page.getContent().stream().filter(p -> p.id().equals(multiRolePerson.getId()))
                .findFirst().orElseThrow();
        var singleRoleResult = page.getContent().stream().filter(p -> p.id().equals(singleRolePerson.getId()))
                .findFirst().orElseThrow();

        assertThat(multiRoleResult.movieCount()).isEqualTo(2);
        assertThat(singleRoleResult.movieCount()).isEqualTo(1);
    }

    private Movie buildMovie(String slug) {
        return Movie.builder().title("ZZTEST Movie " + slug).slug(slug).trailerUrl("https://example.com/trailer")
                .description("Test movie for person regression test").durationMinutes(120)
                .releaseDate(LocalDate.now().minusDays(1)).endShowingDate(LocalDate.now().plusMonths(1))
                .status(MovieStatus.CURRENT).posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build();
    }
}
