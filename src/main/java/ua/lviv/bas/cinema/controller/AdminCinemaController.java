package ua.lviv.bas.cinema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminCinemaController {

	@GetMapping("/dashboard")
	public String adminDashboard() {
		return "admin/admin-dashboard";
	}
}
