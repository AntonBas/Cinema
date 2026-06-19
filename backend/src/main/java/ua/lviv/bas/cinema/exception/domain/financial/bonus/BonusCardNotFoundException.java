package ua.lviv.bas.cinema.exception.domain.financial.bonus;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class BonusCardNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BonusCardNotFoundException(Long userId) {
        super("Bonus card not found", "BONUS_CARD_NOT_FOUND",
                String.format("BonusCard entity for user id %d does not exist", userId));
    }
}