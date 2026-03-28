package ua.lviv.bas.cinema.exception.domain.financial.bonus;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class BonusCardNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public BonusCardNotFoundException(Long userId) {
		super(String.format("Bonus card not found for user ID: %d", userId), "BONUS_CARD_NOT_FOUND",
				String.format("BonusCard entity for user id %d does not exist", userId));
	}
}