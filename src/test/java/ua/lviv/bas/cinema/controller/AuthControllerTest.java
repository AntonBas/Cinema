package ua.lviv.bas.cinema.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ua.lviv.bas.cinema.config.JwtTokenProvider;
import ua.lviv.bas.cinema.config.TestSecurityConfig;
import ua.lviv.bas.cinema.dto.UserLoginDto;
import ua.lviv.bas.cinema.dto.UserRegistrationDto;
import ua.lviv.bas.cinema.service.EmailTokenGeneratorService;
import ua.lviv.bas.cinema.service.EmailTokenService;
import ua.lviv.bas.cinema.service.UserService;

@WebMvcTest(AuthController.class)
@Import(TestSecurityConfig.class)
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
	private AuthenticationManager authenticationManager;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;

	@Test
	void registerUser_ShouldReturnOk_WhenValidData() throws Exception {
		UserRegistrationDto dto = createValidRegistrationDto();

		doNothing().when(userService).registerUser(any());
		when(tokenGeneratorService.generateVerificationToken(anyString())).thenReturn("token");

		mockMvc.perform(post("/api/auth/registration").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto))).andExpect(status().isOk())
				.andExpect(content().string("Check your email to confirm account"));

		verify(userService).registerUser(any());
		verify(tokenGeneratorService).generateVerificationToken(anyString());
	}

	@Test
	void registerUser_ShouldReturnBadRequest_WhenEmailExists() throws Exception {
		UserRegistrationDto dto = createValidRegistrationDto();

		doThrow(new RuntimeException("Email is already registered")).when(userService).registerUser(any());

		mockMvc.perform(post("/api/auth/registration").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
				.andExpect(content().string("Email is already registered"));

		verify(userService).registerUser(any());
		verifyNoInteractions(tokenGeneratorService);
	}

	@Test
	void registerUser_ShouldReturnBadRequest_WhenPasswordsDontMatch() throws Exception {
		UserRegistrationDto dto = createValidRegistrationDto();
		dto.setPasswordConfirm("differentPassword");

		mockMvc.perform(post("/api/auth/registration").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(dto))).andExpect(status().isBadRequest())
				.andExpect(jsonPath("$[0].defaultMessage").value("Passwords do not match"));

		verifyNoInteractions(userService, tokenGeneratorService);
	}

	@Test
	void loginUser_ShouldReturnToken_WhenValidCredentials() throws Exception {
		UserLoginDto loginDto = new UserLoginDto("anton@example.com", "password123");
		Authentication authentication = mock(Authentication.class);

		when(authenticationManager.authenticate(any())).thenReturn(authentication);
		when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwtToken");

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isOk())
				.andExpect(header().string("Authorization", "Bearer jwtToken"))
				.andExpect(content().string("Login successful"));

		verify(authenticationManager).authenticate(any());
		verify(jwtTokenProvider).generateToken(authentication);
	}

	@Test
	void loginUser_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
		UserLoginDto loginDto = new UserLoginDto("anton@example.com", "wrongPassword");

		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isUnauthorized())
				.andExpect(content().string("Invalid email or password"));

		verify(authenticationManager).authenticate(any());
		verifyNoInteractions(jwtTokenProvider);
	}

	@Test
	void loginUser_ShouldReturnUnauthorized_WhenAccountNotVerified() throws Exception {
		UserLoginDto loginDto = new UserLoginDto("anton@example.com", "password123");

		when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("Account disabled"));

		mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(loginDto))).andExpect(status().isUnauthorized())
				.andExpect(content().string("Account is not verified. Please check your email."));

		verify(authenticationManager).authenticate(any());
		verifyNoInteractions(jwtTokenProvider);
	}

	@Test
	void verifyEmail_ShouldReturnOk_WhenValidToken() throws Exception {
		doNothing().when(emailTokenService).confirmEmail("validToken");

		mockMvc.perform(get("/api/auth/verify-email").param("token", "validToken")).andExpect(status().isOk())
				.andExpect(content().string("Email successfully verified! You can now log in."));

		verify(emailTokenService).confirmEmail("validToken");
	}

	@Test
	void verifyEmail_ShouldReturnBadRequest_WhenInvalidToken() throws Exception {
		doThrow(new RuntimeException("Invalid token")).when(emailTokenService).confirmEmail("invalidToken");

		mockMvc.perform(get("/api/auth/verify-email").param("token", "invalidToken")).andExpect(status().isBadRequest())
				.andExpect(content().string("Invalid token"));

		verify(emailTokenService).confirmEmail("invalidToken");
	}

	@Test
	void checkEmailExists_ShouldReturnTrue_WhenEmailExists() throws Exception {
		when(userService.existsByEmail("anton@example.com")).thenReturn(true);

		mockMvc.perform(get("/api/auth/check-email").param("email", "anton@example.com")).andExpect(status().isOk())
				.andExpect(content().string("true"));

		verify(userService).existsByEmail("anton@example.com");
	}

	@Test
	void checkEmailExists_ShouldReturnFalse_WhenEmailNotExists() throws Exception {
		when(userService.existsByEmail("nonexistent@example.com")).thenReturn(false);

		mockMvc.perform(get("/api/auth/check-email").param("email", "nonexistent@example.com"))
				.andExpect(status().isOk()).andExpect(content().string("false"));

		verify(userService).existsByEmail("nonexistent@example.com");
	}

	private UserRegistrationDto createValidRegistrationDto() {
		return UserRegistrationDto.builder().email("anton@example.com").firstName("Anton").lastName("Bas")
				.dateOfBirth(LocalDate.of(2001, 8, 21)).city("Lviv").phoneNumber("+380123456789")
				.password("password123").passwordConfirm("password123").build();
	}
}