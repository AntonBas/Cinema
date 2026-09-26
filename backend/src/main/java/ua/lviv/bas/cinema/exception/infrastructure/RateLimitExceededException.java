package ua.lviv.bas.cinema.exception.infrastructure;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

@Getter
public class RateLimitExceededException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int limit;
    private final int retryAfterSeconds;

    public RateLimitExceededException(int limit, int retryAfterSeconds) {
        super("Too many requests, please try again later", "RATE_LIMIT_EXCEEDED", HttpStatus.TOO_MANY_REQUESTS,
                String.format("Limit of %d requests per %d seconds exceeded", limit, retryAfterSeconds));
        this.limit = limit;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
