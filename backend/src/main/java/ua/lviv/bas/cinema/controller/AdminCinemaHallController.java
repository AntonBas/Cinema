package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.CinemaHallDto;
import ua.lviv.bas.cinema.service.CinemaHallService;

@Controller
@RequestMapping("/admin/hall")
@RequiredArgsConstructor
public class AdminCinemaHallController {

	private final CinemaHallService hallService;

	@GetMapping
	public String listHalls(Model model) {
		model.addAttribute("halls", hallService.getAllHalls());
		return "admin/cinema-hall/hall";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("hallForm", new CinemaHallDto());
		return "admin/cinema-hall/create-hall";
	}

	@PostMapping("/create")
	public String createHallWithSeats(@Valid @ModelAttribute("hallForm") CinemaHallDto form,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "admin/cinema-hall/create-hall";
		}
		hallService.createHallWithSeats(form.getName(), form.getRows(), form.getSeatsPerRow());
		redirectAttributes.addFlashAttribute("success",
				"Cinema hall " + (form.getRows() * form.getSeatsPerRow()) + " and seats successfully created");
		return "redirect:/admin/hall";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		CinemaHall hall = hallService.readHall(id);
		List<Seat> seats = hall.getSeats();

		int maxRow = seats.stream().mapToInt(Seat::getRowNumber).max().orElse(0);

		int seatsInRow = seats.stream().filter(s -> s.getRowNumber() == 1).mapToInt(Seat::getSeatNumber).max()
				.orElse(0);

		CinemaHallDto dto = new CinemaHallDto();
		dto.setId(hall.getId());
		dto.setName(hall.getName());
		dto.setRows(maxRow);
		dto.setSeatsPerRow(seatsInRow);
		model.addAttribute("hallForm", dto);
		return "admin/cinema-hall/edit-hall";
	}

	@PostMapping("/edit/{id}")
	public String updateHall(@PathVariable Long id, @Valid @ModelAttribute("hallForm") CinemaHallDto form,
			BindingResult bindingResult, RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "admin/cinema-hall/edit-hall";
		}

		hallService.updateHallWithSeats(id, form.getName(), form.getRows(), form.getSeatsPerRow());

		redirectAttributes.addFlashAttribute("success", "Cinema hall updated successfully");
		return "redirect:/admin/hall";
	}

	@PostMapping("/delete/{id}")
	public String deleteHall(@PathVariable Long id) {
		hallService.deleteHall(id);
		return "redirect:/admin/hall";
	}

}
