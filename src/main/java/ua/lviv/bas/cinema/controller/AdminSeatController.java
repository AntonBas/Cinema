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
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.service.CinemaHallService;
import ua.lviv.bas.cinema.service.SeatService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/seat")
public class AdminSeatController {

	private final CinemaHallService hallService;
	private final SeatService seatService;

	@GetMapping("/hall/{id}")
	public String manageSeats(@PathVariable Long id, Model model) {
		CinemaHall hall = hallService.readHall(id);
		List<Seat> seats = seatService.getSeatsByHall(hall);

		int totalRows = seats.stream().mapToInt(Seat::getRowNumber).max().orElse(0);

		model.addAttribute("hall", hall);
		model.addAttribute("seats", seats);
		model.addAttribute("totalRows", totalRows);
		return "admin/cinema-hall/seat";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		Seat seat = seatService.readSeat(id);
		model.addAttribute("seat", seat);
		return "admin/cinema-hall/edit-seat";
	}

	@PostMapping("/edit/{id}")
	public String updateSeat(@PathVariable Long id, @ModelAttribute Seat seat) {
		Seat existingSeat = seatService.readSeat(id);

		seat.setHall(existingSeat.getHall());
		seat.setId(id);
		Long hallId = seat.getHall().getId();
		seatService.updateSeat(seat);
		return "redirect:/admin/seat/hall/" + hallId;
	}

	@PostMapping("/delete/{id}")
	public String deleteSeat(@PathVariable Long id) {
		Seat seat = seatService.readSeat(id);
		Long hallId = seat.getHall().getId();
		seatService.deleteSeat(id);
		return "redirect:/admin/seat/hall/" + hallId;
	}
}
