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
		log.info("=== SCHEDULER START ===");
		log.info("Date now: {}", LocalDate.now());

		LocalDate today = LocalDate.now();
		List<Movie> allMovies = movieRepository.findAll();

		log.info("Found {} movies", allMovies.size());

		int updatedCount = 0;

		for (Movie movie : allMovies) {
			MovieStatus currentStatus = movie.getStatus();
			MovieStatus newStatus = calculateMovieStatus(movie, today);

			log.info("Checking movie {} '{}': release={}, end={}, current={}, calculated={}", movie.getId(),
					movie.getTitle(), movie.getReleaseDate(), movie.getEndShowingDate(), currentStatus, newStatus);

			if (currentStatus != newStatus) {
				movie.setStatus(newStatus);
				updatedCount++;
				log.warn("CHANGING: {} from {} to {}", movie.getTitle(), currentStatus, newStatus);
			}
		}

		if (updatedCount > 0) {
			movieRepository.saveAll(allMovies);
			log.info("SAVED {} CHANGES!", updatedCount);
		} else {
			log.info("No changes needed");
		}

		log.info("=== SCHEDULER END ===");
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