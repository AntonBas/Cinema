package ua.lviv.bas.cinema.dto.booking.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Schema(description = "Summary of a booking for lists")
public class BookingSummaryResponse {

	@Schema(description = "Booking ID", example = "123")
	private Long id;

	@Schema(description = "Booking reference number", example = "BK-20240115-00123")
	private String bookingNumber;

	@Schema(description = "Booking status", example = "CONFIRMED")
	private BookingStatus status;

	@Schema(description = "Movie title", example = "Inception")
	private String movieTitle;

	@Schema(description = "Session time", example = "2024-01-15T18:30:00")
	private LocalDateTime sessionTime;

	@Schema(description = "Hall name", example = "Hall A - Dolby Atmos")
	private String hallName;

	@Schema(description = "Number of seats", example = "2")
	private Integer seatsCount;

	@Schema(description = "Total price", example = "360.00")
	private BigDecimal totalPrice;

	@Schema(description = "Whether booking can be cancelled", example = "true")
	private Boolean canCancel;
}