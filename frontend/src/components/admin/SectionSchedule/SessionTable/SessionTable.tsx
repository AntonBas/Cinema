import React from "react";
import type { SessionAdminResponse } from "@/types/session";
import { Button, Badge } from "@/components/ui";
import styles from "./SessionTable.module.css";

interface SessionTableProps {
  sessions: SessionAdminResponse[];
  onEdit: (session: SessionAdminResponse) => void;
  onDelete: (session: SessionAdminResponse) => void;
  onCancel: (session: SessionAdminResponse) => void;
  onReactivate: (session: SessionAdminResponse) => void;
}

const getStatusText = (status: string): string => {
  const statusMap: Record<string, string> = {
    SCHEDULED: "Scheduled",
    ONGOING: "Ongoing",
    COMPLETED: "Completed",
    CANCELLED: "Cancelled",
  };
  return statusMap[status] || status;
};

const getStatusClass = (status: string): string => {
  const classMap: Record<string, string> = {
    SCHEDULED: styles.statusScheduled,
    ONGOING: styles.statusOngoing,
    COMPLETED: styles.statusCompleted,
    CANCELLED: styles.statusCancelled,
  };
  return classMap[status] || "";
};

const canEdit = (status: string): boolean => status === "SCHEDULED";
const canDelete = (status: string): boolean => status === "SCHEDULED";
const canCancel = (status: string): boolean => status === "SCHEDULED";
const canReactivate = (status: string): boolean => status === "CANCELLED";

const getOccupancyPercentage = (
  ticketsSold: number,
  capacity: number,
): number => {
  return capacity > 0 ? Math.round((ticketsSold / capacity) * 100) : 0;
};

const formatCurrency = (price: number | string | null | undefined): string => {
  const num = typeof price === "string" ? parseFloat(price) : (price ?? 0);
  return `${num.toFixed(2)} UAH`;
};

const formatTime = (dateString: string): string => {
  return new Date(dateString).toLocaleTimeString("en-US", {
    hour: "2-digit",
    minute: "2-digit",
  });
};

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
  });
};

export const SessionTable: React.FC<SessionTableProps> = ({
  sessions,
  onEdit,
  onDelete,
  onCancel,
  onReactivate,
}) => {
  if (!sessions.length) {
    return (
      <div className={styles.empty}>
        <h3>No sessions found</h3>
        <p>There are currently no movie sessions matching your criteria.</p>
      </div>
    );
  }

  return (
    <div className={styles.table}>
      <div className={styles.tableHeader}>
        <div>Movie</div>
        <div>Hall</div>
        <div>Time</div>
        <div>Price</div>
        <div>Occupancy</div>
        <div>Revenue</div>
        <div>Status</div>
        <div>Actions</div>
      </div>

      {sessions.map((session) => {
        const occupancy = getOccupancyPercentage(
          session.ticketsSold,
          session.hallCapacity,
        );
        const editable = canEdit(session.status);
        const deletable = canDelete(session.status);
        const cancellable = canCancel(session.status);
        const reactivatable = canReactivate(session.status);

        return (
          <div key={session.id} className={styles.tableRow}>
            <div className={styles.movieInfo}>
              <div className={styles.movieTitle}>{session.movieTitle}</div>
              <div className={styles.movieMeta}>
                {session.movieDuration} min
              </div>
            </div>

            <div className={styles.hallInfo}>
              <div className={styles.hallName}>{session.hallName}</div>
              <div className={styles.capacity}>
                {session.hallCapacity} seats
              </div>
            </div>

            <div className={styles.timeInfo}>
              <div className={styles.date}>{formatDate(session.startTime)}</div>
              <div className={styles.time}>{formatTime(session.startTime)}</div>
            </div>

            <div className={styles.price}>
              {formatCurrency(session.basePrice)}
            </div>

            <div className={styles.occupancyWrapper}>
              <div className={styles.occupancyInfo}>
                {session.ticketsSold}/{session.hallCapacity} ({occupancy}%)
              </div>
              <div className={styles.occupancyBar}>
                <div
                  className={styles.occupancyFill}
                  style={{ width: `${Math.min(occupancy, 100)}%` }}
                />
              </div>
            </div>

            <div className={styles.revenueInfo}>
              {formatCurrency(session.totalRevenue)}
            </div>

            <div className={styles.status}>
              <Badge className={getStatusClass(session.status)}>
                {getStatusText(session.status)}
              </Badge>
            </div>

            <div className={styles.actions}>
              {editable && (
                <Button
                  variant="success"
                  size="small"
                  onClick={() => onEdit(session)}
                >
                  Edit
                </Button>
              )}
              {cancellable && (
                <Button
                  variant="secondary"
                  size="small"
                  onClick={() => onCancel(session)}
                >
                  Cancel
                </Button>
              )}
              {reactivatable && (
                <Button
                  variant="success"
                  size="small"
                  onClick={() => onReactivate(session)}
                >
                  Reactivate
                </Button>
              )}
              {deletable && (
                <Button
                  variant="error"
                  size="small"
                  onClick={() => onDelete(session)}
                >
                  Delete
                </Button>
              )}
            </div>
          </div>
        );
      })}
    </div>
  );
};
