package ua.lviv.bas.cinema.booking.domain.status;

import java.util.List;

public enum ReservationStatus {
    PENDING, CONFIRMED, EXPIRED, CANCELLED;

    public static final List<ReservationStatus> ACTIVE_STATUSES = List.of(PENDING, CONFIRMED);
}