package ua.lviv.bas.cinema.exception.domain.financial.promotion;

import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;

import java.io.Serial;

public class PromotionAlreadyExistsException extends DuplicateEntityException {

    @Serial
    private static final long serialVersionUID = 1L;

    private PromotionAlreadyExistsException(String message) {
        super(message);
    }

    public static PromotionAlreadyExistsException forTitle(String title) {
        return new PromotionAlreadyExistsException(
                String.format("Promotion with title '%s' already exists", title));
    }
}