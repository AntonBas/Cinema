package ua.lviv.bas.cinema.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
		String errorMessage = ex.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage()).findFirst()
				.orElse("Invalid request");
		log.warn("Validation failed: {}", errorMessage);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", errorMessage));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleOtherExceptions(Exception ex) {
		log.error("Unexpected error: ", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(Map.of("success", false, "message", "An unexpected error occurred"));
	}

	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleUserNotFound(UserNotFoundException ex) {
		log.warn("User not found: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", ex.getMessage()));
	}

	@ExceptionHandler(EmailAlreadyExistsException.class)
	public ResponseEntity<Map<String, Object>> handleEmailAlreadyExists(EmailAlreadyExistsException ex) {
		log.warn("Email already exists: {}", ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", ex.getMessage()));
	}

	@ExceptionHandler({ GenreNotFoundException.class, PersonNotFoundException.class, MovieNotFoundException.class,
			CinemaHallNotFoundException.class, SeatNotFoundException.class, SessionNotFoundException.class })
	public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
		log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", ex.getMessage()));
	}

	@ExceptionHandler({ DuplicateEntityException.class, ConflictException.class })
	public ResponseEntity<Map<String, Object>> handleConflictExceptions(RuntimeException ex) {
		log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("success", false, "message", ex.getMessage()));
	}

	@ExceptionHandler({ TokenExpiredException.class, TokenAlreadyConfirmedException.class,
			InvalidTokenException.class })
	public ResponseEntity<Map<String, Object>> handleTokenExceptions(RuntimeException ex) {
		log.warn("{}: {}", ex.getClass().getSimpleName(), ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "message", ex.getMessage()));
	}
}
