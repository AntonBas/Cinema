import React from "react";
import { Button } from "@/components/ui/Button/Button";
import { TicketStatusBadge } from "@/components/ui/TicketStatusBadge/TicketStatusBadge";
import { Calendar, Clock, MapPin, Armchair } from "lucide-react";
import type { TicketResponse } from "@/types/ticket";
import { formatDate, formatPrice, formatTime } from "@/utils/formatters";
import styles from "./TicketCard.module.css";

interface TicketCardProps {
  ticket: TicketResponse;
  viewMode: "grid" | "list";
  onShowQR: (ticketCode: string) => void;
  onViewDetails?: (ticket: TicketResponse) => void;
  onRequestRefund?: (ticket: TicketResponse) => void;
}

const getSeatInfo = (ticket: TicketResponse) => {
  if (ticket.row == null || ticket.seatNumber == null) {
    return "Seat not assigned";
  }
  return `Row ${ticket.row}, Seat ${ticket.seatNumber}`;
};

export const TicketCard: React.FC<TicketCardProps> = ({
  ticket,
  viewMode,
  onShowQR,
  onViewDetails,
  onRequestRefund,
}) => {
  const canRefund = ticket.refundable;

  if (viewMode === "list") {
    return (
      <div className={styles.ticketCardList}>
        <div className={styles.listHeader}>
          <div className={styles.listMovieInfo}>
            <h3 className={styles.listMovieTitle}>{ticket.movieTitle}</h3>
            <div className={styles.listMeta}>
              <span className={styles.listMetaItem}>
                <MapPin size={14} />
                {ticket.hallName}
              </span>
              <span className={styles.listMetaItem}>
                <Armchair size={14} />
                {getSeatInfo(ticket)}
              </span>
              <span className={styles.ticketTypeBadge}>
                {ticket.ticketType}
              </span>
            </div>
          </div>
          <TicketStatusBadge status={ticket.status} />
        </div>

        <div className={styles.listDetails}>
          <span className={styles.listDetailItem}>
            <Calendar size={14} />
            {formatDate(ticket.sessionTime)}
          </span>
          <span className={styles.listDetailItem}>
            <Clock size={14} />
            {formatTime(ticket.sessionTime)}
          </span>
          <span className={styles.listPrice}>{formatPrice(ticket.price)}</span>
        </div>

        <div className={styles.listActions}>
          <Button
            variant="primary"
            size="small"
            onClick={() => onShowQR(ticket.ticketCode)}
            disabled={ticket.status !== "ACTIVE"}
          >
            Show QR
          </Button>
          {canRefund && onRequestRefund && (
            <Button
              variant="secondary"
              size="small"
              onClick={() => onRequestRefund(ticket)}
            >
              Return
            </Button>
          )}
          {onViewDetails && (
            <Button
              variant="secondary"
              size="small"
              onClick={() => onViewDetails(ticket)}
            >
              Details
            </Button>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className={styles.ticketCardGrid}>
      <div className={styles.cardHeader}>
        <div className={styles.headerLeft}>
          <TicketStatusBadge status={ticket.status} />
          <div className={styles.ticketTypeBadge}>{ticket.ticketType}</div>
        </div>
        <div className={styles.ticketCode}>#{ticket.ticketCode}</div>
      </div>

      <div className={styles.cardContent}>
        <h3 className={styles.movieTitle}>{ticket.movieTitle}</h3>

        <div className={styles.detailsGrid}>
          <div className={styles.detailItem}>
            <Calendar size={16} className={styles.detailIcon} />
            <div>
              <div className={styles.detailLabel}>Date</div>
              <div className={styles.detailValue}>
                {formatDate(ticket.sessionTime)}
              </div>
            </div>
          </div>
          <div className={styles.detailItem}>
            <Clock size={16} className={styles.detailIcon} />
            <div>
              <div className={styles.detailLabel}>Time</div>
              <div className={styles.detailValue}>
                {formatTime(ticket.sessionTime)}
              </div>
            </div>
          </div>
          <div className={styles.detailItem}>
            <MapPin size={16} className={styles.detailIcon} />
            <div>
              <div className={styles.detailLabel}>Hall</div>
              <div className={styles.detailValue}>{ticket.hallName}</div>
            </div>
          </div>
          <div className={styles.detailItem}>
            <Armchair size={16} className={styles.detailIcon} />
            <div>
              <div className={styles.detailLabel}>Seat</div>
              <div className={styles.detailValue}>{getSeatInfo(ticket)}</div>
            </div>
          </div>
        </div>
      </div>

      <div className={styles.cardFooter}>
        <div className={styles.priceSection}>
          <div className={styles.priceLabel}>Price</div>
          <div className={styles.priceValue}>{formatPrice(ticket.price)}</div>
        </div>

        <div className={styles.actionButtons}>
          <Button
            variant="primary"
            size="small"
            onClick={() => onShowQR(ticket.ticketCode)}
            disabled={ticket.status !== "ACTIVE"}
          >
            Show QR
          </Button>
          {canRefund && onRequestRefund && (
            <Button
              variant="secondary"
              size="small"
              onClick={() => onRequestRefund(ticket)}
            >
              Return
            </Button>
          )}
          {onViewDetails && (
            <Button
              variant="secondary"
              size="small"
              onClick={() => onViewDetails(ticket)}
            >
              Details
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};
