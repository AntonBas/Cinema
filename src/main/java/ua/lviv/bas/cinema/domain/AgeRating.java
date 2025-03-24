package ua.lviv.bas.cinema.domain;

public enum AgeRating {
	G("General Audiences - All ages admitted"),
	PG_13("Parents Strongly Cautioned - Some material may be inappropriate for children under 13"),
	R("Restricted - Under 17 requires accompanying parent or adult guardian"),
	NC_17("Adults Only - No one 17 and under admitted");

	private final String description;

	AgeRating(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public int getMinAge() {
		return switch (this) {
		case G -> 0;
		case PG_13 -> 13;
		case R -> 17;
		case NC_17 -> 18;
		};
	}
}
