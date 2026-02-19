package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDateTime;

import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;

public interface BonusTransactionProjection {
	Long getId();

	BonusTransactionType getType();

	Integer getPointsChangeRaw();

	LocalDateTime getCreatedAt();

	Integer getNewBalance();

	String getMovieTitle();

	String getBookingReference();

	String getCinemaHall();

	LocalDateTime getSessionDateTime();

	default String getTypeDisplay() {
		return getType() != null ? getType().getDisplayName() : null;
	}

	default String getPointsChange() {
		Integer raw = getPointsChangeRaw();
		return raw != null ? (raw > 0 ? "+" + raw : String.valueOf(raw)) : null;
	}
}