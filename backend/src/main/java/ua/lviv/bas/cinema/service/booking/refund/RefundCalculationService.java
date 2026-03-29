package ua.lviv.bas.cinema.service.booking.refund;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;

@Service
@RequiredArgsConstructor
public class RefundCalculationService {

	public BigDecimal calculateRefundAmount(BigDecimal price, BigDecimal percentage) {
		return price.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
	}

	public Integer calculateBonusRefund(Integer bonusPointsUsed, BigDecimal percentage) {
		if (bonusPointsUsed == null || bonusPointsUsed == 0) {
			return 0;
		}
		return (int) (bonusPointsUsed * percentage.doubleValue() / 100);
	}

	public String formatRemainingTime(LocalDateTime sessionTime) {
		long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), sessionTime);
		long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), sessionTime) % 60;

		if (hours > 0 && minutes > 0) {
			return String.format("%d hours %d minutes", hours, minutes);
		} else if (hours > 0) {
			return String.format("%d hours", hours);
		} else if (minutes > 0) {
			return String.format("%d minutes", minutes);
		} else {
			return "Less than a minute";
		}
	}

	public RefundPreviewResponse createPreviewResponse(Ticket ticket, RefundRules refundRules) {
		LocalDateTime sessionTime = ticket.getBooking().getSession().getStartTime();
		BigDecimal percentage = refundRules.getRefundPercentage(sessionTime);
		BigDecimal refundAmount = calculateRefundAmount(ticket.getFinalPrice(), percentage);
		BigDecimal feeAmount = ticket.getFinalPrice().subtract(refundAmount);

		String seatInfo = "N/A";
		if (ticket.getBooking().getSeatReservations() != null && !ticket.getBooking().getSeatReservations().isEmpty()) {
			SeatReservation bookedSeat = ticket.getBooking().getSeatReservations().get(0);
			seatInfo = String.format("Row %d, Seat %d", bookedSeat.getSeat().getRow(),
					bookedSeat.getSeat().getNumber());
		}

		return new RefundPreviewResponse(ticket.getId(), ticket.getUniqueCode(),
				ticket.getBooking().getSession().getMovie().getTitle(), sessionTime,
				ticket.getBooking().getSession().getHall().getName(), seatInfo, ticket.getOriginalPrice(),
				ticket.getFinalPrice(), refundAmount, percentage, feeAmount,
				BigDecimal.valueOf(100).subtract(percentage), ticket.getBonusPointsUsed(),
				calculateBonusRefund(ticket.getBonusPointsUsed(), percentage), refundRules.getPolicyName(sessionTime),
				refundRules.getPolicyDescription(sessionTime), true, null, sessionTime.minusMinutes(30),
				formatRemainingTime(sessionTime), ticket.getPurchaseTime().toString(),
				ticket.getTicketType().getDisplayName());
	}
}