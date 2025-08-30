package ua.lviv.bas.cinema.service;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.MovieRepository;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.dto.MovieDto;
import ua.lviv.bas.cinema.mapper.MovieMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieService {

	private static final Logger logger = LogManager.getLogger(MovieService.class);

	private final MovieRepository movieRepository;
	private final MovieMapper movieMapper;

	@Transactional
	public MovieDto createMovie(MovieDto movieDto) {
		logger.info("Creating movie: {}", movieDto.getTitle());

		if (movieRepository.findBySlug(movieDto.getSlug()).isPresent()) {
			throw new RuntimeException("Movie with slug '" + movieDto.getSlug() + "' already exists");
		}

		Movie movie = movieMapper.toEntity(movieDto);
		Movie savedMovie = movieRepository.save(movie);
		return movieMapper.toDto(savedMovie);
	}

	@Transactional(readOnly = true)
	public MovieDto getMovieById(Long id) {
		logger.info("Reading movie by id: {}", id);
		return movieRepository.findById(id).map(movieMapper::toDto)
				.orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
	}

	@Transactional
	public MovieDto updateMovie(Long id, MovieDto movieDto) {
		logger.info("Updating movie with id: {}", id);

		Movie existingMovie = movieRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

		if (!existingMovie.getSlug().equals(movieDto.getSlug())
				&& movieRepository.findBySlug(movieDto.getSlug()).isPresent()) {
			throw new RuntimeException("Movie with slug '" + movieDto.getSlug() + "' already exists");
		}

		movieMapper.updateMovieFromDto(movieDto, existingMovie);
		Movie updatedMovie = movieRepository.save(existingMovie);

		return movieMapper.toDto(updatedMovie);
	}

	@Transactional
	public void deleteMovie(Long id) {
		logger.info("Deleting movie by id: {}", id);
		if (!movieRepository.existsById(id)) {
			throw new RuntimeException("Movie not found with id: " + id);
		}
		movieRepository.deleteById(id);
	}

	@Transactional(readOnly = true)
	public List<MovieDto> getAllMovies() {
		logger.info("Retrieving all movies");
		return movieRepository.findAll().stream().map(movieMapper::toDto).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public MovieDto getMovieBySlug(String slug) {
		logger.info("Reading movie by slug: {}", slug);
		return movieRepository.findBySlug(slug).map(movieMapper::toDto)
				.orElseThrow(() -> new RuntimeException("Movie not found with slug: " + slug));
	}

	@Transactional(readOnly = true)
	public Page<MovieDto> getPaginatedMovies(int page, int size) {
		logger.info("Getting paginated movies - page: {}, size: {}", page, size);
		Pageable pageable = PageRequest.of(page, size);
		return movieRepository.findAll(pageable).map(movieMapper::toDto);
	}

	@Transactional(readOnly = true)
	public List<MovieDto> getMoviesByStatus(String status) {
		logger.info("Getting movies by status: {}", status);
		return movieRepository.findAll().stream().filter(movie -> movie.getStatus().name().equalsIgnoreCase(status))
				.map(movieMapper::toDto).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<MovieDto> getCurrentlyShowingMovies() {
		logger.info("Getting currently showing movies");
		return movieRepository.findAll().stream().filter(Movie::isCurrentlyShowing).map(movieMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<MovieDto> getUpcomingMovies() {
		logger.info("Getting upcoming movies");
		return movieRepository.findAll().stream().filter(Movie::isUpcoming).map(movieMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Movie getMovieEntityById(Long id) {
		return movieRepository.findById(id).orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
	}
}