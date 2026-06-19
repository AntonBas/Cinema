import React from "react";
import { TicketCard } from "@/components/account/TicketSection/TicketCard/TicketCard";
import type { TicketResponse } from "@/types/ticket";
import styles from "./TicketsList.module.css";

interface TicketsListProps {
  tickets: TicketResponse[];
  viewMode: "grid" | "list";
  onShowQR: (ticketCode: string) => void;
  onRequestRefund?: (ticket: TicketResponse) => void;
}

export const TicketsList: React.FC<TicketsListProps> = ({
  tickets,
  viewMode,
  onShowQR,
  onRequestRefund,
}) => {
  if (!tickets || tickets.length === 0) {
    return (
      <div className={styles.emptyState}>
        <h3 className={styles.emptyTitle}>No tickets found</h3>
      </div>
    );
  }

  return (
    <div className={viewMode === "grid" ? styles.gridView : styles.listView}>
      {tickets.map((ticket) => (
        <TicketCard
          key={ticket.id}
          ticket={ticket}
          viewMode={viewMode}
          onShowQR={onShowQR}
          onRequestRefund={onRequestRefund}
        />
      ))}
    </div>
  );
};
