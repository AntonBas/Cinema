package ua.lviv.bas.cinema.exception.domain.cinema;

import java.time.LocalDateTime;

import ua.lviv.bas.cinema.exception.core.ConflictException;

public class SessionTimeConflictException extends ConflictException {

	private static final long serialVersionUID = 1L;

	public SessionTimeConflictException() {
		super("Session time conflict", "SESSION_TIME_CONFLICT",
				"There is already a session in this hall at the selected time");
	}

	public SessionTimeConflictException(Long hallId, LocalDateTime startTime) {
		super(String.format("Time conflict in hall %d at %s", hallId, startTime), "SESSION_TIME_CONFLICT",
				String.format("Hall %d already has a session at %s", hallId, startTime));
	}
}