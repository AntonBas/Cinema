package ua.lviv.bas.cinema.domain.enums;

public enum PaymentStatus {
	PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, EXPIRED, REFUNDED, PARTIALLY_REFUNDED;

	public boolean isActive() {
		return this == PENDING || this == PROCESSING;
	}

	public boolean isCompleted() {
		return this == SUCCESS;
	}

	public boolean isFailed() {
		return this == FAILED || this == EXPIRED || this == CANCELLED;
	}

	public boolean canBeRetried() {
		return this == FAILED;
	}
}