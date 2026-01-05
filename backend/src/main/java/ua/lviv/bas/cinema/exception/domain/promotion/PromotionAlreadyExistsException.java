package ua.lviv.bas.cinema.exception.domain.promotion;

import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;

public class PromotionAlreadyExistsException extends DuplicateEntityException {
	private static final long serialVersionUID = 1L;

	public PromotionAlreadyExistsException(String title) {
		super("Promotion", title, String.format("Promotion with title '%s' already exists in database", title));
	}
}