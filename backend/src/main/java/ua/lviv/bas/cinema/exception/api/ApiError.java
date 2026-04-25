package ua.lviv.bas.cinema.exception.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiError {

    private HttpStatus status;
    private int statusCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private final LocalDateTime timestamp = LocalDateTime.now();

    @Setter
    private String message;

    @Setter
    private String debugMessage;

    @Setter
    private String path;

    private List<ApiSubError> subErrors;

    public ApiError(HttpStatus status) {
        this();
        this.status = status;
        this.statusCode = status.value();
    }

    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
    public void addValidationErrors(List<FieldError> fieldErrors) {
        fieldErrors.forEach(this::addValidationError);
    }

    @SuppressWarnings("unused")
    public void addValidationError(List<ObjectError> globalErrors) {
        globalErrors.forEach(this::addValidationError);
    }

    public void addValidationError(String object, String message) {
        addSubError(new ApiSubError.ValidationError(object, message));
    }

    public void addValidationError(String object, String field, Object rejectedValue, String message) {
        addSubError(new ApiSubError.ValidationError(object, field, rejectedValue, message));
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
}