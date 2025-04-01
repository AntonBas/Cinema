package ua.lviv.bas.cinema.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	public Movie findById(Integer id) {
		return movieRepository.findById(id).get();
	}

	@Transactional
	public void delete(Movie movie) {
		movieRepository.delete(movie);
	}

}
