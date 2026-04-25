package ua.lviv.bas.cinema.exception.domain.user;

import ua.lviv.bas.cinema.exception.core.NotFoundException;

import java.io.Serial;

public class UserNotFoundException extends NotFoundException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(Long userId) {
        super("User not found", "USER_NOT_FOUND",
                String.format("User entity with id %d does not exist", userId));
    }

    public UserNotFoundException(String email) {
        super("User not found", "USER_NOT_FOUND",
                String.format("User entity with email %s does not exist", email));
    }
}