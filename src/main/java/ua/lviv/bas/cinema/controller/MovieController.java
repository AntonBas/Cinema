package ua.lviv.bas.cinema.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.service.MovieService;

@Controller
public class MovieController {

	@Autowired
	private MovieService movieService;

	private final String UPLOAD_DIR = "src/main/resources/static/posters/";

	@GetMapping("/admin/create-movie")
	public String showCreateMovie(Model model) {
		model.addAttribute("allAgeRattings", AgeRating.values());
		model.addAttribute("allMovieStatuses", MovieStatus.values());
		model.addAttribute("movie", new Movie());
		return "admin/create-movie";
	}

	@PostMapping("/admin/create-movie")
	public String createMovie(@Valid @ModelAttribute("movie") Movie movie, BindingResult bindingResult,
			@RequestParam("posterFile") MultipartFile file) throws IOException {
		if (bindingResult.hasErrors()) {
			return "/admin/create-movie";
		}

		String posterPath = savePoster(file);
		if (posterPath != null) {
			movie.setPosterImagePath(posterPath);
		}

		movieService.save(movie);
		return "redirect:/admin/create-movie";
	}

	@GetMapping("/admin/movies")
	public ModelAndView showAdminMovies() {
		ModelAndView map = new ModelAndView("admin/admin-movie-list");
		map.addObject("movie", movieService.getAllMovies());
		return map;
	}

	@GetMapping("/admin/movie/edit/{id}")
	public String showEditMovieForm(@PathVariable("id") Integer id, Model model) {
		Movie movie = movieService.findById(id);
		model.addAttribute("allAgeRatings", AgeRating.values());
		model.addAttribute("allMovieStatuses", MovieStatus.values());
		model.addAttribute("movie", movie);
		return "admin/edit-movie";
	}

	@PostMapping("/admin/movie/edit/{id}")
	public String updateMovie(@PathVariable("id") Integer id, @Valid @ModelAttribute("movie") Movie movie,
			BindingResult bindingResult, @RequestParam(value = "posterFile", required = false) MultipartFile file)
			throws IOException {
		if (bindingResult.hasErrors()) {
			return "/admin/edit-movie";
		}

		Movie existingMovie = movieService.findById(id);

		existingMovie.setTitle(movie.getTitle());
		existingMovie.setDescription(movie.getDescription());
		existingMovie.setProduction(movie.getProduction());
		existingMovie.setGenre(movie.getGenre());
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
		movieService.save(existingMovie);

		return "redirect:/admin/movies";
	}

	@PostMapping("/admin/movie/delete/{id}")
	public String deleteMovie(@PathVariable("id") Integer id) throws IOException {
		Movie movie = movieService.findById(id);

		if (movie != null) {
			if (movie.getPosterImagePath() != null) {
				Path posterPath = Paths.get("src/main/resources/static" + movie.getPosterImagePath());
				if (Files.exists(posterPath)) {
					Files.delete(posterPath);
				}
			}

			movieService.delete(movie);
		}

		return "redirect:/admin/movies";
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