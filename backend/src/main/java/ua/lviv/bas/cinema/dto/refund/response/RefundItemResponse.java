package ua.lviv.bas.cinema.dto.refund.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Refund item (single ticket)")
public class RefundItemResponse {

	@Schema(description = "Refund item ID", example = "1")
	private Long id;

	@Schema(description = "Ticket ID", example = "123")
	private Long ticketId;

	@Schema(description = "Ticket code", example = "TK2024000123")
	private String ticketCode;

	@Schema(description = "Ticket price", example = "200.00")
	private BigDecimal ticketPrice;

	@Schema(description = "Refund percentage", example = "90.00")
	private BigDecimal refundPercentage;

	@Schema(description = "Refund amount", example = "180.00")
	private BigDecimal refundAmount;

	@Schema(description = "Bonus points to deduct", example = "90")
	private Integer bonusPointsToDeduct;

	@Schema(description = "Status", example = "COMPLETED")
	private String status;

	@JsonFormat(pattern = "dd.MM.yyyy HH:mm")
	@Schema(description = "Creation timestamp", example = "15.12.2024 10:15:00")
	private LocalDateTime createdAt;
}