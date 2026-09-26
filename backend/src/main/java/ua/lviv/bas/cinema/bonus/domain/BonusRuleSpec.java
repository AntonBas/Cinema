package ua.lviv.bas.cinema.bonus.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record BonusRuleSpec(Set<BonusRuleField> requiredFields, Set<BonusRuleField> optionalFields) {

    private static final Map<BonusTransactionType, BonusRuleSpec> SPECS = Map.of(
            BonusTransactionType.WELCOME_BONUS, new BonusRuleSpec(Set.of(BonusRuleField.POINTS), Set.of()),
            BonusTransactionType.BIRTHDAY_BONUS, new BonusRuleSpec(Set.of(BonusRuleField.POINTS), Set.of()),
            BonusTransactionType.BOOKING_SPEND,
            new BonusRuleSpec(Set.of(), Set.of(BonusRuleField.MIN_POINTS, BonusRuleField.MAX_POINTS)),
            BonusTransactionType.PAYMENT_ACCRUAL, new BonusRuleSpec(Set.of(BonusRuleField.MONEY_RATIO),
                    Set.of(BonusRuleField.MIN_POINTS, BonusRuleField.MAX_POINTS)));

    public static Optional<BonusRuleSpec> of(BonusTransactionType type) {
        return Optional.ofNullable(SPECS.get(type));
    }

    public static Set<BonusTransactionType> configurableTypes() {
        return SPECS.keySet();
    }

    public static List<BonusRuleField> requiredFieldsOf(BonusTransactionType type) {
        return of(type).map(spec -> sorted(spec.requiredFields())).orElse(List.of());
    }

    public static List<BonusRuleField> optionalFieldsOf(BonusTransactionType type) {
        return of(type).map(spec -> sorted(spec.optionalFields())).orElse(List.of());
    }

    public boolean allows(BonusRuleField field) {
        return requiredFields.contains(field) || optionalFields.contains(field);
    }

    public boolean requires(BonusRuleField field) {
        return requiredFields.contains(field);
    }

    private static List<BonusRuleField> sorted(Set<BonusRuleField> fields) {
        return fields.stream().sorted(Comparator.naturalOrder()).toList();
    }
}
