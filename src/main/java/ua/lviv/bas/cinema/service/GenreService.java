package ua.lviv.bas.cinema.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.GenreRepository;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.GenreDto;
import ua.lviv.bas.cinema.mapper.GenreMapper;

@Service
@RequiredArgsConstructor
public class GenreService {

	private static final Logger logger = LogManager.getLogger(GenreService.class);

	private final GenreRepository genreRepository;
	private final GenreMapper genreMapper;

	public GenreDto createGenre(GenreDto genreDto) {
		logger.info("Creating genre: {}", genreDto.getName());
		Genre genre = genreMapper.toEntity(genreDto);
		Genre saved = genreRepository.save(genre);
		return genreMapper.toDto(saved);
	}

	public GenreDto readGenre(Long id) {
		logger.info("Reading genre by id: {}", id);
		return genreRepository.findById(id).map(genreMapper::toDto).orElse(null);
	}

	public GenreDto updateGenre(Long id, GenreDto genreDto) {
	    logger.info("Updating genre with id: {}", id);
	    
	    Genre existingGenre = genreRepository.findById(id)
	        .orElseThrow(() -> new RuntimeException("Genre not found with id: " + id));
	    
	    existingGenre.setName(genreDto.getName());
	    
	    Genre updated = genreRepository.save(existingGenre);
	    return genreMapper.toDto(updated);
	}

	public void deleteGenre(Long id) {
		logger.info("Deleting genre by id: {}", id);
		genreRepository.deleteById(id);
	}

	public List<GenreDto> getAllGenres() {
		logger.info("Retrieving all genres");
		return genreRepository.findAll().stream().map(genreMapper::toDto).collect(Collectors.toList());
	}

	public List<GenreDto> findAllById(List<Long> ids) {
		logger.info("Finding genres by ids: {}", ids);
		return genreRepository.findAllById(ids).stream().map(genreMapper::toDto).collect(Collectors.toList());
	}
}
