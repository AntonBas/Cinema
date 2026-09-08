package ua.lviv.bas.cinema.common;

import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class UniquenessValidator {

    private UniquenessValidator() {
    }

    public static void validate(Long excludeId, BooleanSupplier existsCheck, Predicate<Long> existsExcludingId,
                                Supplier<? extends RuntimeException> exceptionSupplier) {
        boolean exists = excludeId != null ? existsExcludingId.test(excludeId) : existsCheck.getAsBoolean();
        if (exists) {
            throw exceptionSupplier.get();
        }
    }
}
