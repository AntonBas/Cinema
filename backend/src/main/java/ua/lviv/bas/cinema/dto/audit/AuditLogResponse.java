package ua.lviv.bas.cinema.dto.audit;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import ua.lviv.bas.cinema.domain.audit.AuditAction;

public record AuditLogResponse(

		@Schema(description = "Unique identifier of the audit log entry", example = "1") Long id,

		@Schema(description = "Type of the entity that was changed", example = "User", allowableValues = {
				"User", "Booking", "Payment", "Refund", "Bonus", "Ticket", "TicketType", "Movie", "Session",
				"CinemaHall", "Promotion", "BonusRules" }) String entityType,

		@Schema(description = "Action that was performed", example = "CREATED", allowableValues = { "CREATED",
				"UPDATED", "DELETED", "SUCCESS", "FAILED", "REFUND", "CANCELLED", "CONFIRMED", "VALIDATED", "CLAIMED",
				"TOGGLE_STATUS", "RESET_TO_DEFAULTS", "REJECTED", "RETRY", "REACTIVATED", "REGISTER",
				"PASSWORD_CHANGED", "PASSWORD_RESET_REQUESTED", "PASSWORD_RESET_COMPLETED", "EMAIL_CHANGE_REQUESTED",
				"ROLE_CHANGED", "STATUS_CHANGED", "VERIFICATION_CHANGED", "POINTS_ADDED", "POINTS_SPENT",
				"POINTS_ACCRUED", "POINTS_REFUNDED" }) AuditAction action,

		@Schema(description = "Old value of the entity before change (JSON format)") String oldValue,

		@Schema(description = "New value of the entity after change (JSON format)") String newValue,

		@Schema(description = "Email of the user who performed the action", example = "admin@example.com") String changedBy,

		@Schema(description = "Date and time when the action was performed", example = "2024-01-15T10:30:00") LocalDateTime changedAt) {
}