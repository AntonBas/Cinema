package ua.lviv.bas.cinema.domain.cinema.status;

import lombok.Getter;

@Getter
public enum CinemaSessionStatus {
    SCHEDULED("Scheduled"), ONGOING("Ongoing"), CANCELLED("Cancelled"), COMPLETED("Completed");

    private final String displayName;

    CinemaSessionStatus(String displayName) {
        this.displayName = displayName;
    }

    public boolean isActive() {
        return this == SCHEDULED || this == ONGOING;
    }
}