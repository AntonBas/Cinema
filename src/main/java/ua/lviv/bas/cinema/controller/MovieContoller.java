package ua.lviv.bas.cinema.controller;

import java.io.IOException;
import java.io.InputStream;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;
import ua.lviv.bas.cinema.service.MovieService;

@Controller
public class MovieContoller {

	@Autowired
	private MovieService movieService;

	private final String UPLOAD_DIR = "src/main/resources/static/posters/";

	@RequestMapping(value = "/admin/create-movie", method = RequestMethod.GET)
	public String showCreateMovie(Model model) {
		model.addAttribute("allAgeRattings", AgeRating.values());
		model.addAttribute("allMovieStatuses", MovieStatus.values());
		model.addAttribute("movie", new Movie());
		return "admin/create-movie";
	}

	@RequestMapping(value = "/admin/create-movie", method = RequestMethod.POST)
	public String createMovie(@Valid @ModelAttribute("movie") Movie movie, BindingResult bindingResult,
			@RequestParam("posterFile") MultipartFile file) throws IOException {
		if (bindingResult.hasErrors()) {
			return "admin/movie";
		}

		if (!file.isEmpty()) {
			String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();

			Path uploadPath = Paths.get(UPLOAD_DIR);
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}

			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, Paths.get(UPLOAD_DIR + fileName), StandardCopyOption.REPLACE_EXISTING);
			}

			movie.setPosterImagePath("/posters/" + fileName);
		}

		movieService.save(movie);
		return "redirect:/admin/create-movie";
	}

	@RequestMapping(value = "/admin/movies", method = RequestMethod.GET)
	public ModelAndView showAdminMovies() {
		ModelAndView map = new ModelAndView("admin/admin-movie-list");
		map.addObject("movie", movieService.getAllMovies());
		return map;
	}

	@GetMapping("/admin/movie/edit/{id}")
	public String getMovieDetails(@PathVariable Integer id, Model model) {
		Movie movie = movieService.findById(id);
		model.addAttribute("movie", movie);
		return "admin/movie-details";
	}
}