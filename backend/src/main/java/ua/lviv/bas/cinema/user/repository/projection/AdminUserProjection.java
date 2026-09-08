package ua.lviv.bas.cinema.user.repository.projection;

import ua.lviv.bas.cinema.user.domain.UserRole;
import ua.lviv.bas.cinema.user.domain.VerificationStatus;

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