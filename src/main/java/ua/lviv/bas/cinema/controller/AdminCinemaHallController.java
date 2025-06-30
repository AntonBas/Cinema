package ua.lviv.bas.cinema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.service.CinemaHallService;

@Controller
@RequestMapping("/admin/cinema-hall")
@RequiredArgsConstructor
public class AdminCinemaHallController {

	private final CinemaHallService hallService;

	@GetMapping
	public String showHallList(Model model) {
		model.addAttribute("halls", hallService.getAllHalls());
		return "admin/cinema-hall/hall";
	}

	@GetMapping("/create")
	public String showCreateHall(Model model) {
		model.addAttribute("hall", new CinemaHall());
		return "admin/cinema-hall/create-hall";
	}

	@PostMapping("/create")
	public String createHall(@Valid @ModelAttribute("hall") CinemaHall hall, BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			return "admin/cinema-hall/create-hall";
		}
		hallService.createHall(hall);
		return "redirect:/admin/cinema-hall";
	}

	@GetMapping("/edit/{id}")
	public String showEditHall(@PathVariable Long id, Model model) {
		CinemaHall hall = hallService.readHall(id);
		if (hall == null) {
			return "redirect:/admin/cinema-hall";
		}
		model.addAttribute("hall", hall);
		return "admin/cinema-hall/edit-hall";
	}

	@PostMapping("/edit")
	public String editHall(@Valid @ModelAttribute("hall") CinemaHall hall, BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return "admin/cinema-hall/edit-hall";
		}
		hallService.updateHall(hall);
		return "redirect:/admin/cinema-hall";
	}

	@PostMapping("/delete/{id}")
	public String deleteHall(@PathVariable Long id) {
		hallService.deleteHall(id);
		return "redirect:/admin/cinema-hall";
	}

}
