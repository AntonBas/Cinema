package ua.lviv.bas.cinema.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
public class CsrfHeaderFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final String REQUIRED_HEADER = "X-Requested-With";
    private static final String LIQPAY_CALLBACK_PATH = "/api/liqpay/callback";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        boolean exempt = SAFE_METHODS.contains(request.getMethod()) || LIQPAY_CALLBACK_PATH.equals(request.getRequestURI());
        if (!exempt && request.getHeader(REQUIRED_HEADER) == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Missing required header: " + REQUIRED_HEADER);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
