package ua.lviv.bas.cinema.dto.payment.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment information")
public class PaymentResponse {

	@Schema(description = "Payment ID", example = "123")
	private Long id;

	@Schema(description = "Booking ID", example = "456")
	private Long bookingId;

	@Schema(description = "Booking number", example = "BK-20240115-00123")
	private String bookingNumber;

	@Schema(description = "User email", example = "user@example.com")
	private String userEmail;

	@Schema(description = "Movie title", example = "Inception")
	private String movieTitle;

	@Schema(description = "Session time", example = "2024-01-15T18:30:00")
	private LocalDateTime sessionTime;

	@Schema(description = "Hall name", example = "Hall A")
	private String hallName;

	@Schema(description = "Payment amount", example = "1000.00")
	private BigDecimal amount;

	@Schema(description = "Final amount after discounts", example = "950.00")
	private BigDecimal finalAmount;

	@Schema(description = "Payment status", example = "PENDING")
	private PaymentStatus status;

	@Schema(description = "LiqPay order ID", example = "ORDER_ABC123")
	private String liqpayOrderId;

	@Schema(description = "LiqPay payment ID", example = "lp_123456789")
	private String liqpayPaymentId;

	@Schema(description = "Payment time", example = "2024-01-15T14:35:00")
	private LocalDateTime paymentTime;

	@Schema(description = "Error code from LiqPay", example = "error_validation")
	private String errorCode;

	@Schema(description = "Error description from LiqPay", example = "Invalid signature")
	private String errorDescription;

	@Schema(description = "Masked card number", example = "****4832")
	private String senderCardMask;

	@Schema(description = "LiqPay action type", example = "pay")
	private String actionType;

	@Schema(description = "Whether payment can be refunded via API", example = "true")
	private Boolean refundableViaApi;

	@Schema(description = "Created at", example = "2024-01-15T14:30:00")
	private LocalDateTime createdAt;

	@Schema(description = "Updated at", example = "2024-01-15T14:35:00")
	private LocalDateTime updatedAt;
}