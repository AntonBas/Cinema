package ua.lviv.bas.cinema.domain.enums;

public enum CinemaSessionStatus {
	SCHEDULED("Scheduled"), ONGOING("Ongoing"), CANCELLED("Cancelled"), COMPLETED("Completed");

	private final String displayName;

	CinemaSessionStatus(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isActive() {
		return this == SCHEDULED || this == ONGOING;
	}
}