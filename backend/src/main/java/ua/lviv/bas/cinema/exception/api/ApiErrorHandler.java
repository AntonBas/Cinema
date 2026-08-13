package ua.lviv.bas.cinema.exception.api;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import jakarta.annotation.Nonnull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ua.lviv.bas.cinema.exception.core.BusinessException;
import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.util.Objects;
import java.util.Optional;

import static org.springframework.http.HttpStatus.*;

@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
@SuppressWarnings("unused")
public class ApiErrorHandler extends ResponseEntityExceptionHandler {

    @Override
    @Nonnull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@Nonnull HttpMessageNotReadableException ex,
                                                                  @Nonnull HttpHeaders headers, @Nonnull HttpStatusCode status, @Nonnull WebRequest request) {
        String error = "Malformed JSON request";
        log.warn("Malformed JSON request: {}", ex.getMessage());
        return buildResponseEntity(new ApiError(BAD_REQUEST, error, ex), request);
    }

    @Override
    @Nonnull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@Nonnull MethodArgumentNotValidException ex,
                                                                  @Nonnull HttpHeaders headers, @Nonnull HttpStatusCode status, @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage("Validation error");
        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());

        log.warn("Validation error: {} errors detected", ex.getBindingResult().getFieldErrors().size());

        return buildResponseEntity(apiError, request);
    }

    @Override
    @Nonnull
    protected ResponseEntity<Object> handleNoHandlerFoundException(@Nonnull NoHandlerFoundException ex,
                                                                   @Nonnull HttpHeaders headers, @Nonnull HttpStatusCode status, @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(
                String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL()));
        apiError.setDebugMessage(ex.getMessage());

        log.warn("No handler found: {} {}", ex.getHttpMethod(), ex.getRequestURL());

        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(@Nonnull EntityNotFoundException ex,
                                                          @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());

        log.warn("Entity not found: {}", ex.getMessage());

        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<Object> handleBusinessException(@Nonnull BusinessException ex,
                                                             @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(ex.getStatus());
        apiError.setMessage(ex.getMessage());
        apiError.setDebugMessage(ex.getDebugMessage());

        log.warn("Business exception [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage());

        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<Object> handleNotFoundException(@Nonnull NotFoundException ex,
                                                             @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        apiError.setDebugMessage(ex.getDebugMessage());

        log.warn("Not found: {}", ex.getMessage());

        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolation(@Nonnull DataIntegrityViolationException ex,
                                                                  @Nonnull WebRequest request) {
        if (ex.getCause() instanceof ConstraintViolationException) {
            ApiError apiError = new ApiError(CONFLICT, "Database constraint violation", ex.getCause());
            log.warn("Database constraint violation: {}", ex.getMessage());
            return buildResponseEntity(apiError, request);
        }

        ApiError apiError = new ApiError(INTERNAL_SERVER_ERROR, "Database error", ex);
        log.error("Database error: ", ex);
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatch(@Nonnull MethodArgumentTypeMismatchException ex,
                                                                      @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);

        String requiredType = Optional.ofNullable(ex.getRequiredType()).map(Class::getSimpleName).orElse("unknown");

        apiError.setMessage(String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                ex.getName(), ex.getValue(), requiredType));
        apiError.setDebugMessage(ex.getMessage());

        log.warn("Type mismatch for parameter '{}': {}", ex.getName(), ex.getMessage());

        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(@Nonnull ConstraintViolationException ex,
                                                               @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage("Validation error");

        ex.getConstraintViolations().forEach(cv -> apiError.addValidationError(cv.getRootBeanClass().getSimpleName(),
                cv.getPropertyPath().toString(), cv.getInvalidValue(), cv.getMessage()));

        log.warn("Constraint violation: {} violations", ex.getConstraintViolations().size());

        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    protected ResponseEntity<Object> handleBadCredentials(@Nonnull BadCredentialsException ex,
                                                          @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(UNAUTHORIZED, "Invalid email or password");
        log.warn("Authentication failed: {}", ex.getMessage());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(DisabledException.class)
    protected ResponseEntity<Object> handleDisabled(@Nonnull DisabledException ex, @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(UNAUTHORIZED, "Account is disabled");
        log.warn("Disabled account attempt: {}", ex.getMessage());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(LockedException.class)
    protected ResponseEntity<Object> handleLocked(@Nonnull LockedException ex, @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(UNAUTHORIZED, "Account is locked");
        log.warn("Locked account attempt: {}", ex.getMessage());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDenied(@Nonnull AccessDeniedException ex,
                                                        @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(FORBIDDEN, "Access denied");
        log.warn("Access denied: {}", ex.getMessage());
        return buildResponseEntity(apiError, request);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleAllExceptions(@Nonnull Exception ex, @Nonnull WebRequest request) {
        ApiError apiError = new ApiError(INTERNAL_SERVER_ERROR, "Unexpected error occurred", ex);
        log.error("Unexpected error: ", ex);
        return buildResponseEntity(apiError, request);
    }

    @Nonnull
    private ResponseEntity<Object> buildResponseEntity(@Nonnull ApiError apiError, @Nonnull WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            apiError.setPath(servletWebRequest.getRequest().getRequestURI());
        } else {
            apiError.setPath("unknown");
        }
        return new ResponseEntity<>(apiError,
                Objects.requireNonNull(apiError.getStatus(), "ApiError status must not be null"));
    }
}