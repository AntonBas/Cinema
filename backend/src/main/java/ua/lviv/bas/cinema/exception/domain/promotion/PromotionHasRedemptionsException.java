package ua.lviv.bas.cinema.exception.domain.promotion;

import ua.lviv.bas.cinema.exception.core.ConflictException;

public class PromotionHasRedemptionsException extends ConflictException {
	private static final long serialVersionUID = 1L;

	public PromotionHasRedemptionsException(Long promotionId, int redemptionCount) {
		super(String.format("Cannot delete promotion with ID: %s because it has %d user redemption(s)", promotionId,
				redemptionCount), "PROMOTION_HAS_REDEMPTIONS",
				String.format("Promotion ID: %s has %d user redemption(s)", promotionId, redemptionCount));
	}
}