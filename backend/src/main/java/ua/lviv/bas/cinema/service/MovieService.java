package ua.lviv.bas.cinema.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.dao.GenreRepository;
import ua.lviv.bas.cinema.dao.MovieRepository;
import ua.lviv.bas.cinema.dao.PersonRepository;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.dto.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.MovieDto;
import ua.lviv.bas.cinema.mapper.MovieMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class MovieService {

	private static final Logger logger = LogManager.getLogger(MovieService.class);

	private final MovieRepository movieRepository;
	private final GenreRepository genreRepository;
	private final PersonRepository personRepository;
	private final MovieMapper movieMapper;

	@Value("${app.upload.dir:uploads}")
	private String uploadDir;

	@Transactional
	public MovieDto createMovie(MovieCreateRequest request) {
		logger.info("Creating movie: {}", request.getMovie().getTitle());

		MovieDto movieDto = request.getMovie();
		validateMovieDates(movieDto.getReleaseDate(), movieDto.getEndShowingDate());

		if (movieRepository.findBySlug(movieDto.getSlug()).isPresent()) {
			throw new RuntimeException("Movie with slug '" + movieDto.getSlug() + "' already exists");
		}

		Movie movie = movieMapper.toEntity(request);

		if (request.getPosterFile() != null && !request.getPosterFile().isEmpty()) {
			String fileName = savePosterFile(request.getPosterFile());
			movie.setPosterFileName(fileName);
		}

		setMovieRelations(movie, movieDto);

		Movie savedMovie = movieRepository.save(movie);
		return movieMapper.toDto(savedMovie);
	}

	@Transactional
	public MovieDto updateMovie(Long id, MovieCreateRequest request) {
		logger.info("Updating movie with id: {}", id);

		Movie existingMovie = movieRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

		MovieDto movieDto = request.getMovie();
		validateMovieDates(movieDto.getReleaseDate(), movieDto.getEndShowingDate());

		if (!existingMovie.getSlug().equals(movieDto.getSlug())
				&& movieRepository.findBySlug(movieDto.getSlug()).isPresent()) {
			throw new RuntimeException("Movie with slug '" + movieDto.getSlug() + "' already exists");
		}

		if (request.getPosterFile() != null && !request.getPosterFile().isEmpty()) {
			deletePosterFile(existingMovie.getPosterFileName());
			String fileName = savePosterFile(request.getPosterFile());
			existingMovie.setPosterFileName(fileName);
		}

		movieMapper.updateBasicFields(movieDto, existingMovie);

		updateMovieRelations(existingMovie, movieDto);

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
	public MovieDto getMovieById(Long id) {
		logger.info("Reading movie by id: {}", id);
		return movieRepository.findById(id).map(movieMapper::toDto)
				.orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
	}

	@Transactional(readOnly = true)
	public MovieDto getMovieBySlug(String slug) {
		logger.info("Reading movie by slug: {}", slug);
		return movieRepository.findBySlug(slug).map(movieMapper::toDto)
				.orElseThrow(() -> new RuntimeException("Movie not found with slug: " + slug));
	}

	@Transactional(readOnly = true)
	public List<MovieDto> getAllMovies() {
		logger.info("Retrieving all movies");
		return movieRepository.findAll().stream().map(movieMapper::toDto).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public Page<MovieDto> getPaginatedMovies(Pageable pageable) {
		logger.info("Getting paginated movies - page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
		return movieRepository.findAll(pageable).map(movieMapper::toDto);
	}

	@Transactional(readOnly = true)
	public List<MovieDto> getMoviesByStatus(String status) {
		logger.info("Getting movies by status: {}", status);
		try {
			MovieStatus movieStatus = MovieStatus.valueOf(status.toUpperCase());
			return movieRepository.findByStatus(movieStatus).stream().map(movieMapper::toDto)
					.collect(Collectors.toList());
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("Invalid movie status: " + status);
		}
	}

	@Transactional(readOnly = true)
	public List<MovieDto> getCurrentlyShowingMovies() {
		logger.info("Getting currently showing movies");
		LocalDate now = LocalDate.now();
		return movieRepository.findByReleaseDateBeforeAndEndShowingDateAfter(now, now).stream().map(movieMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<MovieDto> getUpcomingMovies() {
		logger.info("Getting upcoming movies");
		LocalDate now = LocalDate.now();
		return movieRepository.findByReleaseDateAfter(now).stream().map(movieMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<MovieDto> getMoviesByGenre(Long genreId) {
		logger.info("Getting movies by genre id: {}", genreId);

		if (!genreRepository.existsById(genreId)) {
			throw new RuntimeException("Genre not found with id: " + genreId);
		}

		return movieRepository.findByGenresContaining(genreId).stream().map(movieMapper::toDto)
				.collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
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

	private Movie getMovieEntityById(Long id) {
		return movieRepository.findById(id).orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
	}

	private void validateMovieDates(LocalDate releaseDate, LocalDate endShowingDate) {
		if (endShowingDate.isBefore(releaseDate)) {
			throw new RuntimeException("End showing date cannot be before release date");
		}

		if (releaseDate.isAfter(LocalDate.now())) {
			throw new RuntimeException("Release date cannot be in the future");
		}
	}

	private void setMovieRelations(Movie movie, MovieDto movieDto) {
		if (movieDto.getGenreIds() != null && !movieDto.getGenreIds().isEmpty()) {
			Set<Genre> genres = new HashSet<>(genreRepository.findAllById(movieDto.getGenreIds()));
			movie.setGenres(genres);
		}

		if (movieDto.getCastIds() != null && !movieDto.getCastIds().isEmpty()) {
			Set<Person> cast = new HashSet<>(personRepository.findAllById(movieDto.getCastIds()));
			movie.setCast(cast);
		}

		if (movieDto.getDirectorIds() != null && !movieDto.getDirectorIds().isEmpty()) {
			Set<Person> directors = new HashSet<>(personRepository.findAllById(movieDto.getDirectorIds()));
			movie.setDirectors(directors);
		}

		if (movieDto.getScreenwriterIds() != null && !movieDto.getScreenwriterIds().isEmpty()) {
			Set<Person> screenwriters = new HashSet<>(personRepository.findAllById(movieDto.getScreenwriterIds()));
			movie.setScreenwriters(screenwriters);
		}
	}

	private void updateMovieRelations(Movie movie, MovieDto movieDto) {
		movie.getGenres().clear();
		movie.getCast().clear();
		movie.getDirectors().clear();
		movie.getScreenwriters().clear();

		setMovieRelations(movie, movieDto);
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