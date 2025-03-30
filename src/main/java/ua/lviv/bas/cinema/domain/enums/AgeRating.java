package ua.lviv.bas.cinema.domain.enums;

public enum AgeRating {
	PEGI_3(3, "Suitable for all ages – no restrictions"),
	PEGI_7(7, "May contain mild violence/fear scenes for young children"),
	PEGI_12(12, "Recommended for viewers aged 12 and older"), 
	PEGI_16(16, "Suitable only for teens aged 16+"),
	PEGI_18(18, "Adults only (18+) – restricted content");

	private final int minAge;
	private final String description;

	AgeRating(int minAge, String description) {
		this.minAge = minAge;
		this.description = description;
	}

	public int getMinAge() {
		return minAge;
	}

	public String getDescription() {
		return description;
	}

	public String getDisplayName() {
		return minAge + "+";
	}

}
