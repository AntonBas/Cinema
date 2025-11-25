package ua.lviv.bas.cinema.exception.domain.user;

import ua.lviv.bas.cinema.exception.core.ConflictException;

public class SelfBlockException extends ConflictException {

	private static final long serialVersionUID = 1L;

	public SelfBlockException() {
		super("Cannot block yourself", "SELF_BLOCK", "User attempted to block their own account");
	}

	public SelfBlockException(Long userId) {
		super(String.format("User with id '%d' attempted to block themselves", userId), "SELF_BLOCK",
				String.format("Self-block attempt detected for user id %d", userId));
	}
}