package ua.lviv.bas.cinema.domain.projection.bonus;

import java.time.LocalDateTime;

public interface BonusTransactionProjection {
	Long getId();

	String getType();

	Integer getPointsChangeRaw();

	LocalDateTime getCreatedAt();

	Integer getNewBalance();

	default String getPointsChange() {
		Integer raw = getPointsChangeRaw();
		return raw != null ? (raw > 0 ? "+" + raw : String.valueOf(raw)) : null;
	}
}