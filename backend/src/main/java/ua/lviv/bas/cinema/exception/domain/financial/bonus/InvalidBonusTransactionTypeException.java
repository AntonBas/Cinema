package ua.lviv.bas.cinema.exception.domain.financial.bonus;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class InvalidBonusTransactionTypeException extends ValidationException {
	private static final long serialVersionUID = 1L;

	public InvalidBonusTransactionTypeException(String type) {
		super(String.format("Invalid bonus transaction type: %s", type), "INVALID_BONUS_TRANSACTION_TYPE",
				String.format("Provided type '%s' is not a valid bonus transaction type", type));
	}
}