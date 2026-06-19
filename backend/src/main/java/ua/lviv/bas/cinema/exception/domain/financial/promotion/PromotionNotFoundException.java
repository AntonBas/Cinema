package ua.lviv.bas.cinema.exception.domain.financial.promotion;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class PromotionNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public PromotionNotFoundException(Long promotionId) {
        super("Promotion not found", "PROMOTION_NOT_FOUND",
                String.format("Promotion ID: %d not found in database", promotionId));
    }
}