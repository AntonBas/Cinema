package ua.lviv.bas.cinema.exception.domain.hall;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class InvalidCoupleRowsConfigurationException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidCoupleRowsConfigurationException(int row, int totalRows) {
        super(String.format("Couple row %d is out of range. Hall has %d rows", row, totalRows),
                "INVALID_COUPLE_ROWS_CONFIGURATION", HttpStatus.BAD_REQUEST,
                String.format("Couple row %d exceeds total rows %d", row, totalRows));
    }
}