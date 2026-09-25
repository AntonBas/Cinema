package ua.lviv.bas.cinema.config.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TOKEN = "jwt-token";
    private static final String EMAIL = "user@test.com";

    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private JwtCookieService jwtCookieService;
    @Mock
    private JwtBlacklistService jwtBlacklistService;
    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
        when(jwtCookieService.extractToken(request)).thenReturn(TOKEN);
        when(jwtTokenProvider.validateToken(TOKEN)).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken(TOKEN)).thenReturn(EMAIL);
        when(jwtTokenProvider.getJtiFromToken(TOKEN)).thenReturn("jti");
        when(userDetailsService.loadUserByUsername(EMAIL))
                .thenReturn(new CustomUserDetails(1L, EMAIL, "hash", true, true, "ROLE_USER", 0));
        when(jwtTokenProvider.getTokenVersionFromToken(TOKEN)).thenReturn(0);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAuthenticateWhenUserIdClaimMatches() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken(TOKEN)).thenReturn(1L);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenTokenBelongsToAnotherAccountWithSameEmail() throws Exception {
        when(jwtTokenProvider.getUserIdFromToken(TOKEN)).thenReturn(99L);

        filter.doFilter(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }
}
