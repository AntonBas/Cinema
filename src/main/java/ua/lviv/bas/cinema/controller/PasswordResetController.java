package ua.lviv.bas.cinema.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
		return "redirect:/forgot-password?sent=true";
	}

	@GetMapping("/reset-password")
	public String showResetPasswordForm(@RequestParam String token, @RequestParam(required = false) String error,
			Model model) {
		model.addAttribute("token", token);
		model.addAttribute("errorMessage", error);
		return "auth/reset-password";
	}

	@PostMapping("/reset-password")
	public String processResetPassword(@RequestParam String token, @RequestParam String password,
			@RequestParam String confirmPassword, RedirectAttributes redirectAttributes) {
		if (!password.equals(confirmPassword)) {
			redirectAttributes.addAttribute("token", token);
			redirectAttributes.addAttribute("error", "Passwords don't match");
			return "redirect:/reset-password";
		}

		try {
			passwordResetService.resetPassword(token, password);
		} catch (RuntimeException e) {
			redirectAttributes.addAttribute("token", token);
			redirectAttributes.addAttribute("error", e.getMessage());
			return "redirect:/reset-password";
		}

		return "redirect:/login?resetSuccess=true";
	}
}
