export interface AuditLogResponse {
  id: number;
  entityType: string;
  entityId: number;
  targetInfo: string;
  action: string;
  changedBy: string;
  changedAt: string;
  details: AuditLogDetail[];
}

export interface AuditLogDetail {
  fieldName: string;
  oldValue: string | null;
  newValue: string | null;
}

export const EntityTypeDisplay: Record<string, string> = {
  User: "User",
  Booking: "Booking",
  Payment: "Payment",
  Refund: "Refund",
  Bonus: "Bonus",
  Ticket: "Ticket",
  TicketType: "Ticket Type",
  Movie: "Movie",
  Session: "Session",
  CinemaHall: "Cinema Hall",
  Promotion: "Promotion",
  BonusRules: "Bonus Rules",
};

export const ActionDisplay: Record<string, string> = {
  CREATED: "Created",
  UPDATED: "Updated",
  DELETED: "Deleted",
  SUCCESS: "Success",
  FAILED: "Failed",
  REFUND: "Refund",
  CANCELLED: "Cancelled",
  CONFIRMED: "Confirmed",
  VALIDATED: "Validated",
  CLAIMED: "Claimed",
  TOGGLE_STATUS: "Toggled Status",
  RESET_TO_DEFAULTS: "Reset to Defaults",
  REJECTED: "Rejected",
  RETRY: "Retry",
  REACTIVATED: "Reactivated",
  REGISTER: "Register",
  PASSWORD_CHANGED: "Password Changed",
  PASSWORD_RESET_REQUESTED: "Password Reset Requested",
  PASSWORD_RESET_COMPLETED: "Password Reset Completed",
  EMAIL_CHANGE_REQUESTED: "Email Change Requested",
  ROLE_CHANGED: "Role Changed",
  STATUS_CHANGED: "Status Changed",
  VERIFICATION_CHANGED: "Verification Changed",
  POINTS_ADDED: "Points Added",
  POINTS_SPENT: "Points Spent",
  POINTS_ACCRUED: "Points Accrued",
  POINTS_REFUNDED: "Points Refunded",
};

export const getEntityTypeDisplay = (entityType: string): string => {
  return EntityTypeDisplay[entityType] || entityType;
};

export const getActionDisplay = (action: string): string => {
  return ActionDisplay[action] || action;
};

export const getActionColor = (action: string): string => {
  if (action.includes("CREATED") || action.includes("REGISTER")) return "green";
  if (action.includes("DELETED") || action.includes("REJECTED")) return "red";
  if (action.includes("SUCCESS") || action.includes("CONFIRMED"))
    return "green";
  if (action.includes("FAILED") || action.includes("CANCELLED")) return "red";
  if (action.includes("UPDATED") || action.includes("TOGGLE")) return "blue";
  if (action.includes("REFUND") || action.includes("POINTS")) return "orange";
  return "default";
};
