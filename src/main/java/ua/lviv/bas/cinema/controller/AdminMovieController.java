package ua.lviv.bas.cinema.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.service.GenreService;
import ua.lviv.bas.cinema.service.MovieService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminMovieController {

	private final MovieService movieService;
	private final GenreService genreService;
	private final String UPLOAD_DIR = "src/main/resources/static/posters/";

	@GetMapping("/movie")
	public String showAdminMovieList(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "1") int size, Model model) {
		Page<Movie> moviePage = movieService.getPaginatedMovies(page, size);
		model.addAttribute("moviePage", moviePage);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", moviePage.getTotalPages());
		return "admin/movie/movie";
	}

	@GetMapping("/movie/create")
	public String showCreateMovie(Model model) {
		model.addAttribute("allGenres", genreService.getAllGenres());
		model.addAttribute("allAgeRatings", AgeRating.values());
		model.addAttribute("allMovieStatuses", MovieStatus.values());
		model.addAttribute("movie", new Movie());
		return "admin/movie/create-movie";
	}

	@PostMapping("/movie/create")
	public String createMovie(@Valid @ModelAttribute("movie") Movie movie, BindingResult bindingResult,
			@RequestParam("posterFile") MultipartFile file, @RequestParam("genreIds") List<Long> genreIds, Model model)
			throws IOException {
		if (bindingResult.hasErrors()) {
			model.addAttribute("allAgeRatings", AgeRating.values());
			model.addAttribute("allMovieStatuses", MovieStatus.values());
			return "admin/movie/create-movie";
		}

		String posterPath = savePoster(file);
		if (posterPath != null) {
			movie.setPosterImagePath(posterPath);
		}

		List<Genre> selectGenres = genreService.findAllById(genreIds);
		movie.setGenres(selectGenres);

		movieService.createMovie(movie);
		return "redirect:/admin/movie/create";
	}

	@GetMapping("/movie/edit/{slug}")
	public String showEditMovieForm(@PathVariable("slug") String slug, Model model) {
		Movie movie = movieService.readBySlug(slug);
		model.addAttribute("allGenres", genreService.getAllGenres());
		model.addAttribute("allAgeRatings", AgeRating.values());
		model.addAttribute("allMovieStatuses", MovieStatus.values());
		model.addAttribute("movie", movie);
		return "admin/movie/edit-movie";
	}

	@PostMapping("/movie/edit/{slug}")
	public String updateMovie(@PathVariable("slug") String slug, @Valid @ModelAttribute("movie") Movie movie,
			BindingResult bindingResult, @RequestParam(value = "posterFile", required = false) MultipartFile file)
			throws IOException {
		if (bindingResult.hasErrors()) {
			return "/admin/movie/edit-movie";
		}

		Movie existingMovie = movieService.readBySlug(slug);

		existingMovie.setTitle(movie.getTitle());
		existingMovie.setDescription(movie.getDescription());
		existingMovie.setProduction(movie.getProduction());
		existingMovie.setGenres(movie.getGenres());
		existingMovie.setDurationMinutes(movie.getDurationMinutes());
		existingMovie.setDirector(movie.getDirector());
		existingMovie.setReleaseYear(movie.getReleaseYear());
		existingMovie.setReleaseDate(movie.getReleaseDate());
		existingMovie.setEndShowingDate(movie.getEndShowingDate());
		existingMovie.setScreenwriter(movie.getScreenwriter());
		existingMovie.setMainCast(movie.getMainCast());
		existingMovie.setStatus(movie.getStatus());
		existingMovie.setAgeRating(movie.getAgeRating());

		if (file != null && !file.isEmpty()) {
			deleteOldPoster(existingMovie.getPosterImagePath());
			String newPosterPath = savePoster(file);
			if (newPosterPath != null) {
				existingMovie.setPosterImagePath(newPosterPath);
			}
		}
		movieService.createMovie(existingMovie);

		return "redirect:/admin/movie";
	}

	@PostMapping("/movie/delete/{id}")
	public String deleteMovie(@PathVariable Long id) throws IOException {
		Movie movie = movieService.readMovie(id);
		if (movie != null) {
			deleteOldPoster(movie.getPosterImagePath());
			movieService.deleteMovie(id);
		}
		return "redirect:/admin/movie";
	}

	private String savePoster(MultipartFile file) throws IOException {
		if (file.isEmpty()) {
			return null;
		}

		String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
		Path uploadPath = Paths.get(UPLOAD_DIR);

		if (!Files.exists(uploadPath)) {
			Files.createDirectories(uploadPath);
		}

		Files.copy(file.getInputStream(), uploadPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
		return "/posters/" + fileName;
	}

	private void deleteOldPoster(String posterPath) throws IOException {
		if (posterPath != null) {
			Path oldFilePath = Paths.get("src/main/resources/static" + posterPath);
			if (Files.exists(oldFilePath)) {
				Files.delete(oldFilePath);
			}
		}
	}
}