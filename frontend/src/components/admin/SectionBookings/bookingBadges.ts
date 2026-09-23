import type { BadgeVariant } from "@/components/ui/Badge/Badge";
import type { BookingStatus } from "@/types/booking";
import type { PaymentStatus } from "@/types/payment";
import type { TicketStatus } from "@/types/ticket";

export const BOOKING_STATUS_VARIANT: Record<BookingStatus, BadgeVariant> = {
  PENDING: "warning",
  CONFIRMED: "success",
  CANCELLED: "error",
  EXPIRED: "secondary",
  FAILED: "error",
  REFUNDED: "info",
};

export const PAYMENT_STATUS_VARIANT: Record<PaymentStatus, BadgeVariant> = {
  PENDING: "warning",
  PROCESSING: "warning",
  SUCCESS: "success",
  FAILED: "error",
  CANCELLED: "error",
  EXPIRED: "secondary",
  REFUNDED: "info",
  PARTIALLY_REFUNDED: "info",
};

export const TICKET_STATUS_VARIANT: Record<TicketStatus, BadgeVariant> = {
  ACTIVE: "success",
  USED: "secondary",
  REFUNDED: "info",
  EXPIRED: "secondary",
};
