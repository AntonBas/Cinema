package ua.lviv.bas.cinema.repository.user.projection;

import ua.lviv.bas.cinema.domain.user.UserRole;
import ua.lviv.bas.cinema.domain.user.VerificationStatus;

import java.time.LocalDateTime;

public interface AdminUserProjection {
    Long getId();

    String getEmail();

    String getFirstName();

    String getLastName();

    UserRole getUserRole();

    boolean isEnabled();

    VerificationStatus getVerificationStatus();

    LocalDateTime getVerifiedAt();

    Long getTicketsCount();

    LocalDateTime getLastActivity();
}