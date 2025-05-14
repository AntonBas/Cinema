package ua.lviv.bas.cinema.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.lviv.bas.cinema.dao.MovieRepository;
import ua.lviv.bas.cinema.domain.Movie;

@Service
public class MovieService {

	@Autowired
	private MovieRepository movieRepository;

	public Movie createMovie(Movie movie) {
		return movieRepository.save(movie);
	}

	public Movie readMovie(Long id) {
		return movieRepository.findById(id).orElse(null);
	}

	public Movie updateMovie(Movie movie) {
		return movieRepository.save(movie);
	}

	public void deleteMovie(Long id) {
		movieRepository.deleteById(id);
	}

	public List<Movie> getAllMovies() {
		return movieRepository.findAll();
	}

	public Movie readBySlug(String slug) {
		return movieRepository.findBySlug(slug)
				.orElseThrow(() -> new RuntimeException("Movie not found with slug: " + slug));
	}
}
