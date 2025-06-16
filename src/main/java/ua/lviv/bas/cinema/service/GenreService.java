package ua.lviv.bas.cinema.service;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.GenreRepository;
import ua.lviv.bas.cinema.domain.Genre;

@Service
@RequiredArgsConstructor
public class GenreService {

	private static final Logger logger = LogManager.getLogger(GenreService.class);

	private final GenreRepository genreRepository;

	public Genre createGenre(Genre genre) {
		logger.info("Creating genre: {}", genre.getName());
		return genreRepository.save(genre);
	}

	public Genre readGenre(Long id) {
		logger.info("Reading genre by id: {}", id);
		return genreRepository.findById(id).orElse(null);
	}

	public Genre updateGenre(Genre genre) {
		logger.info("Updating genre with id: {}", genre.getId());
		return genreRepository.save(genre);
	}

	public void deleteGenre(Long id) {
		logger.info("Deleting genre by id: {}", id);
		genreRepository.deleteById(id);
	}

	public List<Genre> getAllGenres() {
		logger.info("Retrieving all genres");
		return genreRepository.findAll();
	}

	public List<Genre> findAllById(List<Long> ids) {
		logger.info("Finding genres by ids: {}", ids);
		return genreRepository.findAllById(ids);
	}
}
