package ua.lviv.bas.cinema.exception.domain.financial.promotion;

import ua.lviv.bas.cinema.exception.core.ConflictException;

public class AlreadyClaimedException extends ConflictException {
	private static final long serialVersionUID = 1L;

	public AlreadyClaimedException(Long userId, Long promotionId) {
		super(String.format("User with ID: %s has already claimed promotion with ID: %s", userId, promotionId),
				"PROMOTION_ALREADY_CLAIMED",
				String.format("User ID: %s already has promotion ID: %s in user_promotion table", userId, promotionId));
	}

	public AlreadyClaimedException(String userEmail, String promotionTitle) {
		super(String.format("User '%s' has already claimed promotion '%s'", userEmail, promotionTitle),
				"PROMOTION_ALREADY_CLAIMED",
				String.format("User '%s' already claimed promotion '%s'", userEmail, promotionTitle));
	}
}