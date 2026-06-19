import React from "react";
import { Button } from "@/components/ui/Button/Button";
import type { TicketStatus } from "@/types/ticket";

interface CashierValidateButtonProps {
  status: TicketStatus;
  loading: boolean;
  onValidate: () => void;
}

export const CashierValidateButton: React.FC<CashierValidateButtonProps> = ({
  status,
  loading,
  onValidate,
}) => {
  if (status !== "ACTIVE") {
    return null;
  }

  return (
    <Button
      variant="success"
      size="large"
      loading={loading}
      onClick={onValidate}
    >
      Validate Ticket
    </Button>
  );
};
