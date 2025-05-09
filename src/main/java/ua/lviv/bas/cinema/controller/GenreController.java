package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.service.GenreService;

@Controller
@RequestMapping("/admin")
public class GenreController {

	@Autowired
	private GenreService genreService;

	@GetMapping("/genres")
	public String listGenres(Model model) {
		List<Genre> genres = genreService.getAllGenres();
		model.addAttribute("genres", genres);
		return "admin/genre/genres";
	}

	@GetMapping("/genre/create")
	public String showCreateGenreForm(Model model) {
		model.addAttribute("genre", new Genre());
		return "admin/genre/create-genre";
	}

	@PostMapping("/genre/create")
	public String addGenre(@ModelAttribute Genre genre) {
		genreService.create(genre);
		return "admin/genre/create-genre";
	}

	@GetMapping("/genre/edit/{id}")
	public String showEditGenreForm(@PathVariable Integer id, Model model) {
		Genre genre = genreService.read(id);
		model.addAttribute("genre", genre);
		return "admin/genre/edit-genre";
	}

	@PostMapping("/genre/edit/{id}")
	public String updateGenre(@PathVariable Integer id, @ModelAttribute Genre genre) {
		genre.setId(id);
		genreService.update(genre);
		return "redirect:/admin/genres";
	}

	@PostMapping("/genre/delete/{id}")
	public String deleteGenre(@PathVariable Integer id) {
		genreService.delete(id);
		return "redirect:/admin/genres";
	}

}
