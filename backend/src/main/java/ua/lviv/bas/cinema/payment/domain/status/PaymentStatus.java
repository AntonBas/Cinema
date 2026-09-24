package ua.lviv.bas.cinema.payment.domain.status;

public enum PaymentStatus {
    PENDING, PROCESSING, SUCCESS, FAILED, CANCELLED, EXPIRED, REFUNDED, PARTIALLY_REFUNDED, REFUND_REQUIRED;

    public boolean isActive() {
        return this == PENDING || this == PROCESSING;
    }

    public boolean isFailed() {
        return this == FAILED || this == EXPIRED || this == CANCELLED;
    }

    public boolean canReceiveLateSuccess() {
        return isActive() || isFailed();
    }

    public boolean canBeRetried() {
        return this == FAILED;
    }
}