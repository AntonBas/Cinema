package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.repository.MovieRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieScheduler {

	private final MovieRepository movieRepository;

	@Scheduled(cron = "${scheduler.movie-status.cron:0 */5 * * * *}")
	@Transactional
	public void updateMovieStatuses() {
		LocalDate today = LocalDate.now();
		log.info("Starting movie status update for date: {}", today);

		List<Movie> allMovies = movieRepository.findAll();
		log.info("Found {} movies to check", allMovies.size());

		int updatedCount = 0;
		int currentCount = 0;
		int upcomingCount = 0;
		int archivedCount = 0;

		for (Movie movie : allMovies) {
			MovieStatus currentStatus = movie.getStatus();
			MovieStatus newStatus = calculateMovieStatus(movie, today);

			if (currentStatus != newStatus) {
				movie.setStatus(newStatus);
				updatedCount++;
				log.info("Movie ID {} '{}': status changed from {} to {}", movie.getId(), movie.getTitle(),
						currentStatus, newStatus);
			}

			switch (newStatus) {
			case CURRENT -> currentCount++;
			case UPCOMING -> upcomingCount++;
			case ARCHIVED -> archivedCount++;
			default -> {
			}
			}
		}

		if (updatedCount > 0) {
			movieRepository.saveAll(allMovies);
			log.info("Updated {} movie statuses", updatedCount);
		}

		log.info("Movie status summary - CURRENT: {}, UPCOMING: {}, ARCHIVED: {}", currentCount, upcomingCount,
				archivedCount);
		log.info("Movie status update completed");
	}

	public MovieStatus calculateMovieStatus(Movie movie, LocalDate referenceDate) {
		if (movie == null || movie.getReleaseDate() == null) {
			return MovieStatus.UNKNOWN;
		}

		if (referenceDate.isBefore(movie.getReleaseDate())) {
			return MovieStatus.UPCOMING;
		} else if (movie.getEndShowingDate() != null && referenceDate.isAfter(movie.getEndShowingDate())) {
			return MovieStatus.ARCHIVED;
		} else {
			return MovieStatus.CURRENT;
		}
	}
}