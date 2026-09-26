package ua.lviv.bas.cinema.booking.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Booking details for the admin panel")
public record AdminBookingDetailsResponse(
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

        @Schema(description = "Booking expires at, as UTC instant", example = "2026-01-15T14:50:00Z")
        Instant expiresAt,

        @Schema(description = "Customer user ID", example = "42")
        Long userId,

        @Schema(description = "Customer email", example = "john.doe@example.com")
        String userEmail,

        @Schema(description = "Customer first name", example = "John")
        String userFirstName,

        @Schema(description = "Customer last name", example = "Doe")
        String userLastName,

        @Schema(description = "Session ID", example = "789")
        Long sessionId,

        @Schema(description = "Movie title", example = "Inception")
        String movieTitle,

        @Schema(description = "Hall name", example = "Hall A")
        String hallName,

        @Schema(description = "Session start time", example = "2026-01-15T18:30:00")
        LocalDateTime sessionTime,

        @Schema(description = "Total price", example = "1000.00")
        BigDecimal totalPrice,

        @Schema(description = "Bonus points used", example = "100")
        Integer bonusPointsUsed,

        @Schema(description = "Bonus discount amount", example = "50.00")
        BigDecimal bonusDiscountAmount,

        @Schema(description = "Final price", example = "950.00")
        BigDecimal finalPrice,

        @Schema(description = "Reserved seats")
        List<BookingResponse.SeatReservationInfo> seatReservations,

        @Schema(description = "Issued tickets")
        List<TicketInfo> tickets,

        @Schema(description = "Payment, null when no payment was started")
        PaymentInfo payment
) {
    @Schema(description = "Ticket issued for the booking")
    public record TicketInfo(
            @Schema(description = "Ticket ID", example = "501")
            Long id,

            @Schema(description = "Ticket code", example = "TKT-3F2A9C1B7D4E")
            String ticketCode,

            @Schema(description = "Ticket status", example = "ACTIVE")
            TicketStatus status,

            @Schema(description = "Ticket type name", example = "Adult")
            String ticketType,

            @Schema(description = "Row number", example = "5")
            Integer row,

            @Schema(description = "Seat number", example = "12")
            Integer seatNumber,

            @Schema(description = "Ticket price", example = "250.00")
            BigDecimal price
    ) {
    }

    @Schema(description = "Payment attempt for the booking")
    public record PaymentInfo(
            @Schema(description = "Payment ID", example = "77")
            Long id,

            @Schema(description = "Payment status", example = "SUCCESS")
            PaymentStatus status,

            @Schema(description = "Charged amount", example = "950.00")
            BigDecimal amount,

            @Schema(description = "Payment time, as UTC instant", example = "2026-01-15T14:35:00Z")
            Instant paymentTime,

            @Schema(description = "LiqPay order ID", example = "ORD_ABC123")
            String liqpayOrderId,

            @Schema(description = "Masked card number", example = "4731****1234")
            String cardMask,

            @Schema(description = "LiqPay error code", example = "err_payment")
            String errorCode,

            @Schema(description = "LiqPay error description", example = "Insufficient funds")
            String errorDescription
    ) {
    }
}
