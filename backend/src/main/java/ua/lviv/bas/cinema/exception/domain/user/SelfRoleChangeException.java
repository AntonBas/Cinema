package ua.lviv.bas.cinema.exception.domain.user;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class SelfRoleChangeException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SelfRoleChangeException(Long userId) {
        super("Admin can't change their own role", "SELF_ROLE_CHANGE",
                String.format("Self-role change attempt detected for user id %d", userId));
    }
}