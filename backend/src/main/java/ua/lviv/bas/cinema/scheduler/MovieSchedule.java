package ua.lviv.bas.cinema.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.repository.MovieRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieSchedule {

	private final MovieRepository movieRepository;

	public void updateMovieStatuses() {
		log.info("Starting automatic movie status update");

		LocalDate today = LocalDate.now();
		List<Movie> allMovies = movieRepository.findAll();

		int updatedCount = 0;

		for (Movie movie : allMovies) {
			try {
				MovieStatus currentStatus = movie.getStatus();
				MovieStatus newStatus = calculateMovieStatus(movie, today);

				if (currentStatus != newStatus) {
					movie.setStatus(newStatus);
					updatedCount++;
				}
			} catch (Exception e) {
				log.error("Failed to update status for movie {}: {}", movie.getId(), e.getMessage(), e);
			}
		}

		if (updatedCount > 0) {
			movieRepository.saveAll(allMovies);
		}

		log.info("Movie status update completed. Updated {} movies", updatedCount);
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
