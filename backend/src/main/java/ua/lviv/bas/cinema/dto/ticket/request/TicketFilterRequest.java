package ua.lviv.bas.cinema.dto.ticket.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO for filtering tickets")
public class TicketFilterRequest {

	@Schema(description = "User ID", example = "1")
	@Positive
	private Long userId;

	@Schema(description = "Ticket status", example = "ACTIVE")
	private TicketStatus status;

	@Schema(description = "Purchase date from", example = "2024-01-01")
	@PastOrPresent
	private LocalDate purchaseDateFrom;

	@Schema(description = "Purchase date to", example = "2024-12-31")
	@FutureOrPresent
	private LocalDate purchaseDateTo;

	@Schema(description = "Movie session date from", example = "2024-01-01")
	private LocalDate sessionDateFrom;

	@Schema(description = "Movie session date to", example = "2024-12-31")
	private LocalDate sessionDateTo;

	@Schema(description = "Movie ID", example = "5")
	@Positive
	private Long movieId;

	@AssertTrue(message = "sessionDateFrom must be before or equal to sessionDateTo")
	public boolean isSessionDateRangeValid() {
		if (sessionDateFrom == null || sessionDateTo == null) {
			return true;
		}
		return !sessionDateFrom.isAfter(sessionDateTo);
	}

	@AssertTrue(message = "purchaseDateFrom must be before or equal to purchaseDateTo")
	public boolean isPurchaseDateRangeValid() {
		if (purchaseDateFrom == null || purchaseDateTo == null) {
			return true;
		}
		return !purchaseDateFrom.isAfter(purchaseDateTo);
	}
}