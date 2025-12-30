package ua.lviv.bas.cinema.domain.enums;

import java.time.LocalDateTime;

public enum CinemaSessionStatus {
	SCHEDULED("Scheduled"), ONGOING("Ongoing"), CANCELLED("Cancelled"), COMPLETED("Completed");

	private final String displayName;

	CinemaSessionStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static CinemaSessionStatus fromString(String value) {
		for (CinemaSessionStatus status : values()) {
			if (status.name().equalsIgnoreCase(value) || status.displayName.equalsIgnoreCase(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("Unknown CinemaSessionStatus: " + value);
	}

	public static boolean isActive(CinemaSessionStatus status) {
		return status == SCHEDULED || status == ONGOING;
	}

	public static boolean isInactive(CinemaSessionStatus status) {
		return status == CANCELLED || status == COMPLETED;
	}

	public static boolean isAvailableForBooking(CinemaSessionStatus status, LocalDateTime sessionStartTime) {
		return status == SCHEDULED && sessionStartTime.isAfter(LocalDateTime.now());
	}

	public static boolean isPastSession(CinemaSessionStatus status, LocalDateTime sessionStartTime,
			int durationMinutes) {
		if (sessionStartTime == null)
			return false;
		LocalDateTime sessionEnd = sessionStartTime.plusMinutes(durationMinutes);
		return status == COMPLETED || sessionEnd.isBefore(LocalDateTime.now());
	}

	public static boolean isSessionOngoing(LocalDateTime sessionStartTime, int durationMinutes) {
		if (sessionStartTime == null)
			return false;
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime sessionEnd = sessionStartTime.plusMinutes(durationMinutes);
		return now.isAfter(sessionStartTime) && now.isBefore(sessionEnd);
	}
}