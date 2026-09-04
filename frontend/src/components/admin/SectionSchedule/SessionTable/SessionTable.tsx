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
    <div className={styles.tableWrapper}>
      <div className={styles.tableContainer}>
        <table className={styles.table}>
          <thead>
            <tr>
              <th>Movie</th>
              <th>Hall</th>
              <th>Time</th>
              <th>Price</th>
              <th>Occupancy</th>
              <th>Revenue</th>
              <th>Status</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
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
                <tr key={session.id}>
                  <td className={styles.movieCell} data-label="Movie">
                    <div className={styles.movieInfo}>
                      <div className={styles.movieTitle}>{session.movieTitle}</div>
                      <div className={styles.movieMeta}>
                        {session.movieDuration} min
                      </div>
                    </div>
                  </td>

                  <td className={styles.hallCell} data-label="Hall">
                    <div className={styles.hallInfo}>
                      <div className={styles.hallName}>{session.hallName}</div>
                      <div className={styles.capacity}>
                        {session.hallCapacity} seats
                      </div>
                    </div>
                  </td>

                  <td className={styles.timeCell} data-label="Time">
                    <div className={styles.timeInfo}>
                      <div className={styles.date}>{formatDate(session.startTime)}</div>
                      <div className={styles.time}>{formatTime(session.startTime)}</div>
                    </div>
                  </td>

                  <td className={styles.priceCell} data-label="Price">
                    <span className={styles.price}>
                      {formatCurrency(session.basePrice)}
                    </span>
                  </td>

                  <td className={styles.occupancyCell} data-label="Occupancy">
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
                  </td>

                  <td className={styles.revenueCell} data-label="Revenue">
                    <span className={styles.revenueInfo}>
                      {formatCurrency(session.totalRevenue)}
                    </span>
                  </td>

                  <td className={styles.statusCell} data-label="Status">
                    <Badge className={getStatusClass(session.status)}>
                      {getStatusText(session.status)}
                    </Badge>
                  </td>

                  <td className={styles.actionsCell} data-label="Actions">
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
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
