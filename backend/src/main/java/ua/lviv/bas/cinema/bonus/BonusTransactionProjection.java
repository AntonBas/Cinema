package ua.lviv.bas.cinema.bonus;

import java.time.Instant;

public interface BonusTransactionProjection {
    Long getId();

    String getType();

    Integer getPointsChangeRaw();

    Instant getCreatedAt();

    Integer getNewBalance();

    default String getPointsChange() {
        Integer raw = getPointsChangeRaw();
        return raw != null ? (raw > 0 ? "+" + raw : String.valueOf(raw)) : null;
    }
}