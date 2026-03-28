package ua.lviv.bas.cinema.service.oauth2;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ua.lviv.bas.cinema.config.security.jwt.JwtTokenProvider;
import ua.lviv.bas.cinema.config.security.oauth2.OAuth2AuthenticationSuccessHandler;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class OAuth2AuthenticationSuccessHandlerTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private UserRepository userRepository;

	@Mock
	private HttpServletRequest request;

	@Mock
	private HttpServletResponse response;

	@Mock
	private Authentication authentication;

	@Mock
	private OAuth2User oAuth2User;

	@InjectMocks
	private OAuth2AuthenticationSuccessHandler successHandler;

	private final String FRONTEND_URL = "http://localhost:5173";
	private final String EMAIL = "test@gmail.com";
	private final Long USER_ID = 1L;
	private final String TOKEN = "jwt-token-123";

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(successHandler, "frontendUrl", FRONTEND_URL);
	}

	@Test
	void onAuthenticationSuccess_ShouldRedirectWithTokenAndUserData() throws IOException {
		Map<String, Object> attributes = Map.of("email", EMAIL);
		User user = User.builder().id(USER_ID).email(EMAIL).build();

		when(authentication.getPrincipal()).thenReturn(oAuth2User);
		when(oAuth2User.getAttributes()).thenReturn(attributes);
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		when(jwtTokenProvider.generateToken(authentication)).thenReturn(TOKEN);

		successHandler.onAuthenticationSuccess(request, response, authentication);

		verify(response).encodeRedirectURL(anyString());
	}

	@Test
	void onAuthenticationSuccess_ShouldThrowException_WhenUserNotFound() {
		Map<String, Object> attributes = Map.of("email", EMAIL);

		when(authentication.getPrincipal()).thenReturn(oAuth2User);
		when(oAuth2User.getAttributes()).thenReturn(attributes);
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> successHandler.onAuthenticationSuccess(request, response, authentication))
				.isInstanceOf(RuntimeException.class).hasMessageContaining("User not found with email: " + EMAIL);
	}

	@Test
	void onAuthenticationSuccess_ShouldGenerateTokenWithAuthentication() throws IOException {
		Map<String, Object> attributes = Map.of("email", EMAIL);
		User user = User.builder().id(USER_ID).email(EMAIL).build();

		when(authentication.getPrincipal()).thenReturn(oAuth2User);
		when(oAuth2User.getAttributes()).thenReturn(attributes);
		when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
		when(jwtTokenProvider.generateToken(authentication)).thenReturn(TOKEN);

		successHandler.onAuthenticationSuccess(request, response, authentication);

		verify(jwtTokenProvider).generateToken(authentication);
		verify(response).encodeRedirectURL(anyString());
	}

	@Test
	void onAuthenticationSuccess_ShouldHandleEmailWithSpecialCharacters() throws IOException {
		String specialEmail = "test.test+alias@gmail.com";
		Map<String, Object> attributes = Map.of("email", specialEmail);
		User user = User.builder().id(USER_ID).email(specialEmail).build();

		when(authentication.getPrincipal()).thenReturn(oAuth2User);
		when(oAuth2User.getAttributes()).thenReturn(attributes);
		when(userRepository.findByEmail(specialEmail)).thenReturn(Optional.of(user));
		when(jwtTokenProvider.generateToken(authentication)).thenReturn(TOKEN);

		successHandler.onAuthenticationSuccess(request, response, authentication);

		verify(response).encodeRedirectURL(anyString());
	}
}