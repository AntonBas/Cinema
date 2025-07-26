package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.service.GenreService;

@Controller
@RequestMapping("/admin/genre")
@RequiredArgsConstructor
public class GenreController {

	private final GenreService genreService;

	@GetMapping
	public String listGenres(Model model) {
		List<Genre> genres = genreService.getAllGenres();
		model.addAttribute("genres", genres);
		return "admin/genre/genre";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("genre", new Genre());
		return "admin/genre/create-genre";
	}

	@PostMapping("/create")
	public String CreateGenre(@ModelAttribute Genre genre) {
		genreService.createGenre(genre);
		return "admin/genre/create-genre";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		Genre genre = genreService.readGenre(id);
		model.addAttribute("genre", genre);
		return "admin/genre/edit-genre";
	}

	@PostMapping("/edit/{id}")
	public String updateGenre(@PathVariable Long id, @ModelAttribute Genre genre) {
		genre.setId(id);
		genreService.updateGenre(genre);
		return "redirect:/admin/genre";
	}

	@PostMapping("/delete/{id}")
	public String deleteGenre(@PathVariable Long id) {
		genreService.deleteGenre(id);
		return "redirect:/admin/genre";
	}

}
