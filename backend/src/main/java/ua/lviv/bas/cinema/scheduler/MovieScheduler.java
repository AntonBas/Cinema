package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;
import ua.lviv.bas.cinema.service.cinema.MovieStatusCalculator;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieScheduler {

	private final MovieRepository movieRepository;
	private final MovieStatusCalculator movieStatusCalculator;

	@Scheduled(cron = "${scheduler.movie-status.cron:0 */5 * * * *}")
	@Transactional
	public void updateMovieStatuses() {
		LocalDate today = LocalDate.now();
		log.info("Starting movie status update for date: {}", today);

		List<Movie> allMovies = movieRepository.findAll();
		log.info("Found {} movies to check", allMovies.size());

		var summary = applyStatusUpdates(allMovies, today);

		if (summary.updatedCount() > 0) {
			movieRepository.saveAll(allMovies);
			log.info("Updated {} movie statuses", summary.updatedCount());
		}

		log.info("Movie status summary - CURRENT: {}, UPCOMING: {}, ARCHIVED: {}", summary.currentCount(),
				summary.upcomingCount(), summary.archivedCount());
		log.info("Movie status update completed");
	}

	private StatusUpdateSummary applyStatusUpdates(List<Movie> movies, LocalDate referenceDate) {
		int updatedCount = 0;
		int currentCount = 0;
		int upcomingCount = 0;
		int archivedCount = 0;

		for (Movie movie : movies) {
			MovieStatus newStatus = movieStatusCalculator.calculate(movie, referenceDate);

			if (updateStatusIfChanged(movie, newStatus)) {
				updatedCount++;
			}

			switch (newStatus) {
			case CURRENT -> currentCount++;
			case UPCOMING -> upcomingCount++;
			case ARCHIVED -> archivedCount++;
			default -> {
			}
			}
		}

		return new StatusUpdateSummary(updatedCount, currentCount, upcomingCount, archivedCount);
	}

	private boolean updateStatusIfChanged(Movie movie, MovieStatus newStatus) {
		MovieStatus currentStatus = movie.getStatus();
		if (currentStatus == newStatus) {
			return false;
		}

		movie.setStatus(newStatus);
		log.info("Movie ID {} '{}': status changed from {} to {}", movie.getId(), movie.getTitle(), currentStatus,
				newStatus);
		return true;
	}

	private record StatusUpdateSummary(int updatedCount, int currentCount, int upcomingCount, int archivedCount) {
	}
}