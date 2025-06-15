//package ua.lviv.bas.cinema.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//import jakarta.validation.Valid;
//import ua.lviv.bas.cinema.dto.UserLoginDto;
//import ua.lviv.bas.cinema.dto.UserRegistrationDto;
//import ua.lviv.bas.cinema.service.EmailTokenGeneratorService;
//import ua.lviv.bas.cinema.service.EmailTokenService;
//import ua.lviv.bas.cinema.service.UserService;
//
//@Controller
//public class AuthController {
//
//	@Autowired
//	private UserService userService;
//
//	@Autowired
//	private EmailTokenGeneratorService tokenGeneratorService;
//
//	@Autowired
//	private EmailTokenService emailTokenService;
//
//	@GetMapping("/registration")
//	public String registration(Model model) {
//		model.addAttribute("userForm", new UserRegistrationDto());
//		return "auth/registration";
//	}
//
//	@PostMapping("/registration")
//	public String registration(@ModelAttribute("userForm") @Valid UserRegistrationDto userForm,
//			BindingResult bindingResult, Model model) {
//		if (bindingResult.hasErrors()) {
//			return "auth/registration";
//		}
//
//		if (!userForm.getPassword().equals(userForm.getPasswordConfirm())) {
//			bindingResult.rejectValue("passwordConfirm", "error.userForm", "Passwords do not match");
//			return "auth/registration";
//		}
//
//		try {
//			userService.save(userForm);
//			tokenGeneratorService.generateVerificationToken(userForm.getEmail());
//			model.addAttribute("message", "Check your email to confirm your account.");
//			return "auth/login";
//		} catch (RuntimeException e) {
//			model.addAttribute("error", e.getMessage());
//			return "auth/registration";
//		}
//	}
//
//	@GetMapping({ "/", "/login" })
//	public String login(Model model, String error, String logout) {
//		if (error != null)
//			model.addAttribute("error", "Your username and password is invalid.");
//
//		if (logout != null)
//			model.addAttribute("message", "You have been logged out successfully.");
//
//		model.addAttribute("user", new UserLoginDto());
//		return "auth/login";
//	}
//
//	@GetMapping("/home")
//	public String home(Model model) {
//		return "home";
//	}
//
//	@GetMapping("/verify-email")
//	public String verifyEmail(@RequestParam("token") String token, Model model) {
//		try {
//			emailTokenService.confirmEmail(token);
//			model.addAttribute("successMessage", "Email successfully verified! You can now log in.");
//			return "auth/email-confirmation";
//		} catch (RuntimeException e) {
//			model.addAttribute("errorMessage", e.getMessage());
//			return "auth/email-confirmation";
//		}
//	}
//}

package ua.lviv.bas.cinema.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import ua.lviv.bas.cinema.dto.UserLoginDto;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;
import ua.lviv.bas.cinema.service.EmailTokenGeneratorService;
import ua.lviv.bas.cinema.service.EmailTokenService;
import ua.lviv.bas.cinema.service.UserService;

@Controller
public class AuthController {

	@Autowired
	private UserService userService;

	@Autowired
	private EmailTokenGeneratorService tokenGeneratorService;

	@Autowired
	private EmailTokenService emailTokenService;

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
			tokenGeneratorService.generateVerificationToken(userForm.getEmail());
			model.addAttribute("message", "Check your email to confirm your account.");
			return "redirect:/login?message=Check_your_email_to_confirm_your_account";
		} catch (RuntimeException e) {
			model.addAttribute("error", e.getMessage());
			return "auth/registration";
		}
	}

	@GetMapping("/login")
	public String login(Model model, @RequestParam(required = false) String message) {
		if (message != null) {
			model.addAttribute("message", message.replace("_", " "));
		}
		model.addAttribute("user", new UserLoginDto());
		return "auth/login";
	}

	@GetMapping("/home")
	public String home(Model model) {
		return "home";
	}

	@GetMapping("/verify-email")
	public String verifyEmail(@RequestParam("token") String token, Model model) {
		try {
			emailTokenService.confirmEmail(token);
			model.addAttribute("successMessage", "Email successfully verified! You can now log in.");
			return "auth/email-confirmation";
		} catch (RuntimeException e) {
			model.addAttribute("errorMessage", e.getMessage());
			return "auth/email-confirmation";
		}
	}
}
