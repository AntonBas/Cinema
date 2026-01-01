package ua.lviv.bas.cinema.controller.api;

import static org.mockito.Mockito.never;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
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
import ua.lviv.bas.cinema.exception.domain.auth.InvalidTokenException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenAlreadyConfirmedException;
import ua.lviv.bas.cinema.exception.domain.auth.TokenExpiredException;
import ua.lviv.bas.cinema.service.user.UserPasswordResetService;
import ua.lviv.bas.cinema.service.user.UserService;

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
	private UserPasswordResetService passwordResetService;

	@MockitoBean
	private AuthenticationManager authenticationManager;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	private UserRegistrationRequest registrationRequest;
	private UserLoginRequest loginRequest;
	private User user;
	private UserProfileResponse userProfileResponse;
	private UserResponse userResponse;

	@BeforeEach
	void setUp() {
		registrationRequest = UserRegistrationRequest.builder().email("anton@example.com").firstName("Anton")
				.lastName("Bas").dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.password("password123").passwordConfirm("password123").build();

		loginRequest = new UserLoginRequest("anton@example.com", "password123");

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
	void register_ShouldReturnCreated_WhenValidRequest() throws Exception {
		when(userService.registerUser(registrationRequest)).thenReturn(userResponse);

		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registrationRequest))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.email").value("anton@example.com"))
				.andExpect(jsonPath("$.firstName").value("Anton")).andExpect(jsonPath("$.lastName").value("Bas"));

		verify(userService).registerUser(registrationRequest);
	}

	@Test
	void login_ShouldReturnOk_WhenValidCredentials() throws Exception {
		Authentication authentication = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
				loginRequest.getPassword());

		when(authenticationManager
				.authenticate(org.mockito.ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwtToken");
		when(userService.getByEmail(loginRequest.getEmail())).thenReturn(user);
		when(userService.getUserById(user.getId())).thenReturn(userResponse);

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("jwtToken")).andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.user.email").value("anton@example.com"));

		verify(authenticationManager)
				.authenticate(org.mockito.ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class));
		verify(userService).getByEmail(loginRequest.getEmail());
		verify(userService).getUserById(user.getId());
	}

	@Test
	void getCurrentUser_ShouldReturnUnauthorized_WhenNotAuthenticated() throws Exception {
		mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());

		verify(userService, never()).getUserById(1L);
	}

	@Test
	void forgotPassword_ShouldReturnOk() throws Exception {
		mockMvc.perform(post("/api/auth/password/forgot").param("email", "anton@example.com"))
				.andExpect(status().isOk());

		verify(passwordResetService).requestPasswordReset("anton@example.com");
	}

	@Test
	void resetPassword_ShouldReturnOk() throws Exception {
		mockMvc.perform(post("/api/auth/password/reset").param("token", "token123").param("newPassword", "newPass"))
				.andExpect(status().isOk());

		verify(passwordResetService).resetPassword("token123", "newPass");
	}

	@Test
	void verifyEmail_ShouldReturnOk_WhenValidToken() throws Exception {
		when(userService.confirmRegistration("validToken")).thenReturn("Email verified successfully");

		mockMvc.perform(post("/api/auth/email/verify").param("token", "validToken")).andExpect(status().isOk())
				.andExpect(jsonPath("$.message").value("Email verified successfully"));

		verify(userService).confirmRegistration("validToken");
	}

	@Test
	void confirmEmailChange_ShouldReturnOk_WhenValidToken() throws Exception {
		when(userService.confirmEmailChange("validToken")).thenReturn(userProfileResponse);

		mockMvc.perform(post("/api/auth/email/change/confirm").param("token", "validToken")).andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.email").value("new@example.com"));

		verify(userService).confirmEmailChange("validToken");
	}

	@Test
	void checkEmailExists_ShouldReturnTrue_WhenEmailExists() throws Exception {
		when(userService.existsByEmail("anton@example.com")).thenReturn(true);

		mockMvc.perform(get("/api/auth/email/check").param("email", "anton@example.com")).andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));

		verify(userService).existsByEmail("anton@example.com");
	}

	@Test
	void checkEmailExists_ShouldReturnFalse_WhenEmailNotExists() throws Exception {
		when(userService.existsByEmail("nonexistent@example.com")).thenReturn(false);

		mockMvc.perform(get("/api/auth/email/check").param("email", "nonexistent@example.com"))
				.andExpect(status().isOk()).andExpect(jsonPath("$").value(false));

		verify(userService).existsByEmail("nonexistent@example.com");
	}

	@Test
	void confirmEmailChange_ShouldReturnBadRequest_WhenInvalidToken() throws Exception {
		when(userService.confirmEmailChange("badToken")).thenThrow(new InvalidTokenException("Invalid token"));

		mockMvc.perform(post("/api/auth/email/change/confirm").param("token", "badToken"))
				.andExpect(status().isBadRequest());

		verify(userService).confirmEmailChange("badToken");
	}

	@Test
	void confirmEmailChange_ShouldReturnBadRequest_WhenTokenExpired() throws Exception {
		when(userService.confirmEmailChange("expiredToken")).thenThrow(new TokenExpiredException("Token expired"));

		mockMvc.perform(post("/api/auth/email/change/confirm").param("token", "expiredToken"))
				.andExpect(status().isBadRequest());

		verify(userService).confirmEmailChange("expiredToken");
	}

	@Test
	void confirmEmailChange_ShouldReturnConflict_WhenTokenAlreadyConfirmed() throws Exception {
		when(userService.confirmEmailChange("confirmedToken"))
				.thenThrow(new TokenAlreadyConfirmedException("Token already confirmed"));

		mockMvc.perform(post("/api/auth/email/change/confirm").param("token", "confirmedToken"))
				.andExpect(status().isConflict());

		verify(userService).confirmEmailChange("confirmedToken");
	}
}