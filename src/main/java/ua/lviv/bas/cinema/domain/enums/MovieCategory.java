package ua.lviv.bas.cinema.domain.enums;

public enum MovieCategory {
	CURRENT("Available"), UPCOMING("Coming"), ARCHIVED("Archived");

	private final String displayName;

	MovieCategory(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static MovieCategory fromDisplayName(String displayName) {
		for (MovieCategory category : values()) {
			if (category.displayName.equals(displayName)) {
				return category;
			}
		}
		throw new IllegalArgumentException("Unknown display name: " + displayName);
	}

}
