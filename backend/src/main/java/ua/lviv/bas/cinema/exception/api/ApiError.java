package ua.lviv.bas.cinema.exception.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

	private HttpStatus status;
	private int statusCode;

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
	private LocalDateTime timestamp;

	private String message;
	private String debugMessage;
	private String path;

	private List<ApiSubError> subErrors;

	private ApiError() {
		timestamp = LocalDateTime.now();
	}

	public ApiError(HttpStatus status) {
		this();
		this.status = status;
		this.statusCode = status.value();
	}

	public ApiError(HttpStatus status, Throwable ex) {
		this();
		this.status = status;
		this.statusCode = status.value();
		this.message = "Unexpected error";
		this.debugMessage = ex.getLocalizedMessage();
	}

	public ApiError(HttpStatus status, String message, Throwable ex) {
		this();
		this.status = status;
		this.statusCode = status.value();
		this.message = message;
		this.debugMessage = ex.getLocalizedMessage();
	}

	public ApiError(HttpStatus status, String message) {
		this();
		this.status = status;
		this.statusCode = status.value();
		this.message = message;
	}

	public void addValidationErrors(List<FieldError> fieldErrors) {
		fieldErrors.forEach(this::addValidationError);
	}

	public void addValidationError(List<ObjectError> globalErrors) {
		globalErrors.forEach(this::addValidationError);
	}

	public void addValidationError(String object, String message) {
		addSubError(new ApiValidationError(object, message));
	}

	public void addValidationError(String object, String field, Object rejectedValue, String message) {
		addSubError(new ApiValidationError(object, field, rejectedValue, message));
	}

	public void addValidationError(FieldError fieldError) {
		this.addValidationError(fieldError.getObjectName(), fieldError.getField(), fieldError.getRejectedValue(),
				fieldError.getDefaultMessage());
	}

	private void addValidationError(ObjectError objectError) {
		this.addValidationError(objectError.getObjectName(), objectError.getDefaultMessage());
	}

	private void addSubError(ApiSubError subError) {
		if (subErrors == null) {
			subErrors = new ArrayList<>();
		}
		subErrors.add(subError);
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Data
	@EqualsAndHashCode(callSuper = false)
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public static class ApiValidationError extends ApiSubError {
		private String object;
		private String field;
		private Object rejectedValue;
		private String message;

		ApiValidationError(String object, String message) {
			this.object = object;
			this.message = message;
		}

		public ApiValidationError(String object, String field, Object rejectedValue, String message) {
			this.object = object;
			this.field = field;
			this.rejectedValue = rejectedValue;
			this.message = message;
		}
	}
}