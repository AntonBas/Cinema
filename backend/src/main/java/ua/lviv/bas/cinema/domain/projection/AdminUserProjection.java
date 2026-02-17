package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDateTime;

import ua.lviv.bas.cinema.domain.enums.UserRole;
import ua.lviv.bas.cinema.domain.enums.VerificationStatus;

public interface AdminUserProjection {
	Long getId();

	String getEmail();

	String getFirstName();

	String getLastName();

	UserRole getUserRole();

	boolean isEnabled();

	VerificationStatus getVerificationStatus();

	LocalDateTime getVerifiedAt();

	LocalDateTime getCreatedAt();

	LocalDateTime getUpdatedAt();

	Long getTicketsCount();

	LocalDateTime getLastActivity();
}