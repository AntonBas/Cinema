package ua.lviv.bas.cinema.exception.domain.promotion;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class PromotionNotFoundException extends NotFoundException {
	private static final long serialVersionUID = 1L;

	public PromotionNotFoundException(Long promotionId) {
		super(String.format("Promotion not found with id: %s", promotionId), "PROMOTION_NOT_FOUND",
				String.format("Promotion ID: %s not found in database", promotionId));
	}

	public PromotionNotFoundException(String title) {
		super(String.format("Promotion not found with title: %s", title), "PROMOTION_NOT_FOUND",
				String.format("Promotion with title '%s' not found in database", title));
	}
}