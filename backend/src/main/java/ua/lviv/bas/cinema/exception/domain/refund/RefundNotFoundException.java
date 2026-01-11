package ua.lviv.bas.cinema.exception.domain.refund;

import org.springframework.lang.Nullable;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class RefundNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public RefundNotFoundException(Long refundId) {
		super(String.format("Refund with ID %d not found", refundId), "REFUND_NOT_FOUND",
				String.format("Refund entity not found with ID: %d", refundId));
	}

	public RefundNotFoundException(String message) {
		super(message, "REFUND_NOT_FOUND", message);
	}

	public RefundNotFoundException(String message, @Nullable Throwable cause) {
		super(message, "REFUND_NOT_FOUND", message, cause);
	}
}