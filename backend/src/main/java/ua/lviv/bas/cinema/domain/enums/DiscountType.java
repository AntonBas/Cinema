package ua.lviv.bas.cinema.domain.enums;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public enum DiscountType {
	DISABLED_GROUP_1("Disabled Group 1", new BigDecimal("50.00")),
	DISABLED_GROUP_2("Disabled Group 2", new BigDecimal("50.00")),
	DISABLED_GROUP_3("Disabled Group 3", new BigDecimal("50.00")),
	DISABLED_CHILD("Disabled Child", new BigDecimal("50.00")),

	MILITARY_ACTIVE("Active Military", new BigDecimal("50.00")),
	MILITARY_VETERAN("Military Veteran", new BigDecimal("50.00")),
	FAMILY_OF_FALLEN("Family of Fallen Soldier", new BigDecimal("50.00")),

	STUDENT("Student", new BigDecimal("30.00")), PENSIONER("Pensioner", new BigDecimal("30.00")),
	CHILD_UNDER_12("Child under 12", new BigDecimal("30.00")), LARGE_FAMILY("Large Family", new BigDecimal("25.00")),
	TEACHER("Teacher", new BigDecimal("20.00")), DOCTOR("Medical Worker", new BigDecimal("20.00"));

	private final String displayName;
	private final BigDecimal defaultPercent;

	DiscountType(String displayName, BigDecimal defaultPercent) {
		this.displayName = displayName;
		this.defaultPercent = defaultPercent;
	}

	public boolean isDisabledCategory() {
		return this == DISABLED_GROUP_1 || this == DISABLED_GROUP_2 || this == DISABLED_GROUP_3
				|| this == DISABLED_CHILD;
	}

	public boolean isMilitaryCategory() {
		return this == MILITARY_ACTIVE || this == MILITARY_VETERAN || this == FAMILY_OF_FALLEN;
	}

	public boolean requiresDocumentVerification() {
		return isDisabledCategory() || isMilitaryCategory() || this == STUDENT || this == PENSIONER;
	}
}