package ua.lviv.bas.cinema.dto.shared;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	private boolean success;
	private String message;
	private T data;
	private HttpStatus status;
	private LocalDateTime timestamp;

	public static <T> ApiResponse<T> success(T data) {
		return ApiResponse.<T>builder().success(true).message("Success").data(data).status(HttpStatus.OK)
				.timestamp(LocalDateTime.now()).build();
	}

	public static <T> ApiResponse<T> success(T data, String message) {
		return ApiResponse.<T>builder().success(true).message(message).data(data).status(HttpStatus.OK)
				.timestamp(LocalDateTime.now()).build();
	}

	public static <T> ApiResponse<T> created(T data) {
		return ApiResponse.<T>builder().success(true).message("Resource created successfully").data(data)
				.status(HttpStatus.CREATED).timestamp(LocalDateTime.now()).build();
	}

	public static <T> ApiResponse<T> error(String message, HttpStatus status) {
		return ApiResponse.<T>builder().success(false).message(message).status(status).timestamp(LocalDateTime.now())
				.build();
	}
}
