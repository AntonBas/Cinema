package ua.lviv.bas.cinema.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import ua.lviv.bas.cinema.exception.api.ApiError;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class CsrfHeaderFilter extends OncePerRequestFilter {

    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");
    private static final String REQUIRED_HEADER = "X-Requested-With";
    private static final String LIQPAY_CALLBACK_PATH = "/api/liqpay/callback";

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        boolean exempt = SAFE_METHODS.contains(request.getMethod()) || LIQPAY_CALLBACK_PATH.equals(request.getRequestURI());
        if (!exempt && request.getHeader(REQUIRED_HEADER) == null) {
            writeForbidden(request, response);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private void writeForbidden(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ApiError apiError = new ApiError(HttpStatus.FORBIDDEN, "Missing required header: " + REQUIRED_HEADER);
        apiError.setPath(request.getRequestURI());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), apiError);
    }
}
