package ua.lviv.bas.cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jakarta.validation.Valid;
import ua.lviv.bas.cinema.dto.UserLoginDto;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;
import ua.lviv.bas.cinema.service.UserService;

@Controller
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/registration", method = RequestMethod.GET)
	public String registration(Model model) {
		model.addAttribute("userForm", new UserRegistrationDto());
		return "registration";
	}

	@RequestMapping(value = "/registration", method = RequestMethod.POST)
	public String registration(@ModelAttribute("userForm") @Valid UserRegistrationDto userForm,
			BindingResult bindingResult, Model model) {
		if (bindingResult.hasErrors()) {
			return "registration";
		}

		if (!userForm.getPassword().equals(userForm.getPasswordConfirm())) {
			bindingResult.rejectValue("passwordConfirm", "error.userForm", "Passwords do not match");
			return "registration";
		}

		try {
			userService.save(userForm);
		} catch (RuntimeException e) {
			model.addAttribute("error", e.getMessage());
			return "registration";
		}

		return "redirect:/home";
	}

	@RequestMapping(value = { "/", "/login" }, method = RequestMethod.GET)
	public String login(Model model, String error, String logout) {
		if (error != null)
			model.addAttribute("error", "Your username and password is invalid.");

		if (logout != null)
			model.addAttribute("message", "You have been logged out successfully.");

		model.addAttribute("user", new UserLoginDto());
		return "login";
	}

	@RequestMapping(value = "/home", method = RequestMethod.GET)
	public String home(Model model) {
		return "home";
	}

}
