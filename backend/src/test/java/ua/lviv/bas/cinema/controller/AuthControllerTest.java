package ua.lviv.bas.cinema.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.config.JwtTokenProvider;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.dto.user.request.UserLoginRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.response.UserProfileResponse;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.exception.InvalidTokenException;
import ua.lviv.bas.cinema.exception.TokenAlreadyConfirmedException;
import ua.lviv.bas.cinema.exception.TokenExpiredException;
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
	private PasswordResetService passwordResetService;

	@MockitoBean
	private AuthenticationManager authenticationManager;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	private UserRegistrationRequest registrationDto;
	private UserLoginRequest loginDto;
	private User user;
	private UserProfileResponse userProfileResponse;
	private UserResponse userResponse;

	@BeforeEach
	void setUp() {
		registrationDto = UserRegistrationRequest.builder().email("anton@example.com").firstName("Anton")
				.lastName("Bas").dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.password("password123").passwordConfirm("password123").build();

		loginDto = new UserLoginRequest("anton@example.com", "password123");

		user = new User();
		user.setId(1L);
		user.setEmail("anton@example.com");
		user.setFirstName("Anton");
		user.setLastName("Bas");
		user.setUserRole(UserRole.ROLE_USER);

		userProfileResponse = UserProfileResponse.builder().id(1L).email("new@example.com").firstName("Anton")
				.lastName("Bas").build();

		userResponse = UserResponse.builder().id(1L).email("anton@example.com").firstName("Anton").lastName("Bas")
				.userRole(UserRole.ROLE_USER).enabled(false).build();
	}

	@Test
	void registerUser_ShouldReturnOk_WhenValidRequest() throws Exception {
		mockMvc.perform(post("/api/auth/registration").contentType("application/json")
				.content(objectMapper.writeValueAsString(registrationDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true));
	}

	@Test
	void loginUser_ShouldReturnOk_WhenValidCredentials() throws Exception {
		when(userService.findByEmail(loginDto.getEmail())).thenReturn(user);
		when(userService.getUserById(user.getId())).thenReturn(userResponse);
		when(jwtTokenProvider.generateToken(org.mockito.Mockito.any())).thenReturn("jwtToken");

		mockMvc.perform(post("/api/auth/login").contentType("application/json")
				.content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.token").value("jwtToken"))
				.andExpect(jsonPath("$.user.email").value("anton@example.com"));

		verify(userService).findByEmail(loginDto.getEmail());
		verify(userService).getUserById(user.getId());
	}

	@Test
	void forgotPassword_ShouldReturnOk() throws Exception {
		mockMvc.perform(post("/api/auth/forgot-password").param("email", "anton@example.com"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));

		verify(passwordResetService).requestPasswordReset("anton@example.com");
	}

	@Test
	void resetPassword_ShouldReturnOk() throws Exception {
		mockMvc.perform(post("/api/auth/reset-password").param("token", "token123").param("newPassword", "newPass"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));

		verify(passwordResetService).resetPassword("token123", "newPass");
	}

	@Test
	void confirmEmailChange_ShouldReturnOk_WhenValidToken() throws Exception {
		when(userService.confirmEmailChange("validToken")).thenReturn(userProfileResponse);

		mockMvc.perform(post("/api/auth/email/confirm-change").param("token", "validToken")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true)).andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.email").value("new@example.com"));

		verify(userService).confirmEmailChange("validToken");
	}

	@Test
	void verifyEmail_ShouldReturnOk_WhenValidToken() throws Exception {
		when(userService.confirmEmailChange("verifyToken")).thenReturn(userProfileResponse);

		mockMvc.perform(get("/api/auth/verify-email").param("token", "verifyToken")).andExpect(status().isOk())
				.andExpect(jsonPath("$.success").value(true))
				.andExpect(jsonPath("$.message").value("Email verified successfully for: new@example.com"));

		verify(userService).confirmEmailChange("verifyToken");
	}

	@Test
	void checkEmailExists_ShouldReturnTrueOrFalse() throws Exception {
		when(userService.existsByEmail("anton@example.com")).thenReturn(true);

		mockMvc.perform(get("/api/auth/check-email").param("email", "anton@example.com")).andExpect(status().isOk())
				.andExpect(jsonPath("$.exists").value(true));

		verify(userService).existsByEmail("anton@example.com");
	}

	@Test
	void confirmEmailChange_ShouldReturnBadRequest_WhenInvalidToken() throws Exception {
		when(userService.confirmEmailChange("badToken")).thenThrow(new InvalidTokenException("Invalid token"));

		mockMvc.perform(post("/api/auth/email/confirm-change").param("token", "badToken"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid token"));
	}

	@Test
	void confirmEmailChange_ShouldReturnBadRequest_WhenTokenExpired() throws Exception {
		when(userService.confirmEmailChange("expiredToken")).thenThrow(new TokenExpiredException("Token expired"));

		mockMvc.perform(post("/api/auth/email/confirm-change").param("token", "expiredToken"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Token expired"));
	}

	@Test
	void confirmEmailChange_ShouldReturnBadRequest_WhenTokenAlreadyConfirmed() throws Exception {
		when(userService.confirmEmailChange("confirmedToken"))
				.thenThrow(new TokenAlreadyConfirmedException("Token already confirmed"));

		mockMvc.perform(post("/api/auth/email/confirm-change").param("token", "confirmedToken"))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Token already confirmed"));
	}
}