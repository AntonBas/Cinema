package ua.lviv.bas.cinema.exception.domain.financial.promotion;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;
import java.time.LocalDate;

public class InvalidPromotionDateRangeException extends BusinessException {

    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidPromotionDateRangeException(LocalDate startDate, LocalDate endDate) {
        super(String.format("End date %s must not be before start date %s", endDate, startDate),
                "INVALID_PROMOTION_DATE_RANGE", HttpStatus.BAD_REQUEST,
                String.format("startDate=%s, endDate=%s", startDate, endDate));
    }
}
