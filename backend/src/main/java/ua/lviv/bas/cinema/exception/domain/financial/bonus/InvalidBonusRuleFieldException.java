package ua.lviv.bas.cinema.exception.domain.financial.bonus;

import ua.lviv.bas.cinema.bonus.domain.BonusRuleField;
import ua.lviv.bas.cinema.bonus.domain.BonusTransactionType;
import ua.lviv.bas.cinema.exception.core.ValidationException;

import java.io.Serial;

public class InvalidBonusRuleFieldException extends ValidationException {

    @Serial
    private static final long serialVersionUID = 1L;

    private InvalidBonusRuleFieldException(String message, String errorCode) {
        super(message, errorCode);
    }

    public static InvalidBonusRuleFieldException notApplicable(BonusTransactionType type, BonusRuleField field) {
        return new InvalidBonusRuleFieldException(
                String.format("Field '%s' is not used by bonus rule %s", field.getFieldName(), type),
                "BONUS_RULE_FIELD_NOT_APPLICABLE");
    }

    public static InvalidBonusRuleFieldException required(BonusTransactionType type, BonusRuleField field) {
        return new InvalidBonusRuleFieldException(
                String.format("Field '%s' is required for bonus rule %s", field.getFieldName(), type),
                "BONUS_RULE_FIELD_REQUIRED");
    }
}
