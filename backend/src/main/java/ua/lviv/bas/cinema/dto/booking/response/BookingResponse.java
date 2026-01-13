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
@Schema(description = "Booking information")
public class BookingResponse {

	@Schema(description = "Booking ID", example = "123")
	private Long id;

	@Schema(description = "Booking number", example = "BK-20240115-00123")
	private String bookingNumber;

	@Schema(description = "Booking status", example = "PENDING")
	private BookingStatus status;

	@Schema(description = "Session ID", example = "789")
	private Long sessionId;

	@Schema(description = "Session start time", example = "2024-01-15T18:30:00")
	private LocalDateTime sessionTime;

	@Schema(description = "Movie title", example = "Inception")
	private String movieTitle;

	@Schema(description = "Hall name", example = "Hall A")
	private String hallName;

	@Schema(description = "Total price", example = "1000.00")
	private BigDecimal totalPrice;

	@Schema(description = "Bonus points used", example = "100")
	private Integer bonusPointsUsed;

	@Schema(description = "Bonus discount amount", example = "50.00")
	private BigDecimal bonusDiscountAmount;

	@Schema(description = "Final price", example = "950.00")
	private BigDecimal finalPrice;

	@Schema(description = "LiqPay order ID", example = "ORDER_ABC123")
	private String liqpayOrderId;

	@Schema(description = "Booking expires at", example = "2024-01-15T14:50:00")
	private LocalDateTime expiresAt;

	@Schema(description = "Created at", example = "2024-01-15T14:30:00")
	private LocalDateTime createdAt;

	@Schema(description = "List of booked seats")
	private List<BookedSeatInfo> bookedSeats;

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
		private String ticketTypeName;

		@Schema(description = "Seat price", example = "250.00")
		private BigDecimal seatPrice;
	}
}