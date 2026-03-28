package ua.lviv.bas.cinema.exception.domain.financial.promotion;

import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;

public class PromotionAlreadyExistsException extends DuplicateEntityException {
	private static final long serialVersionUID = 1L;

	private PromotionAlreadyExistsException(String message) {
		super(message, new RuntimeException(message));
	}

	public static PromotionAlreadyExistsException forTitle(String title) {
		return new PromotionAlreadyExistsException(String.format("Promotion with title '%s' already exists", title));
	}
}