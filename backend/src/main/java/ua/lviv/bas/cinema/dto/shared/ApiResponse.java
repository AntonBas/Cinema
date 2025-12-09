//package ua.lviv.bas.cinema.dto.shared;
//
//import java.time.LocalDateTime;
//
//import org.springframework.http.HttpStatus;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//
//import io.swagger.v3.oas.annotations.media.Schema;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
//@Schema(description = "Standard API response wrapper for all endpoints")
//public class ApiResponse<T> {
//
//	@Schema(description = "Indicates if the request was successful", example = "true")
//	private boolean success;
//
//	@Schema(description = "Response message", example = "Success")
//	private String message;
//
//	@Schema(description = "Response data payload")
//	private T data;
//
//	@Schema(description = "HTTP status of the response", example = "OK")
//	private HttpStatus status;
//
//	@Schema(description = "Timestamp of the response", example = "2024-01-15T10:30:00", type = "string", format = "date-time")
//	private LocalDateTime timestamp;
//
//	public static <T> ApiResponse<T> success(T data) {
//		return ApiResponse.<T>builder().success(true).message("Success").data(data).status(HttpStatus.OK)
//				.timestamp(LocalDateTime.now()).build();
//	}
//
//	public static <T> ApiResponse<T> success(T data, String message) {
//		return ApiResponse.<T>builder().success(true).message(message).data(data).status(HttpStatus.OK)
//				.timestamp(LocalDateTime.now()).build();
//	}
//
//	public static <T> ApiResponse<T> created(T data) {
//		return ApiResponse.<T>builder().success(true).message("Resource created successfully").data(data)
//				.status(HttpStatus.CREATED).timestamp(LocalDateTime.now()).build();
//	}
//
//	public static <T> ApiResponse<T> error(String message, HttpStatus status) {
//		return ApiResponse.<T>builder().success(false).message(message).status(status).timestamp(LocalDateTime.now())
//				.build();
//	}
//}