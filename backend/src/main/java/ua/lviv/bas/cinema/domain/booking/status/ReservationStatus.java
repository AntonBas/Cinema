package ua.lviv.bas.cinema.domain.booking.status;

import java.util.List;

public enum ReservationStatus {
	PENDING, CONFIRMED, EXPIRED;

	public static final List<ReservationStatus> ACTIVE_STATUSES = List.of(PENDING, CONFIRMED);
}