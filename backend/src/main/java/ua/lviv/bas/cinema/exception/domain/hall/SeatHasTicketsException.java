package ua.lviv.bas.cinema.exception.domain.hall;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;
import java.util.List;

public class SeatHasTicketsException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SeatHasTicketsException(List<Long> seatIds) {
        super(String.format("Cannot delete or reposition %d seat(s) that already have booked tickets",
                        seatIds.size()),
                "SEAT_HAS_TICKETS", HttpStatus.CONFLICT,
                "Seats blocked by existing tickets: " + seatIds);
    }
}
