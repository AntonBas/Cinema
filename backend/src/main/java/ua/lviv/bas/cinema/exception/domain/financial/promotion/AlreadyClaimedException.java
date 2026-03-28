package ua.lviv.bas.cinema.exception.domain.financial.promotion;

import ua.lviv.bas.cinema.exception.core.ConflictException;

public class AlreadyClaimedException extends ConflictException {
	private static final long serialVersionUID = 1L;

	public AlreadyClaimedException(String userEmail, String promotionTitle) {
		super(String.format("User '%s' has already claimed promotion '%s'", userEmail, promotionTitle),
				"PROMOTION_ALREADY_CLAIMED",
				String.format("User '%s' already claimed promotion '%s'", userEmail, promotionTitle));
	}
}