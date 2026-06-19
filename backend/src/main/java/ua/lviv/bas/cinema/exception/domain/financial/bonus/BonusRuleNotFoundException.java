package ua.lviv.bas.cinema.exception.domain.financial.bonus;

import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class BonusRuleNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public BonusRuleNotFoundException(BonusTransactionType type) {
        super(String.format("Bonus rule '%s' is not configured", type), "BONUS_RULE_NOT_FOUND",
                String.format("BonusRules for type %s does not exist or is inactive", type));
    }
}