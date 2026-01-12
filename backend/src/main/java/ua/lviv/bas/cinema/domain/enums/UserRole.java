package ua.lviv.bas.cinema.domain.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
	ROLE_USER("User"), ROLE_CASHIER("Cashier"), ROLE_CONTENT_MANAGER("Content Manager"), ROLE_ADMIN("Administrator");

	private final String displayName;

	private static final Map<String, UserRole> BY_NAME = Arrays.stream(values())
			.collect(Collectors.toMap(Enum::name, Function.identity()));

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
		if (role == null || role.isBlank()) {
			throw new IllegalArgumentException("Role cannot be null or empty");
		}

		UserRole userRole = BY_NAME.get(role.toUpperCase());
		if (userRole == null) {
			throw new IllegalArgumentException("Unknown user role: " + role);
		}
		return userRole;
	}
}