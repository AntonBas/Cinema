package ua.lviv.bas.cinema.user.repository.projection;

import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;

import java.time.Instant;

public interface AdminUserProjection {
    Long getId();

    String getEmail();

    String getFirstName();

    String getLastName();

    UserRole getUserRole();

    boolean isEnabled();

    VerificationStatus getVerificationStatus();

    Instant getVerifiedAt();

    Long getTicketsCount();

    Instant getLastActivity();
}