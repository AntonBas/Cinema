import React from "react";
import { Badge } from "@/components/ui/Badge/Badge";
import type { BadgeVariant } from "@/components/ui/Badge/Badge";
import type { TicketStatus } from "@/types/ticket";

interface CashierStatusBadgeProps {
  status: TicketStatus;
}

const statusConfig: Record<
  TicketStatus,
  { variant: BadgeVariant; label: string }
> = {
  ACTIVE: { variant: "success", label: "Active" },
  USED: { variant: "error", label: "Used" },
  REFUNDED: { variant: "error", label: "Refunded" },
  EXPIRED: { variant: "warning", label: "Expired" },
};

export const CashierStatusBadge: React.FC<CashierStatusBadgeProps> = ({
  status,
}) => {
  const config = statusConfig[status];

  return (
    <Badge variant={config.variant} size="large">
      {config.label}
    </Badge>
  );
};
