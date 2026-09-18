package ua.lviv.bas.cinema.config.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class JwtCookieServiceTest {

    private final JwtCookieService cookieService = new JwtCookieService(86_400_000L, true, "Lax");

    @Test
    void addTokenCookieShouldSetHttpOnlySecureCookieWithToken() {
        var response = new MockHttpServletResponse();

        cookieService.addTokenCookie(response, "jwt-token-value");

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("jwt=jwt-token-value").contains("HttpOnly").contains("Secure")
                .contains("SameSite=Lax").contains("Path=/");
    }

    @Test
    void clearTokenCookieShouldSetEmptyValueWithZeroMaxAge() {
        var response = new MockHttpServletResponse();

        cookieService.clearTokenCookie(response);

        String setCookie = response.getHeader("Set-Cookie");
        assertThat(setCookie).contains("jwt=").contains("Max-Age=0");
    }

    @Test
    void extractTokenShouldReturnValueWhenCookiePresent() {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("jwt", "extracted-token"));

        assertThat(cookieService.extractToken(request)).isEqualTo("extracted-token");
    }

    @Test
    void extractTokenShouldReturnNullWhenCookieAbsent() {
        var request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other", "value"));

        assertThat(cookieService.extractToken(request)).isNull();
    }

    @Test
    void extractTokenShouldReturnNullWhenNoCookiesAtAll() {
        var request = new MockHttpServletRequest();

        assertThat(cookieService.extractToken(request)).isNull();
    }
}
