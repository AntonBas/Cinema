package ua.lviv.bas.cinema.bonus.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BonusRuleField {
    POINTS("points"),
    MONEY_RATIO("moneyRatio"),
    MIN_POINTS("minPointsPerTransaction"),
    MAX_POINTS("maxPointsPerTransaction");

    @JsonValue
    private final String fieldName;
}
