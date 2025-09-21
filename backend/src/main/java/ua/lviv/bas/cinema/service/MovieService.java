package ua.lviv.bas.cinema.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

	@Value("${app.upload.dir:uploads}")
	private String uploadDir;

	@Transactional
	public MovieDto createMovie(MovieDto movieDto) {
		logger.info("Creating movie: {}", movieDto.getTitle());

		if (movieRepository.findBySlug(movieDto.getSlug()).isPresent()) {
			throw new RuntimeException("Movie with slug '" + movieDto.getSlug() + "' already exists");
		}

		if (movieDto.getPosterFile() != null && !movieDto.getPosterFile().isEmpty()) {
			String fileName = savePosterFile(movieDto.getPosterFile());
			movieDto.setPosterFileName(fileName);
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

		if (movieDto.getPosterFile() != null && !movieDto.getPosterFile().isEmpty()) {
			deletePosterFile(existingMovie.getPosterFileName());

			String fileName = savePosterFile(movieDto.getPosterFile());
			existingMovie.setPosterFileName(fileName);
		}

		movieMapper.updateMovieFromDto(movieDto, existingMovie);
		Movie updatedMovie = movieRepository.save(existingMovie);

		return movieMapper.toDto(updatedMovie);
	}

	@Transactional
	public void deleteMovie(Long id) {
		logger.info("Deleting movie by id: {}", id);
		Movie movie = movieRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

		deletePosterFile(movie.getPosterFileName());

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

	private String savePosterFile(MultipartFile file) {
		try {
			String originalFileName = file.getOriginalFilename();
			String fileExtension = originalFileName != null
					? originalFileName.substring(originalFileName.lastIndexOf("."))
					: ".jpg";
			String fileName = UUID.randomUUID() + fileExtension;

			Path uploadPath = Paths.get(uploadDir, "posters");
			Files.createDirectories(uploadPath);

			Path filePath = uploadPath.resolve(fileName);
			Files.write(filePath, file.getBytes());

			logger.info("Poster file saved: {}", fileName);
			return fileName;

		} catch (IOException e) {
			logger.error("Failed to save poster file", e);
			throw new RuntimeException("Failed to save poster file", e);
		}
	}

	private void deletePosterFile(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return;
		}

		try {
			Path filePath = Paths.get(uploadDir, "posters", fileName);
			if (Files.exists(filePath)) {
				Files.delete(filePath);
				logger.info("Poster file deleted: {}", fileName);
			}
		} catch (IOException e) {
			logger.error("Failed to delete poster file: {}", fileName, e);
		}
	}

	public ResponseEntity<byte[]> getMoviePoster(Long id) {
		try {
			Movie movie = getMovieEntityById(id);

			if (movie.getPosterFileName() == null || movie.getPosterFileName().isEmpty()) {
				return ResponseEntity.notFound().build();
			}

			Path filePath = Paths.get(uploadDir, "posters", movie.getPosterFileName());

			if (!Files.exists(filePath)) {
				return ResponseEntity.notFound().build();
			}

			String contentType = determineContentType(movie.getPosterFileName());

			byte[] imageBytes = Files.readAllBytes(filePath);

			return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
					.header(HttpHeaders.CACHE_CONTROL, "max-age=3600").body(imageBytes);

		} catch (Exception e) {
			logger.error("Error loading poster for movie id: {}", id, e);
			return ResponseEntity.notFound().build();
		}
	}

	private String determineContentType(String fileName) {
		if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
			return "image/jpeg";
		} else if (fileName.toLowerCase().endsWith(".png")) {
			return "image/png";
		} else if (fileName.toLowerCase().endsWith(".gif")) {
			return "image/gif";
		} else if (fileName.toLowerCase().endsWith(".webp")) {
			return "image/webp";
		}
		return "application/octet-stream";
	}
}