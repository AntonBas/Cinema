package ua.lviv.bas.cinema.dto.booking.response;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookingTempHoldResponse {
	private Long bookingId;
	private LocalDateTime expiresAt;
	private Integer remainingSeconds;
}
