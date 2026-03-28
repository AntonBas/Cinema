package ua.lviv.bas.cinema.exception.domain.financial.refund;

import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;

import ua.lviv.bas.cinema.exception.core.BusinessException;

public class RefundProcessingException extends BusinessException {

	private static final long serialVersionUID = 1L;

	public RefundProcessingException(String message, @Nullable Throwable cause) {
		super(message, "REFUND_PROCESSING_ERROR", HttpStatus.INTERNAL_SERVER_ERROR,
				"Error occurred during refund processing: " + message, cause);
	}
}