package ua.lviv.bas.cinema.dto.booking.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.booking.BookingStatus;

public record BookingResponse(@Schema(description = "Booking ID", example = "123") Long id,

		@Schema(description = "Booking number", example = "BK-20240115-00123") String bookingNumber,

		@Schema(description = "Booking status", example = "PENDING") BookingStatus status,

		@Schema(description = "Session ID", example = "789") Long sessionId,

		@Schema(description = "Session start time", example = "2024-01-15T18:30:00") LocalDateTime sessionTime,

		@Schema(description = "Movie title", example = "Inception") String movieTitle,

		@Schema(description = "Hall name", example = "Hall A") String hallName,

		@Schema(description = "Total price", example = "1000.00") BigDecimal totalPrice,

		@Schema(description = "Bonus points used", example = "100") Integer bonusPointsUsed,

		@Schema(description = "Bonus discount amount", example = "50.00") BigDecimal bonusDiscountAmount,

		@Schema(description = "Final price", example = "950.00") BigDecimal finalPrice,

		@Schema(description = "LiqPay order ID", example = "ORDER_ABC123") String liqpayOrderId,

		@Schema(description = "Booking expires at", example = "2024-01-15T14:50:00") LocalDateTime expiresAt,

		@Schema(description = "Created at", example = "2024-01-15T14:30:00") LocalDateTime createdAt,

		@Schema(description = "List of seat reservations") List<SeatReservationInfo> seatReservations) {
	public record SeatReservationInfo(@Schema(description = "Seat reservation ID", example = "456") Long id,

			@Schema(description = "Seat ID", example = "45") Long seatId,

			@Schema(description = "Row number", example = "5") Integer row,

			@Schema(description = "Seat number", example = "12") Integer seatNumber,

			@Schema(description = "Ticket type name", example = "Adult") String ticketTypeName,

			@Schema(description = "Seat price", example = "250.00") BigDecimal seatPrice) {
	}
}