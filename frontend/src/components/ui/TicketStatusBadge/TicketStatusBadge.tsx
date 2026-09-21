import React from "react";
import { Badge } from "@/components/ui/Badge/Badge";
import type { BadgeVariant } from "@/components/ui/Badge/Badge";
import { TicketStatusDisplay } from "@/types/ticket";
import type { TicketStatus } from "@/types/ticket";

interface TicketStatusBadgeProps {
  status: TicketStatus;
  size?: "small" | "medium" | "large";
}

const TICKET_STATUS_VARIANT: Record<TicketStatus, BadgeVariant> = {
  ACTIVE: "success",
  USED: "info",
  REFUNDED: "secondary",
  EXPIRED: "warning",
};

export const TicketStatusBadge: React.FC<TicketStatusBadgeProps> = ({
  status,
  size = "medium",
}) => (
  <Badge variant={TICKET_STATUS_VARIANT[status]} size={size}>
    {TicketStatusDisplay[status]}
  </Badge>
);
