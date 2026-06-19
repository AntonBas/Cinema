package ua.lviv.bas.cinema.exception.domain.financial.promotion;

import org.springframework.http.HttpStatus;

import ua.lviv.bas.cinema.exception.core.BusinessException;

import java.io.Serial;

public class PromotionNotActiveException extends BusinessException {

	@Serial
	private static final long serialVersionUID = 1L;

	public PromotionNotActiveException(String promotionTitle) {
		super(String.format("Promotion '%s' is not active or has expired", promotionTitle), "PROMOTION_NOT_ACTIVE",
				HttpStatus.BAD_REQUEST, String.format("Promotion '%s' is outside of active period", promotionTitle));
	}
}