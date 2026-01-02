package ua.lviv.bas.cinema.dto.payment.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.PaymentMethod;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment information")
public class PaymentResponse {

	@Schema(description = "Payment ID", example = "789")
	private Long id;

	@Schema(description = "Booking ID", example = "123")
	private Long bookingId;

	@Schema(description = "Total amount", example = "450.00")
	private BigDecimal amount;

	@Schema(description = "Bonus points used", example = "50")
	private Integer bonusPointsUsed;

	@Schema(description = "Final amount to pay", example = "400.00")
	private BigDecimal finalAmount;

	@Schema(description = "Payment method", example = "CARD")
	private PaymentMethod paymentMethod;

	@Schema(description = "Payment status", example = "PENDING")
	private PaymentStatus status;

	@Schema(description = "Payment URL (for online payments)", example = "https://payment.example.com/pay/123")
	private String paymentUrl;

	@Schema(description = "Creation time", example = "2024-01-15T14:31:00")
	private LocalDateTime createdAt;

	@Schema(description = "Payment time", example = "2024-01-15T14:32:00")
	private LocalDateTime paymentTime;
}