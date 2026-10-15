package ua.lviv.bas.cinema.user.domain;

import java.time.Duration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenType {
    VERIFICATION(Duration.ofHours(24)),
    PASSWORD_RESET(Duration.ofMinutes(30)),
    EMAIL_CHANGE(Duration.ofHours(24));

    private final Duration validity;
}
