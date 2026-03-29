package ua.lviv.bas.cinema.domain.bonus;

import lombok.Getter;

@Getter
public enum BonusTransactionType {
	WELCOME_BONUS("Welcome Bonus"), BIRTHDAY_BONUS("Birthday Bonus"), PROMOTION_BONUS("Promotion Bonus"),
	BOOKING_SPEND("Booking Spend"), PAYMENT_ACCRUAL("Payment Accrual"), REFUND_RETURN("Refund Return"),
	BOOKING_CANCEL("Booking Cancel");

	private final String displayName;

	BonusTransactionType(String displayName) {
		this.displayName = displayName;
	}
}