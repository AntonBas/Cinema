package ua.lviv.bas.cinema.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ua.lviv.bas.cinema.dao.GenreRepository;
import ua.lviv.bas.cinema.domain.Genre;

@Service
public class GenreService {

	@Autowired
	private GenreRepository genreRepository;

	public Genre create(Genre genre) {
		return genreRepository.save(genre);
	}

	public Genre read(Integer id) {
		return genreRepository.findById(id).orElse(null);
	}

	public Genre update(Genre genre) {
		return genreRepository.save(genre);
	}

	public void delete(Integer id) {
		genreRepository.deleteById(id);
	}

	public List<Genre> getAllGenres() {
		return genreRepository.findAll();
	}
}
