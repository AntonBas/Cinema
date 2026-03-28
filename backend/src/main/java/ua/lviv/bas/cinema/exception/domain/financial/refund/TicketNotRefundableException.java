package ua.lviv.bas.cinema.exception.domain.financial.refund;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class TicketNotRefundableException extends ValidationException {

	private static final long serialVersionUID = 1L;

	public TicketNotRefundableException(String reason) {
		super(String.format("Ticket is not refundable: %s", reason), "TICKET_NOT_REFUNDABLE",
				String.format("Refund validation failed. Reason: %s", reason));
	}
}