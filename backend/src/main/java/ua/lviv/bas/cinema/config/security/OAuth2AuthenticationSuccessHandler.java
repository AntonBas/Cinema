package ua.lviv.bas.cinema.config.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.user.repository.UserRepository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        String token = jwtTokenProvider.generateToken(authentication);

        String fragment = "token=" + UriUtils.encode(token, StandardCharsets.UTF_8) + "&userId=" + user.getId()
                + "&email=" + UriUtils.encode(email, StandardCharsets.UTF_8);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/redirect").fragment(fragment)
                .build().toUriString();

        log.info("OAuth2 login successful for user: {}", email);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}