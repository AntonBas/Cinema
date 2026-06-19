package ua.lviv.bas.cinema.exception.domain.financial.promotion;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class AlreadyClaimedException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public AlreadyClaimedException(String promotionTitle) {
        super(String.format("Promotion '%s' has already been claimed", promotionTitle), "PROMOTION_ALREADY_CLAIMED",
                String.format("User already claimed promotion '%s'", promotionTitle));
    }
}