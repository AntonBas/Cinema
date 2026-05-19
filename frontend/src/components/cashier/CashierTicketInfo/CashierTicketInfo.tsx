import React from "react";
import { IdCard } from "lucide-react";
import styles from "./CashierTicketInfo.module.css";
import type { TicketCashierResponse } from "@/types/ticket";

interface CashierTicketInfoProps {
  ticket: TicketCashierResponse;
}

export const CashierTicketInfo: React.FC<CashierTicketInfoProps> = ({
  ticket,
}) => {
  return (
    <div className={styles.container}>
      <div className={styles.row}>
        <span className={styles.label}>Movie</span>
        <span className={styles.value}>{ticket.movieTitle}</span>
      </div>

      <div className={styles.row}>
        <span className={styles.label}>Time</span>
        <span className={styles.value}>
          {new Date(ticket.sessionTime).toLocaleString()}
        </span>
      </div>

      <div className={styles.row}>
        <span className={styles.label}>Hall</span>
        <span className={styles.value}>{ticket.hallName}</span>
      </div>

      <div className={styles.row}>
        <span className={styles.label}>Seat</span>
        <span className={styles.value}>
          Row {ticket.seatRow}, Seat {ticket.seatNumber}
        </span>
      </div>

      <div className={styles.row}>
        <span className={styles.label}>Type</span>
        <span className={styles.value}>{ticket.ticketType}</span>
      </div>

      <div className={styles.row}>
        <span className={styles.label}>Price</span>
        <span className={styles.value}>{ticket.finalPrice} UAH</span>
      </div>

      <div className={styles.row}>
        <span className={styles.label}>Email</span>
        <span className={styles.value}>{ticket.userEmail}</span>
      </div>

      {ticket.requiresDocument && (
        <div className={styles.documentWarning}>
          <IdCard size={18} />
          Check: {ticket.documentType}
        </div>
      )}
    </div>
  );
};
