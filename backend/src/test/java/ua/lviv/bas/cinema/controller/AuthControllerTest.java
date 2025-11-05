package ua.lviv.bas.cinema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.config.JwtTokenProvider;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.user.request.UserLoginRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.service.EmailTokenGeneratorService;
import ua.lviv.bas.cinema.service.EmailTokenService;
import ua.lviv.bas.cinema.service.PasswordResetService;
import ua.lviv.bas.cinema.service.UserService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private EmailTokenGeneratorService tokenGeneratorService;

	@MockitoBean
	private EmailTokenService emailTokenService;

	@MockitoBean
	private PasswordResetService passwordResetService;

	@MockitoBean
	private AuthenticationManager authenticationManager;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	private UserRegistrationRequest registrationDto;
	private UserLoginRequest loginDto;
	private User user;

	@BeforeEach
	void setUp() {
		registrationDto = UserRegistrationRequest.builder().email("anton@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.password("password123").passwordConfirm("password123").build();

		loginDto = new UserLoginRequest("anton@example.com", "password123");

		user = new User();
		user.setId(1L);
		user.setEmail("anton@example.com");
		user.setFirstName("Anton");
		user.setLastName("Bas");
	}

	@Test
	void registerUser_ShouldReturnOk_WhenValidData() throws Exception {
		when(userService.registerUser(any())).thenReturn(
				UserResponse.builder().id(1L).email("anton@example.com").firstName("Anton").lastName("Bas").build());

		when(tokenGeneratorService.generateVerificationToken(anyString())).thenReturn(UUID.randomUUID().toString());

		mockMvc.perform(post("/api/auth/registration").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registrationDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Check your email to confirm account"));

		verify(userService).registerUser(any());
		verify(tokenGeneratorService).generateVerificationToken(eq("anton@example.com"));
	}

	@Test
	void loginUser_ShouldReturnToken_WhenValidCredentials() throws Exception {
		Authentication auth = mock(Authentication.class);
		when(authenticationManager.authenticate(any())).thenReturn(auth);
		when(jwtTokenProvider.generateToken(auth)).thenReturn("jwtToken");
		when(userService.findByEmail(anyString())).thenReturn(user);

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isOk())
				.andExpect(header().string("Authorization", "Bearer jwtToken"))
				.andExpect(jsonPath("$.message").value("Login successful"))
				.andExpect(jsonPath("$.token").value("jwtToken"))
				.andExpect(jsonPath("$.user.email").value("anton@example.com"));

		verify(authenticationManager).authenticate(any());
		verify(jwtTokenProvider).generateToken(auth);
	}

	@Test
	void loginUser_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Invalid email or password"));
	}

	@Test
	void loginUser_ShouldReturnUnauthorized_WhenAccountNotVerified() throws Exception {
		when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("Account disabled"));

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.success").value(false))
				.andExpect(jsonPath("$.message").value("Account is not verified. Please check your email."));
	}

	@Test
	void verifyEmail_ShouldReturnOk_WhenValidToken() throws Exception {
		when(emailTokenService.confirmEmail("validToken")).thenReturn("success");

		mockMvc.perform(get("/api/auth/verify-email").param("token", "validToken")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Email successfully verified! You can now log in."));

		verify(emailTokenService).confirmEmail("validToken");
	}

	@Test
	void verifyEmail_ShouldReturnBadRequest_WhenInvalidToken() throws Exception {
		doThrow(new RuntimeException("Invalid token")).when(emailTokenService).confirmEmail("badToken");

		mockMvc.perform(get("/api/auth/verify-email").param("token", "badToken")).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.success").value(false)).andExpect(jsonPath("$.message").value("Invalid token"));
	}

	@Test
	void checkEmailExists_ShouldReturnTrue_WhenEmailExists() throws Exception {
		when(userService.existsByEmail("anton@example.com")).thenReturn(true);

		mockMvc.perform(get("/api/auth/check-email").param("email", "anton@example.com")).andExpect(status().isOk())
				.andExpect(jsonPath("$.exists").value(true));

		verify(userService).existsByEmail("anton@example.com");
	}

	@Test
	void checkEmailExists_ShouldReturnFalse_WhenEmailNotExists() throws Exception {
		when(userService.existsByEmail("nonexistent@example.com")).thenReturn(false);

		mockMvc.perform(get("/api/auth/check-email").param("email", "nonexistent@example.com"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.exists").value(false));

		verify(userService).existsByEmail("nonexistent@example.com");
	}

	@Test
	void forgotPassword_ShouldReturnOk_WhenValidEmail() throws Exception {
		doNothing().when(passwordResetService).requestPasswordReset(anyString());

		mockMvc.perform(post("/api/auth/forgot-password").param("email", "anton@example.com"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Instructions have been sent to your email address"));

		verify(passwordResetService).requestPasswordReset("anton@example.com");
	}

	@Test
	void resetPassword_ShouldReturnOk_WhenValidToken() throws Exception {
		doNothing().when(passwordResetService).resetPassword(anyString(), anyString());

		mockMvc.perform(
				post("/api/auth/reset-password").param("token", "validToken").param("newPassword", "newPass123"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Password has been successfully changed"));

		verify(passwordResetService).resetPassword("validToken", "newPass123");
	}
}
