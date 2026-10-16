package ua.lviv.bas.cinema.user.admin;

import ua.lviv.bas.cinema.user.UserRole;
import ua.lviv.bas.cinema.user.VerificationStatus;

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