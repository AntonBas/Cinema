package ua.lviv.bas.cinema.controller.api;

import static org.mockito.ArgumentMatchers.any;
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ua.lviv.bas.cinema.config.security.jwt.JwtTokenProvider;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetailsService;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;
import ua.lviv.bas.cinema.dto.user.request.UserLoginRequest;
import ua.lviv.bas.cinema.dto.user.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.dto.user.response.UserResponse;
import ua.lviv.bas.cinema.mapper.user.UserMapper;
import ua.lviv.bas.cinema.service.user.UserPasswordResetService;
import ua.lviv.bas.cinema.service.user.UserService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	private ObjectMapper objectMapper = new ObjectMapper();

	@MockitoBean
	private UserService userService;

	@MockitoBean
	private UserPasswordResetService passwordResetService;

	@MockitoBean
	private AuthenticationManager authenticationManager;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@MockitoBean
	private CustomUserDetailsService customUserDetailsService;

	@MockitoBean
	private UserMapper userMapper;

	private UserRegistrationRequest registrationRequest;
	private UserLoginRequest loginRequest;
	private UserResponse userResponse;
	private User user;
	private CustomUserDetails userDetails;

	@BeforeEach
	void setUp() {
		objectMapper.registerModule(new JavaTimeModule());

		registrationRequest = new UserRegistrationRequest("anton@example.com", "Anton", "Bas",
				LocalDate.of(2001, 8, 21), "Lviv", "+380123456789", "password123", "password123");

		loginRequest = new UserLoginRequest("anton@example.com", "password123");

		userResponse = new UserResponse(1L, "anton@example.com", "Anton", "Bas", LocalDate.of(2001, 8, 21), "Lviv",
				"+380123456789", UserRole.ROLE_USER, true, VerificationStatus.NOT_VERIFIED);

		user = new User();
		user.setId(1L);
		user.setEmail("anton@example.com");
		user.setFirstName("Anton");
		user.setLastName("Bas");
		user.setPassword("encodedPassword");
		user.setUserRole(UserRole.ROLE_USER);
		user.setEnabled(true);
		user.setVerificationStatus(VerificationStatus.NOT_VERIFIED);

		userDetails = new CustomUserDetails(user);
	}

	@Test
	void registerShouldReturnCreatedWhenValidRequest() throws Exception {
		when(userService.register(any(UserRegistrationRequest.class))).thenReturn(userResponse);

		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(registrationRequest))).andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1L)).andExpect(jsonPath("$.email").value("anton@example.com"))
				.andExpect(jsonPath("$.firstName").value("Anton")).andExpect(jsonPath("$.lastName").value("Bas"));
	}

	@Test
	void registerShouldReturnBadRequestWhenInvalidEmail() throws Exception {
		UserRegistrationRequest invalidRequest = new UserRegistrationRequest("invalid-email", "Anton", "Bas",
				LocalDate.of(2001, 8, 21), "Lviv", "+380123456789", "password123", "password123");

		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest());
	}

	@Test
	void registerShouldReturnBadRequestWhenBlankFields() throws Exception {
		UserRegistrationRequest invalidRequest = new UserRegistrationRequest("", "", "", null, "", "", "", "");

		mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest());
	}

	@Test
	void loginShouldReturnOkWhenValidCredentials() throws Exception {
		Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
				userDetails.getAuthorities());

		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenReturn(authentication);
		when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwtToken");
		when(userMapper.toUserResponse(user)).thenReturn(userResponse);

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("jwtToken")).andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.user.email").value("anton@example.com"));
	}

	@Test
	void loginShouldReturnBadRequestWhenInvalidEmailFormat() throws Exception {
		UserLoginRequest invalidRequest = new UserLoginRequest("invalid-email", "password123");

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest());
	}

	@Test
	void loginShouldReturnBadRequestWhenBlankFields() throws Exception {
		UserLoginRequest invalidRequest = new UserLoginRequest("", "");

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(invalidRequest))).andExpect(status().isBadRequest());
	}

	@Test
	void loginShouldReturnUnauthorizedWhenInvalidCredentials() throws Exception {
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
				.thenThrow(new BadCredentialsException("Bad credentials"));

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isUnauthorized());
	}

	@Test
	void forgotPasswordShouldReturnOk() throws Exception {
		mockMvc.perform(post("/api/auth/password/forgot").param("email", "anton@example.com"))
				.andExpect(status().isOk());
	}

	@Test
	void forgotPasswordShouldReturnBadRequestWhenInvalidEmail() throws Exception {
		mockMvc.perform(post("/api/auth/password/forgot").param("email", "invalid-email"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void forgotPasswordShouldReturnBadRequestWhenEmailBlank() throws Exception {
		mockMvc.perform(post("/api/auth/password/forgot").param("email", "")).andExpect(status().isBadRequest());
	}

	@Test
	void resetPasswordShouldReturnOk() throws Exception {
		mockMvc.perform(post("/api/auth/password/reset").param("token", "token123").param("newPassword", "newPass"))
				.andExpect(status().isOk());
	}

	@Test
	void resetPasswordShouldReturnBadRequestWhenTokenIsBlank() throws Exception {
		mockMvc.perform(post("/api/auth/password/reset").param("token", "").param("newPassword", "newPass"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void resetPasswordShouldReturnBadRequestWhenNewPasswordIsBlank() throws Exception {
		mockMvc.perform(post("/api/auth/password/reset").param("token", "token123").param("newPassword", ""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void checkEmailShouldReturnTrueWhenEmailExists() throws Exception {
		when(userService.emailExists("anton@example.com")).thenReturn(true);

		mockMvc.perform(get("/api/auth/email/check").param("email", "anton@example.com")).andExpect(status().isOk())
				.andExpect(jsonPath("$").value(true));
	}

	@Test
	void checkEmailShouldReturnFalseWhenEmailNotExists() throws Exception {
		when(userService.emailExists("nonexistent@example.com")).thenReturn(false);

		mockMvc.perform(get("/api/auth/email/check").param("email", "nonexistent@example.com"))
				.andExpect(status().isOk()).andExpect(jsonPath("$").value(false));
	}

	@Test
	void checkEmailShouldReturnBadRequestWhenInvalidEmail() throws Exception {
		mockMvc.perform(get("/api/auth/email/check").param("email", "invalid-email"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void oauth2SuccessShouldReturnOk() throws Exception {
		when(userService.getUser(1L)).thenReturn(user);
		when(userMapper.toUserResponse(user)).thenReturn(userResponse);

		mockMvc.perform(get("/api/auth/oauth2/success").param("token", "oauth2Token").param("userId", "1")
				.param("email", "anton@example.com")).andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("oauth2Token")).andExpect(jsonPath("$.tokenType").value("Bearer"))
				.andExpect(jsonPath("$.user.email").value("anton@example.com"));
	}

	@Test
	void oauth2SuccessShouldReturnBadRequestWhenUserIdMissing() throws Exception {
		mockMvc.perform(
				get("/api/auth/oauth2/success").param("token", "oauth2Token").param("email", "anton@example.com"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void oauth2SuccessShouldReturnBadRequestWhenEmailMissing() throws Exception {
		mockMvc.perform(get("/api/auth/oauth2/success").param("token", "oauth2Token").param("userId", "1"))
				.andExpect(status().isBadRequest());
	}
}