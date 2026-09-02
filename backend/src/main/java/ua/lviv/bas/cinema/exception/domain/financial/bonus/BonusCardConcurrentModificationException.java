package ua.lviv.bas.cinema.exception.domain.financial.bonus;

import jakarta.annotation.Nullable;
import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class BonusCardConcurrentModificationException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BonusCardConcurrentModificationException(@Nullable Throwable cause) {
        super("Bonus card was modified concurrently, please retry", "BONUS_CARD_CONCURRENT_MODIFICATION",
                "Optimistic lock conflict persisted after retrying", cause);
    }
}
