package ua.lviv.bas.cinema.controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.service.UserService;

@Controller
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/account")
	public String myAccount(Model model, Principal principal) {
		String email = principal.getName();
		User user = userService.findByEmail(email);

		model.addAttribute("firstName", user.getFirstName());
		model.addAttribute("lastName", user.getLastName());
		model.addAttribute("user", user);

		return "user/account";
	}

	@PostMapping("/account/update")
	public String updateUser(@Valid @ModelAttribute("user") User user, BindingResult result, Principal principal,
			RedirectAttributes redirectAttributes) {

		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "Data entered incorrectly");
			return "redirect:/account";
		}

		try {
			User currentUser = userService.findByEmail(principal.getName());

//			currentUser.setEmail(user.getEmail());
			currentUser.setFirstName(user.getFirstName());
			currentUser.setLastName(user.getLastName());
			currentUser.setDateOfBirth(user.getDateOfBirth());
			currentUser.setCity(user.getCity());
			currentUser.setPhoneNumber(user.getPhoneNumber());

			userService.updateUser(currentUser);
			redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("error", "Error updating profile");
		}

		return "redirect:/account";
	}

}