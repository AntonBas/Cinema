package ua.lviv.bas.cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import ua.lviv.bas.cinema.service.UserService;

@Controller
public class UserController {

	private final UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/account")
	public String myAccount(Model model, Authentication authentication) {
		if (authentication != null && authentication.isAuthenticated()) {
			String email = authentication.getName();
			userService.findByEmail(email).ifPresent(user -> {
				model.addAttribute("firstName", user.getFirstName());
				model.addAttribute("lastName", user.getLastName());
			});
		}
		return "/user/account";
	}
}