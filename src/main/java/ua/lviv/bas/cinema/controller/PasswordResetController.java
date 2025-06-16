package ua.lviv.bas.cinema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.service.PasswordResetService;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

	private final PasswordResetService passwordResetService;

	@GetMapping("/forgot-password")
	public String showForgotPasswordForm() {
		return "auth/forgot-password";
	}

	@PostMapping("/forgot-password")
	public String processForgotPassword(@RequestParam String email) {
		passwordResetService.requestPasswordReset(email);
		return "redirect:/forgot-password?sent-true";
	}

	@GetMapping("/reset-password")
	public String showResetPasswordForm(@RequestParam String token, Model model) {
		model.addAttribute("token", token);
		return "auth/reset-password";
	}

	@PostMapping("/reset-password")
	public String processResetPassword(@RequestParam String token, @RequestParam String password,
			@RequestParam String confirmPassword) {
		if (!password.equals(confirmPassword)) {
			return "redirect:/reset-password?token=" + token + "&error=Passwords don't match";
		}

		passwordResetService.resetPassword(token, password);
		return "redirect:/login?resetSuccess=true";
	}
}
