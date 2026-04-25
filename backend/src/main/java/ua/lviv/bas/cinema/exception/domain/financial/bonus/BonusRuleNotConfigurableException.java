package ua.lviv.bas.cinema.exception.domain.financial.bonus;

import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class BonusRuleNotConfigurableException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BonusRuleNotConfigurableException(BonusTransactionType type) {
        super(String.format("Bonus rule type '%s' is not configurable", type), "BONUS_RULE_NOT_CONFIGURABLE",
                String.format("Type %s cannot be configured via admin panel", type));
    }
}