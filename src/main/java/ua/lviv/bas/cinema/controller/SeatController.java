package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.service.CinemaHallService;
import ua.lviv.bas.cinema.service.SeatService;

@Controller
@RequiredArgsConstructor
public class SeatController {

	private final CinemaHallService hallService;
	private final SeatService seatService;

	@GetMapping("/{id}/seats")
	public String viewSeats(@PathVariable Long id, Model model) {
		CinemaHall hall = hallService.readHall(id);
		List<Seat> seats = seatService.getSeatsByHall(hall);

		model.addAttribute("hall", hall);
		model.addAttribute("seats", seats);
		return "user/hall-seats";
	}
}
