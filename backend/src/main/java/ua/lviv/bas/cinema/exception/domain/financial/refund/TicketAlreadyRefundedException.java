package ua.lviv.bas.cinema.exception.domain.financial.refund;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class TicketAlreadyRefundedException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public TicketAlreadyRefundedException(String ticketCode) {
        super("Ticket has already been refunded", "TICKET_ALREADY_REFUNDED",
                String.format("Ticket %s has already been refunded", ticketCode));
    }
}
