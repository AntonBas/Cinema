package ua.lviv.bas.cinema.service.booking.refund;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;

@Service
@RequiredArgsConstructor
public class RefundValidationService {
	private final RefundRules refundRules;

	public String validateRefund(Ticket ticket) {
		if (ticket.getStatus() != TicketStatus.ACTIVE) {
			return "Ticket is not active. Current status: " + ticket.getStatus();
		}

		LocalDateTime sessionTime = ticket.getBooking().getSession().getStartTime();
		if (!refundRules.isRefundable(sessionTime)) {
			return "Refund is not available for this session";
		}

		if (ticket.getRefund() != null) {
			return "Ticket has already been refunded";
		}

		if (sessionTime.isBefore(LocalDateTime.now())) {
			return "Session has already started or finished";
		}

		return null;
	}
}