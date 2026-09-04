package ua.lviv.bas.cinema.movie.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.movie.domain.Movie;
import ua.lviv.bas.cinema.movie.domain.status.MovieStatus;
import ua.lviv.bas.cinema.movie.repository.MovieRepository;
import ua.lviv.bas.cinema.movie.service.MovieStatusCalculator;

@Slf4j
@Component
public class MovieScheduler {

	private final MovieRepository movieRepository;
	private final MovieStatusCalculator movieStatusCalculator;
	private final TransactionTemplate transactionTemplate;

	public MovieScheduler(MovieRepository movieRepository, MovieStatusCalculator movieStatusCalculator,
			PlatformTransactionManager transactionManager) {
		this.movieRepository = movieRepository;
		this.movieStatusCalculator = movieStatusCalculator;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
	}

	@Scheduled(cron = "${scheduler.movie-status.cron:0 */5 * * * *}")
	@CacheEvict(value = {"singleMovies", "movieLists"}, allEntries = true)
	public void updateMovieStatuses() {
		LocalDate today = LocalDate.now();
		log.info("Starting movie status update for date: {}", today);

		List<Movie> candidates = movieRepository.findCandidatesForStatusUpdate(today);
		log.info("Found {} movies to check", candidates.size());

		var summary = applyStatusUpdates(candidates, today);

		log.info("Updated {} movie statuses", summary.updatedCount());
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

			if (movie.getStatus() != newStatus && updateStatus(movie.getId(), newStatus)) {
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

	private boolean updateStatus(Long movieId, MovieStatus newStatus) {
		try {
			transactionTemplate.executeWithoutResult(status -> movieRepository.findById(movieId).ifPresent(movie -> {
				MovieStatus previousStatus = movie.getStatus();
				movie.setStatus(newStatus);
				movieRepository.save(movie);
				log.info("Movie ID {} '{}': status changed from {} to {}", movie.getId(), movie.getTitle(),
						previousStatus, newStatus);
			}));
			return true;
		} catch (ObjectOptimisticLockingFailureException e) {
			log.warn("Skipped updating movie {} to {} due to concurrent update, will retry on next run", movieId,
					newStatus);
			return false;
		}
	}

	private record StatusUpdateSummary(int updatedCount, int currentCount, int upcomingCount, int archivedCount) {
	}
}
