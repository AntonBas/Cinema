package ua.lviv.bas.cinema.booking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Booking row in the admin bookings list")
public record AdminBookingListResponse(
        @Schema(description = "Booking ID", example = "123")
        Long id,

        @Schema(description = "Public identifier of the booking", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6")
        UUID publicId,

        @Schema(description = "Booking number", example = "BK-2026-00123")
        String bookingNumber,

        @Schema(description = "Booking status", example = "CONFIRMED")
        BookingStatus status,

        @Schema(description = "Booking creation time, as UTC instant", example = "2026-01-15T14:30:00Z")
        Instant createdDate,

        @Schema(description = "Customer user ID", example = "42")
        Long userId,

        @Schema(description = "Customer email", example = "john.doe@example.com")
        String userEmail,

        @Schema(description = "Movie title", example = "Inception")
        String movieTitle,

        @Schema(description = "Hall name", example = "Hall A")
        String hallName,

        @Schema(description = "Session start time", example = "2026-01-15T18:30:00")
        LocalDateTime sessionTime,

        @Schema(description = "Final price", example = "950.00")
        BigDecimal finalPrice,

        @Schema(description = "Payment status, null when no payment was started", example = "SUCCESS")
        PaymentStatus paymentStatus
) {
}
