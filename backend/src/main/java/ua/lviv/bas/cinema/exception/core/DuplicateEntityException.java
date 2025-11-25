package ua.lviv.bas.cinema.exception.core;

import org.springframework.lang.Nullable;

public class DuplicateEntityException extends ConflictException {

	private static final long serialVersionUID = 1L;

	public DuplicateEntityException(String entityType, String identifier) {
		super(String.format("%s with identifier '%s' already exists", entityType, identifier), "DUPLICATE_ENTITY",
				String.format("Duplicate entity detected: %s [%s]", entityType, identifier));
	}

	public DuplicateEntityException(String entityType, String identifier, @Nullable String debugMessage) {
		super(String.format("%s with identifier '%s' already exists", entityType, identifier), "DUPLICATE_ENTITY",
				debugMessage);
	}

	public DuplicateEntityException(String message) {
		super(message, "DUPLICATE_ENTITY");
	}

	public DuplicateEntityException(String message, Throwable cause) {
		super(message, "DUPLICATE_ENTITY", cause.getMessage(), cause);
	}
}