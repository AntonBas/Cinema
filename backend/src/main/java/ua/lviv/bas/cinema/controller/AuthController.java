package ua.lviv.bas.cinema.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.config.JwtTokenProvider;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.UserDto;
import ua.lviv.bas.cinema.dto.UserLoginDto;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;
import ua.lviv.bas.cinema.service.EmailTokenGeneratorService;
import ua.lviv.bas.cinema.service.EmailTokenService;
import ua.lviv.bas.cinema.service.UserService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final UserService userService;
	private final EmailTokenGeneratorService tokenGeneratorService;
	private final EmailTokenService emailTokenService;
	private final AuthenticationManager authenticationManager;
	private final JwtTokenProvider jwtTokenProvider;

	@PostMapping("/registration")
	public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto userDto,
			BindingResult bindingResult) {
		if (bindingResult.hasErrors()) {
			return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
		}

		if (!userDto.getPassword().equals(userDto.getPasswordConfirm())) {
			return ResponseEntity.badRequest().body("Password do not match");
		}

		try {
			userService.registerUser(userDto);
			tokenGeneratorService.generateVerificationToken(userDto.getEmail());
			return ResponseEntity.ok().body("Check your email to confirm account");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@Valid @RequestBody UserLoginDto loginDto) {
		try {
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));

			String token = jwtTokenProvider.generateToken(authentication);

			return ResponseEntity.ok().header("Authorization", "Bearer " + token).body("Login successful");

		} catch (BadCredentialsException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
		} catch (DisabledException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body("Account is not verified. Please check your email.");
		}
	}

	@GetMapping("/verify-email")
	public ResponseEntity<?> confirmEmailToken(@RequestParam("token") String token) {
		try {
			emailTokenService.confirmEmail(token);
			return ResponseEntity.ok("Email successfully verified! You can now log in.");
		} catch (RuntimeException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}

	@GetMapping("/check-email")
	public ResponseEntity<?> checkEmailExists(@RequestParam String email) {
		boolean exists = userService.existsByEmail(email);
		return ResponseEntity.ok(exists);
	}

	@GetMapping("profile")
	public ResponseEntity<UserDto> getProfile(Authentication authentication) {
		try {
			String email = authentication.getName();

			User user = userService.findByEmail(email);

			UserDto userDto = UserDto.builder().id(user.getId()).email(user.getEmail()).firstName(user.getFirstName())
					.lastName(user.getLastName()).dateOfBirth(user.getDateOfBirth()).city(user.getCity())
					.phoneNumber(user.getPhoneNumber()).userRole(user.getUserRole()).enabled(user.isEnabled()).build();

			return ResponseEntity.ok(userDto);

		} catch (EntityNotFoundException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}
}
