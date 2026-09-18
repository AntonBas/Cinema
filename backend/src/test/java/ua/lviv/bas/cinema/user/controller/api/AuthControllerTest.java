package ua.lviv.bas.cinema.user.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import jakarta.servlet.http.Cookie;
import ua.lviv.bas.cinema.config.TestcontainersConfig;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.config.security.CustomUserDetailsService;
import ua.lviv.bas.cinema.config.security.JwtBlacklistService;
import ua.lviv.bas.cinema.config.security.JwtCookieService;
import ua.lviv.bas.cinema.config.security.JwtTokenProvider;
import ua.lviv.bas.cinema.config.security.OAuth2ExchangeCodeService;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;
import ua.lviv.bas.cinema.user.dto.request.UserLoginRequest;
import ua.lviv.bas.cinema.user.dto.request.UserRegistrationRequest;
import ua.lviv.bas.cinema.user.dto.response.UserResponse;
import ua.lviv.bas.cinema.user.mapper.UserMapper;
import ua.lviv.bas.cinema.user.service.UserPasswordResetService;
import ua.lviv.bas.cinema.user.service.UserService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @MockitoBean
    private JwtBlacklistService jwtBlacklistService;

    @MockitoBean
    private OAuth2ExchangeCodeService oAuth2ExchangeCodeService;

    private UserRegistrationRequest registrationRequest;
    private UserLoginRequest loginRequest;
    private UserResponse userResponse;
    private User user;
    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .addFilter((request, response, chain) -> {
                    response.setCharacterEncoding("UTF-8");
                    chain.doFilter(request, response);
                })
                .defaultRequest(get("/").header("X-Requested-With", "XMLHttpRequest"))
                .build();

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
        when(userService.getUserResponse(user.getId())).thenReturn(userResponse);

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))).andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.user.email").value("anton@example.com"))
                .andExpect(cookie().exists(JwtCookieService.COOKIE_NAME))
                .andExpect(cookie().httpOnly(JwtCookieService.COOKIE_NAME, true))
                .andExpect(cookie().value(JwtCookieService.COOKIE_NAME, "jwtToken"));
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
    void oauth2ExchangeShouldReturnOkAndSetCookieWhenCodeValid() throws Exception {
        when(oAuth2ExchangeCodeService.consume("valid-code")).thenReturn(Optional.of("anton@example.com"));
        when(userService.getUser("anton@example.com")).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);
        when(jwtTokenProvider.generateToken(any(Authentication.class))).thenReturn("jwtToken");

        mockMvc.perform(post("/api/auth/oauth2/exchange").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"valid-code\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andExpect(jsonPath("$.user.email").value("anton@example.com"))
                .andExpect(cookie().exists(JwtCookieService.COOKIE_NAME))
                .andExpect(cookie().value(JwtCookieService.COOKIE_NAME, "jwtToken"));
    }

    @Test
    void oauth2ExchangeShouldReturnBadRequestWhenCodeInvalid() throws Exception {
        when(oAuth2ExchangeCodeService.consume("bad-code")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/auth/oauth2/exchange").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"bad-code\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void oauth2ExchangeShouldReturnBadRequestWhenCodeMissing() throws Exception {
        mockMvc.perform(post("/api/auth/oauth2/exchange").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void logoutShouldClearCookieAndBlacklistToken() throws Exception {
        when(jwtTokenProvider.validateToken(anyString())).thenReturn(true);
        when(jwtTokenProvider.getJtiFromToken(anyString())).thenReturn("jti-123");
        when(jwtTokenProvider.getExpirationFromToken(anyString())).thenReturn(Instant.now().plusSeconds(3600));

        mockMvc.perform(post("/api/auth/logout").cookie(new Cookie(JwtCookieService.COOKIE_NAME, "jwtToken")))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(JwtCookieService.COOKIE_NAME, 0));

        verify(jwtBlacklistService).blacklist(eq("jti-123"), any(Duration.class));
    }

    @Test
    void logoutShouldReturnOkWhenNoCookiePresent() throws Exception {
        mockMvc.perform(post("/api/auth/logout")).andExpect(status().isOk());

        verify(jwtBlacklistService, never()).blacklist(anyString(), any(Duration.class));
    }

    @Test
    void mutatingRequestWithoutCsrfHeaderShouldReturnForbidden() throws Exception {
        MockMvc mockMvcWithoutCsrfHeader = MockMvcBuilders.webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity()).build();

        mockMvcWithoutCsrfHeader.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void resendVerificationShouldReturnOkWithCooldown() throws Exception {
        when(userService.resendVerificationEmail("anton@example.com")).thenReturn(60);

        mockMvc.perform(post("/api/auth/resend-verification").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"anton@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cooldownSeconds").value(60));
    }

    @Test
    void resendVerificationShouldReturnBadRequestWhenInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/auth/resend-verification").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid-email\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resendVerificationStatusShouldReturnOkWithCooldown() throws Exception {
        when(userService.getResendCooldownStatus("anton@example.com")).thenReturn(30);

        mockMvc.perform(get("/api/auth/resend-verification/status").param("email", "anton@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cooldownSeconds").value(30));
    }
}