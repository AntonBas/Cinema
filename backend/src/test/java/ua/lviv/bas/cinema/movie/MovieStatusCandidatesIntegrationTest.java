package ua.lviv.bas.cinema.movie;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import ua.lviv.bas.cinema.common.CinemaTime;
import ua.lviv.bas.cinema.config.NoOpCacheTestConfig;
import ua.lviv.bas.cinema.config.TestcontainersConfig;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "scheduler.movie-status.cron=-")
@ActiveProfiles("ci")
@Import({TestcontainersConfig.class, NoOpCacheTestConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MovieStatusCandidatesIntegrationTest {

    @Autowired
    private MovieRepository movieRepository;

    @Test
    void shouldReturnOnlyMoviesWhoseStoredStatusDiffersFromDates() {
        LocalDate today = CinemaTime.today();

        Movie currentBeforeRelease = save(MovieStatus.CURRENT, today.plusDays(1), today.plusMonths(1));
        Movie archivedWithExtendedDates = save(MovieStatus.ARCHIVED, today.minusDays(10), today.plusDays(5));
        Movie upcomingReleased = save(MovieStatus.UPCOMING, today, today.plusMonths(1));
        Movie currentEnded = save(MovieStatus.CURRENT, today.minusMonths(1), today.minusDays(1));
        Movie consistentCurrent = save(MovieStatus.CURRENT, today.minusDays(1), today);
        Movie consistentUpcoming = save(MovieStatus.UPCOMING, today.plusDays(3), today.plusMonths(1));
        Movie consistentArchived = save(MovieStatus.ARCHIVED, today.minusMonths(2), today.minusMonths(1));

        List<Long> candidateIds = movieRepository.findCandidatesForStatusUpdate(today).stream()
                .map(Movie::getId).toList();

        assertThat(candidateIds).contains(currentBeforeRelease.getId(), archivedWithExtendedDates.getId(),
                upcomingReleased.getId(), currentEnded.getId());
        assertThat(candidateIds).doesNotContain(consistentCurrent.getId(), consistentUpcoming.getId(),
                consistentArchived.getId());
    }

    private Movie save(MovieStatus status, LocalDate releaseDate, LocalDate endShowingDate) {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return movieRepository.save(Movie.builder().title("Status " + suffix).slug("status-" + suffix)
                .trailerUrl("https://example.com/trailer").description("Status candidate test movie")
                .durationMinutes(120).releaseDate(releaseDate).endShowingDate(endShowingDate).status(status)
                .posterFileName("poster.jpg").ageRating(AgeRating.PEGI_12).build());
    }
}
