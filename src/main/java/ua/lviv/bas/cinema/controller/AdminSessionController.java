package ua.lviv.bas.cinema.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.service.CinemaHallService;
import ua.lviv.bas.cinema.service.MovieService;
import ua.lviv.bas.cinema.service.SessionService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/session")
public class AdminSessionController {

	private final SessionService sessionService;
	private final MovieService movieService;
	private final CinemaHallService hallService;

	@GetMapping
	public String listSessions(Model model) {
		List<Session> sessions = sessionService.getAllSessions();
		model.addAttribute("sessions", sessions);
		model.addAttribute("movies", movieService.getAllMovies());
		model.addAttribute("halls", hallService.getAllHalls());
		return "admin/session/session";
	}

	@GetMapping("/create")
	public String showCreateForm(Model model) {
		model.addAttribute("session", new Session());
		model.addAttribute("movies", movieService.getAllMovies());
		model.addAttribute("halls", hallService.getAllHalls());
		return "admin/session/create-session";
	}

	@PostMapping("/create")
	public String createSession(@ModelAttribute Session session, Model model) {
		Movie movie = movieService.readMovie(session.getMovie().getId());
		session.setMovie(movie);

		LocalDateTime endTime = session.getStartTime().plusMinutes(movie.getDurationMinutes());
		session.setEndTime(endTime);

		boolean isAvailable = sessionService.isSessionTimeAvailable(session.getStartTime(), endTime,
				session.getHall().getId(), null);

		if (!isAvailable) {
			model.addAttribute("errorMessage", "The selected time slot is already booked in this hall.");
			model.addAttribute("movies", movieService.getAllMovies());
			model.addAttribute("halls", hallService.getAllHalls());
			return "admin/session/create-session";
		}

		sessionService.createSession(session);
		return "redirect:/admin/session";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model) {
		Session session = sessionService.readSession(id);
		model.addAttribute("session", session);
		model.addAttribute("movies", movieService.getAllMovies());
		model.addAttribute("halls", hallService.getAllHalls());
		return "admin/session/edit-session";
	}

	@PostMapping("/edit/{id}")
	public String updateSession(@PathVariable Long id, @ModelAttribute Session session) {
		session.setId(id);
		sessionService.updateSession(session);
		return "redirect:/admin/session";
	}

	@PostMapping("/delete/{id}")
	public String deleteSession(@PathVariable Long id) {
		sessionService.deleteSession(id);
		return "redirect:/admin/session";
	}
}
