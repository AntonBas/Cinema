package ua.lviv.bas.cinema.dto.bonus.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Bonus transaction information")
public class BonusTransactionResponse {

	@Schema(description = "Transaction ID", example = "1")
	private Long id;

	@Schema(description = "Transaction type", example = "PAYMENT_ACCRUAL")
	private String type;

	@Schema(description = "Transaction type display name", example = "Payment Accrual")
	private String typeDisplay;

	@Schema(description = "Points change with sign (+ for credit, - for debit)", example = "+150")
	private String pointsChange;

	@Schema(description = "Transaction creation timestamp", example = "2025-01-17T14:30:00")
	private LocalDateTime createdAt;

	@Schema(description = "Balance after this transaction", example = "350")
	private Integer newBalance;

	@Schema(description = "Booking details if applicable", nullable = true)
	private BookingDetails bookingDetails;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class BookingDetails {
		@Schema(description = "Movie title", example = "Interstellar")
		private String movieTitle;

		@Schema(description = "Booking reference number", example = "BK-12345")
		private String bookingReference;

		@Schema(description = "Cinema hall", example = "Hall 1")
		private String cinemaHall;

		@Schema(description = "Session date and time", example = "2025-01-25T19:30:00")
		private LocalDateTime sessionDateTime;
	}
}