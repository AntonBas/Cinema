package ua.lviv.bas.cinema.exception.domain.promotion;

import java.time.LocalDateTime;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class PromotionDatesInvalidException extends ValidationException {
	private static final long serialVersionUID = 1L;

	public PromotionDatesInvalidException(LocalDateTime start, LocalDateTime end) {
		super("Promotion end date cannot be before start date", "INVALID_PROMOTION_DATES");
	}
}