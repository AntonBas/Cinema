package ua.lviv.bas.cinema.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CoupleRowSeatsValidator.class)
public @interface CoupleRowSeatsConstraint {
	String message() default "Number of seats per row must be even when hall has COUPLE seats";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}