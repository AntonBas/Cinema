package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDateTime;

public interface BonusTransactionProjection {
	Long getId();

	String getType();

	String getTypeDisplay();

	Integer getPointsChangeRaw();

	LocalDateTime getCreatedAt();

	Integer getNewBalance();

	String getMovieTitle();

	String getBookingReference();

	String getCinemaHall();

	LocalDateTime getSessionDateTime();

	default String getPointsChange() {
		Integer raw = getPointsChangeRaw();
		return raw != null ? (raw > 0 ? "+" + raw : String.valueOf(raw)) : null;
	}
}