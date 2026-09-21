package ua.lviv.bas.cinema.exception.domain.hall;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class DuplicateSeatPositionException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public DuplicateSeatPositionException(int row, int number) {
        super(String.format("Multiple seats target the same position: row %d, seat %d", row, number),
                "DUPLICATE_SEAT_POSITION", HttpStatus.BAD_REQUEST,
                String.format("Duplicate seat position row=%d, number=%d", row, number));
    }
}
