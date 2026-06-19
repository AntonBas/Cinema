package ua.lviv.bas.cinema.exception.domain.hall;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class HallLayoutHasTicketsException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public HallLayoutHasTicketsException() {
        super("Cannot update hall layout because seats have booked tickets",
                "HALL_LAYOUT_HAS_TICKETS", HttpStatus.CONFLICT,
                "Hall has booked tickets, layout update blocked");
    }
}