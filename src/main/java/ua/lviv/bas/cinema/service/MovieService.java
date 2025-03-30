package ua.lviv.bas.cinema.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.lviv.bas.cinema.dao.MovieRepository;
import ua.lviv.bas.cinema.domain.Movie;

@Service
public class MovieService {

	@Autowired
	private MovieRepository movieRepository;

	public Movie save(Movie movie) {
		return movieRepository.save(movie);
	}

	public List<Movie> getAllMovies() {
		return movieRepository.findAll();
	}

	public Optional<Movie> findByIdOptional(Integer id) {
		return movieRepository.findById(id);
	}
}
