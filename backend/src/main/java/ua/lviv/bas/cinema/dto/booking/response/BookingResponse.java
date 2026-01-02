package ua.lviv.bas.cinema.dto.booking.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed booking information")
public class BookingResponse {

	@Schema(description = "Booking ID", example = "123")
	private Long id;

	@Schema(description = "Booking reference number", example = "BK-20240115-00123")
	private String bookingNumber;

	@Schema(description = "Booking status", example = "PENDING")
	private BookingStatus status;

	@Schema(description = "Total price", example = "450.00")
	private BigDecimal totalPrice;

	@Schema(description = "Booking creation time", example = "2024-01-15T14:30:00")
	private LocalDateTime createdAt;

	@Schema(description = "Booking expiration time", example = "2024-01-15T14:45:00")
	private LocalDateTime expiresAt;

	@Schema(description = "Session information")
	private SessionInfo session;

	@Schema(description = "Booked seats")
	private List<BookedSeatInfo> bookedSeats;

	@Schema(description = "Payment information (if exists)")
	private PaymentInfo payment;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Session information for booking")
	public static class SessionInfo {
		@Schema(description = "Session ID", example = "1")
		private Long id;

		@Schema(description = "Movie title", example = "Inception")
		private String movieTitle;

		@Schema(description = "Session start time", example = "2024-01-15T18:30:00")
		private LocalDateTime startTime;

		@Schema(description = "Hall name", example = "Hall A - Dolby Atmos")
		private String hallName;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Booked seat information")
	public static class BookedSeatInfo {
		@Schema(description = "Booked seat ID", example = "456")
		private Long id;

		@Schema(description = "Seat ID", example = "45")
		private Long seatId;

		@Schema(description = "Row number", example = "5")
		private Integer row;

		@Schema(description = "Seat number", example = "12")
		private Integer seatNumber;

		@Schema(description = "Ticket type name", example = "Adult")
		private String ticketType;

		@Schema(description = "Price for this seat", example = "225.00")
		private BigDecimal price;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	@Schema(description = "Payment information")
	public static class PaymentInfo {
		@Schema(description = "Payment ID", example = "789")
		private Long id;

		@Schema(description = "Payment status", example = "COMPLETED")
		private String status;

		@Schema(description = "Amount paid", example = "450.00")
		private BigDecimal amount;
	}
}