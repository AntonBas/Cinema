package ua.lviv.bas.cinema.domain.enums;

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

	public boolean isCashier() {
		return this == ROLE_CASHIER;
	}

	public boolean isContentManager() {
		return this == ROLE_CONTENT_MANAGER;
	}

	public static UserRole fromString(String role) {
		if (role == null) {
			return ROLE_USER;
		}
		try {
			return UserRole.valueOf(role.toUpperCase());
		} catch (IllegalArgumentException e) {
			return ROLE_USER;
		}
	}
}
