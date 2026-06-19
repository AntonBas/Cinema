package ua.lviv.bas.cinema.domain.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    ROLE_USER("User"), ROLE_CASHIER("Cashier"), ROLE_CONTENT_MANAGER("Content Manager"), ROLE_ADMIN("Administrator");

    private final String displayName;

    public boolean isAdmin() {
        return this == ROLE_ADMIN;
    }

}