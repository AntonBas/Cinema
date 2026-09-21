package ua.lviv.bas.cinema.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OAuth2AuthenticationSuccessHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OAuth2ExchangeCodeService exchangeCodeService;

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

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    private final String EMAIL = "test@gmail.com";
    private final Long USER_ID = 1L;
    private final String CODE = "one-time-code-123";

    @BeforeEach
    void setUp() {
        String FRONTEND_URL = "http://localhost:5173";
        ReflectionTestUtils.setField(successHandler, "frontendUrl", FRONTEND_URL);
    }

    @Test
    void onAuthenticationSuccess_ShouldRedirectWithOneTimeCodeNotToken() throws IOException {
        Map<String, Object> attributes = Map.of("email", EMAIL);
        User user = User.builder().id(USER_ID).email(EMAIL).build();

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(exchangeCodeService.issueCode(EMAIL)).thenReturn(CODE);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).encodeRedirectURL(urlCaptor.capture());
        String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl).contains("?code=" + CODE);
        assertThat(redirectUrl).doesNotContain("token=");
    }

    @Test
    void onAuthenticationSuccess_ShouldRedirectToLoginWithError_WhenUserNotFound() throws IOException {
        Map<String, Object> attributes = Map.of("email", EMAIL);

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).encodeRedirectURL(urlCaptor.capture());
        String redirectUrl = urlCaptor.getValue();
        assertThat(redirectUrl).contains("/login").contains("error=oauth2_failed");
        verify(exchangeCodeService, never()).issueCode(anyString());
    }

    @Test
    void onAuthenticationSuccess_ShouldIssueCodeForCorrectEmail() throws IOException {
        Map<String, Object> attributes = Map.of("email", EMAIL);
        User user = User.builder().id(USER_ID).email(EMAIL).build();

        when(authentication.getPrincipal()).thenReturn(oAuth2User);
        when(oAuth2User.getAttributes()).thenReturn(attributes);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(exchangeCodeService.issueCode(EMAIL)).thenReturn(CODE);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(exchangeCodeService).issueCode(EMAIL);
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
        when(exchangeCodeService.issueCode(specialEmail)).thenReturn(CODE);

        successHandler.onAuthenticationSuccess(request, response, authentication);

        verify(response).encodeRedirectURL(anyString());
    }
}
