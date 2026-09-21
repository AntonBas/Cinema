import React from "react";
import { Select } from "@/components/ui/Select/Select";
import type { TicketPriceInfo } from "@/types/seatReservation";
import styles from "./TicketTypeSelect.module.css";

interface TicketTypeSelectProps {
  seatId: number;
  ticketPrices: TicketPriceInfo[];
  selectedTicketTypeId?: number;
  onSelect: (seatId: number, ticketTypeId: number) => void;
}

export const TicketTypeSelect: React.FC<TicketTypeSelectProps> = ({
  seatId,
  ticketPrices,
  selectedTicketTypeId,
  onSelect,
}) => {
  if (!ticketPrices || ticketPrices.length === 0) {
    return <span className={styles.noTypes}>No ticket types available</span>;
  }

  const handleChange = (value: string | number) => {
    const ticketTypeId =
      typeof value === "number" ? value : parseInt(value, 10);
    if (!isNaN(ticketTypeId)) {
      onSelect(seatId, ticketTypeId);
    }
  };

  const defaultValue = selectedTicketTypeId || ticketPrices[0].ticketTypeId;

  const formatLabel = (ticket: TicketPriceInfo) => {
    let label = ticket.ticketTypeName;

    const hasMinAge = ticket.minAge != null;
    const hasMaxAge = ticket.maxAge != null;

    if (hasMinAge && hasMaxAge) {
      label += ` ${ticket.minAge}-${ticket.maxAge}`;
    } else if (hasMinAge) {
      label += ` ${ticket.minAge}+`;
    } else if (hasMaxAge) {
      label += ` 0-${ticket.maxAge}`;
    }

    if (ticket.requiresDocument && ticket.documentType) {
      label += ` (${ticket.documentType})`;
    } else if (ticket.requiresDocument) {
      label += ` (ID required)`;
    }

    label += ` - ${parseFloat(ticket.finalPrice).toFixed(2)}₴`;

    return label;
  };

  return (
    <Select
      className={styles.select}
      value={defaultValue}
      onChange={handleChange}
      ariaLabel={`Select ticket type for seat ${seatId}`}
      options={ticketPrices.map((ticket) => ({
        value: ticket.ticketTypeId,
        label: formatLabel(ticket),
      }))}
    />
  );
};
