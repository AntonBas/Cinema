package ua.lviv.bas.cinema.exception.domain.financial.bonus;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class InsufficientPointsException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InsufficientPointsException(Integer available, Integer required) {
        super(String.format("Not enough bonus points. Available: %d, Required: %d", available, required),
                "INSUFFICIENT_BONUS_POINTS", HttpStatus.BAD_REQUEST,
                String.format("User has %d points but needs %d", available, required));
    }
}