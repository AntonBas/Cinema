package ua.lviv.bas.cinema.exception.domain.financial.promotion;

import java.time.LocalDate;

import ua.lviv.bas.cinema.exception.core.ValidationException;

public class PromotionDatesInvalidException extends ValidationException {
	private static final long serialVersionUID = 1L;

	public PromotionDatesInvalidException(LocalDate start, LocalDate end) {
		super("Promotion end date cannot be before start date", "INVALID_PROMOTION_DATES");
	}
}