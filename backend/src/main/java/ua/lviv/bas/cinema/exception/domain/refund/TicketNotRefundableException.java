package ua.lviv.bas.cinema.exception.domain.refund;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class TicketNotRefundableException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public TicketNotRefundableException(String reason) {
		super(String.format("Ticket is not refundable: %s", reason), "TICKET_NOT_REFUNDABLE",
				String.format("Refund validation failed. Reason: %s", reason));
	}

	public TicketNotRefundableException(Long ticketId, String reason) {
		super(String.format("Ticket with ID %d is not refundable: %s", ticketId, reason), "TICKET_NOT_REFUNDABLE",
				String.format("Ticket ID %d cannot be refunded. Reason: %s", ticketId, reason));
	}
}