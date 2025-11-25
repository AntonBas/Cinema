package ua.lviv.bas.cinema.exception.domain.user;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

public class UserNotFoundException extends NotFoundException {

	private static final long serialVersionUID = 1L;

	public UserNotFoundException(Long userId) {
		super(String.format("User with id '%d' not found", userId), "USER_NOT_FOUND",
				String.format("User entity with id %d does not exist in database", userId));
	}

	public UserNotFoundException(String email) {
		super(String.format("User with email '%s' not found", email), "USER_NOT_FOUND",
				String.format("User entity with email %s does not exist in database", email));
	}
}