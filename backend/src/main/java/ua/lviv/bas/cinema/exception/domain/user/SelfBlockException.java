package ua.lviv.bas.cinema.exception.domain.user;

import ua.lviv.bas.cinema.exception.core.ConflictException;

import java.io.Serial;

public class SelfBlockException extends ConflictException {

    @Serial
    private static final long serialVersionUID = 1L;

    public SelfBlockException() {
        super("Cannot block yourself", "SELF_BLOCK", "User attempted to block their own account");
    }
}