package ua.lviv.bas.cinema.exception.domain.financial.promotion;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;
import java.time.LocalDate;

public class InvalidPromotionDateRangeException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidPromotionDateRangeException(LocalDate startDate, LocalDate endDate) {
        this(String.format("End date %s must not be before start date %s", endDate, startDate),
                String.format("startDate=%s, endDate=%s", startDate, endDate));
    }

    private InvalidPromotionDateRangeException(String message, String debugMessage) {
        super(message, "INVALID_PROMOTION_DATE_RANGE", HttpStatus.BAD_REQUEST, debugMessage);
    }

    public static InvalidPromotionDateRangeException inPast(String field, LocalDate date) {
        return new InvalidPromotionDateRangeException(String.format("%s %s must not be in the past", field, date),
                String.format("%s=%s", field, date));
    }
}
