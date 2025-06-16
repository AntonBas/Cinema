package ua.lviv.bas.cinema.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.MovieRepository;
import ua.lviv.bas.cinema.domain.Movie;

@Service
@RequiredArgsConstructor
public class MovieService {

	private static final Logger logger = LogManager.getLogger(MovieService.class);
	
	private final MovieRepository movieRepository;

	public Movie createMovie(Movie movie) {
		logger.info("Creating movie: {}", movie.getTitle());
		return movieRepository.save(movie);
	}

	public Movie readMovie(Long id) {
		logger.info("Reading movie by id: {}", id);
		return movieRepository.findById(id).orElse(null);
	}

	public Movie updateMovie(Movie movie) {
		logger.info("Updating movie with id: {}", movie.getId());
		return movieRepository.save(movie);
	}

	public void deleteMovie(Long id) {
		logger.info("Deleting movie by id: {}", id);
		movieRepository.deleteById(id);
	}

	public List<Movie> getAllMovies() {
		logger.info("Retrieving all movies");
		return movieRepository.findAll();
	}

	public Movie readBySlug(String slug) {
		logger.info("Reading movie by slug: {}", slug);
		return movieRepository.findBySlug(slug)
				.orElseThrow(() -> new RuntimeException("Movie not found with slug: " + slug));
	}

	public Page<Movie> getPaginatedMovies(int page, int size) {
		logger.info("Getting paginated movies - page: {}, size: {}", page, size);
		Pageable pageable = PageRequest.of(page, size);
		return movieRepository.findAll(pageable);
	}
}
