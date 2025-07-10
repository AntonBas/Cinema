package ua.lviv.bas.cinema.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

	@GetMapping("hall/{id}")
	public String manageSeats(@PathVariable Long id, Model model) {
		CinemaHall hall = hallService.readHall(id);
		List<Seat> seats = seatService.getSeatsByHall(hall);

		int totalRows = seats.stream().mapToInt(Seat::getRowNumber).max().orElse(0);

		model.addAttribute("hall", hall);
		model.addAttribute("seats", seats);
		model.addAttribute("totalRows", totalRows);
		return "admin/cinema-hall/seat";
	}

	@PostMapping("/hall/{id}/generate")
	public String generateSeats(@PathVariable Long id, @RequestParam int rows, @RequestParam int seatsPerRow,
			@RequestParam(defaultValue = "false") boolean vip) {
		CinemaHall hall = hallService.readHall(id);

		for (int row = 1; row <= rows; row++) {
			for (int seat = 1; seat <= seatsPerRow; seat++) {
				Seat newSeat = new Seat();
				newSeat.setHall(hall);
				newSeat.setRowNumber(row);
				newSeat.setSeatNumber(seat);
				newSeat.setVip(vip);
				seatService.createSeat(newSeat);
			}
		}
		return "redirect:/admin/seat/hall/" + id;
	}
}
