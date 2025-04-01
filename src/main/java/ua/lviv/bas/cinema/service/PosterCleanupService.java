//package ua.lviv.bas.cinema.service;
//
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.time.LocalDateTime;
//import java.util.List;
//
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//import ua.lviv.bas.cinema.dao.MovieRepository;
//import ua.lviv.bas.cinema.domain.Movie;
//
//@Service
//public class PosterCleanupService {
//
//	private final MovieRepository movieRepository;
//
//	public PosterCleanupService(MovieRepository movieRepository) {
//		this.movieRepository = movieRepository;
//	}
//
//	@Scheduled(fixedRate = 3600000)
//	public void cleanupIncompleteUploads() {
//		List<Movie> incompleteMovies = movieRepository.findByIsPosterUploadedFalse();
//
//		for (Movie movie : incompleteMovies) {
//			if (movie.getCreatedAt().isBefore(LocalDateTime.now().minusHours(1))) {
//				try {
//					// Видаляємо неповні завантаження
//					Files.deleteIfExists(Paths.get("static/posters/movie_" + movie.getId() + ".jpg"));
//					movieRepository.delete(movie);
//				} catch (Exception e) {
//					System.err.println("Помилка при видаленні неповного фільму: " + e.getMessage());
//				}
//			}
//		}
//	}
//}