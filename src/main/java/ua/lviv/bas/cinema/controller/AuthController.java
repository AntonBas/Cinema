package ua.lviv.bas.cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import ua.lviv.bas.cinema.dto.UserLoginDto;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;
import ua.lviv.bas.cinema.service.UserService;

@Controller
public class AuthController {

	@Autowired
	private UserService userService;

	@GetMapping("/registration")
	public String registration(Model model) {
		model.addAttribute("userForm", new UserRegistrationDto());
		return "auth/registration";
	}

	@PostMapping("/registration")
	public String registration(@ModelAttribute("userForm") @Valid UserRegistrationDto userForm,
			BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			return "auth/registration";
		}

		if (!userForm.getPassword().equals(userForm.getPasswordConfirm())) {
			bindingResult.rejectValue("passwordConfirm", "error.userForm", "Passwords do not match");
			return "auth/registration";
		}

		try {
			userService.save(userForm);
		} catch (RuntimeException e) {
			model.addAttribute("error", e.getMessage());
			return "auth/registration";
		}

		return "redirect:/home";
	}

	@GetMapping({"/", "/login"})
	public String login(Model model, String error, String logout) {
		if (error != null)
			model.addAttribute("error", "Your username and password is invalid.");

		if (logout != null)
			model.addAttribute("message", "You have been logged out successfully.");

		model.addAttribute("user", new UserLoginDto());
		return "auth/login";
	}

	@GetMapping("/home")
	public String home(Model model) {
		return "home";
	}

}
