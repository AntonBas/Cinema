package ua.lviv.bas.cinema.exception.domain.user;

import ua.lviv.bas.cinema.exception.core.ConflictException;

public class LastAdminException extends ConflictException {

	private static final long serialVersionUID = 1L;

	public LastAdminException() {
		super("Cannot remove the last administrator", "LAST_ADMIN", "System must have at least one administrator user");
	}

	public LastAdminException(Long userId) {
		super(String.format("Cannot remove user with id '%d' - they are the last administrator", userId), "LAST_ADMIN",
				String.format("User with id %d is the last admin user in system", userId));
	}
}