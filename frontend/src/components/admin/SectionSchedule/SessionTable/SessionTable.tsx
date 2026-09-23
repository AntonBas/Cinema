import React from "react";
import { Pencil, Ban, RotateCcw, Trash2 } from "lucide-react";
import type { SessionAdminResponse } from "@/types/session";
import { Badge } from "@/components/ui";
import { ActionIconButton } from "@/components/admin/shared/ActionIconButton/ActionIconButton";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
import { formatPrice, formatShortDate, formatTime } from "@/utils/formatters";
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

export const SessionTable: React.FC<SessionTableProps> = ({
  sessions,
  onEdit,
  onDelete,
  onCancel,
  onReactivate,
}) => {
  if (!sessions.length) {
    return (
      <EmptyState
        title="No sessions found"
        message="There are currently no movie sessions matching your criteria."
      />
    );
  }

  return (
    <div className={tableStyles.wrapper}>
      <div className={tableStyles.container}>
        <table className={tableStyles.table}>
          <colgroup>
            <col style={{ width: "16%" }} />
            <col style={{ width: "10%" }} />
            <col style={{ width: "10%" }} />
            <col style={{ width: "10%" }} />
            <col style={{ width: "15%" }} />
            <col style={{ width: "11%" }} />
            <col style={{ width: "11%" }} />
            <col style={{ width: "17%" }} />
          </colgroup>
          <thead>
            <tr>
              <th>Movie</th>
              <th>Hall</th>
              <th>Time</th>
              <th>Price</th>
              <th>Occupancy</th>
              <th>Revenue</th>
              <th>Status</th>
              <th className={tableStyles.actionsCol}>Actions</th>
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
                  <td data-label="Movie">
                    <div className={styles.movieInfo}>
                      <div className={styles.movieTitle}>{session.movieTitle}</div>
                      <div className={styles.movieMeta}>
                        {session.movieDuration} min
                      </div>
                    </div>
                  </td>

                  <td data-label="Hall">
                    <div className={styles.hallInfo}>
                      <div className={styles.hallName}>{session.hallName}</div>
                      <div className={styles.capacity}>
                        {session.hallCapacity} seats
                      </div>
                    </div>
                  </td>

                  <td data-label="Time">
                    <div className={styles.timeInfo}>
                      <div className={styles.date}>{formatShortDate(session.startTime)}</div>
                      <div className={styles.time}>{formatTime(session.startTime)}</div>
                    </div>
                  </td>

                  <td data-label="Price">
                    <span className={styles.price}>
                      {formatPrice(session.basePrice)}
                    </span>
                  </td>

                  <td data-label="Occupancy">
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

                  <td data-label="Revenue">
                    <span className={styles.revenueInfo}>
                      {formatPrice(session.totalRevenue)}
                    </span>
                  </td>

                  <td data-label="Status">
                    <Badge className={getStatusClass(session.status)}>
                      {getStatusText(session.status)}
                    </Badge>
                  </td>

                  <td data-label="Actions">
                    <div className={tableStyles.actions}>
                      {editable && (
                        <ActionIconButton
                          icon={<Pencil />}
                          label="Edit session"
                          variant="success"
                          onClick={() => onEdit(session)}
                        />
                      )}
                      {cancellable && (
                        <ActionIconButton
                          icon={<Ban />}
                          label="Cancel session"
                          variant="secondary"
                          onClick={() => onCancel(session)}
                        />
                      )}
                      {reactivatable && (
                        <ActionIconButton
                          icon={<RotateCcw />}
                          label="Reactivate session"
                          variant="success"
                          onClick={() => onReactivate(session)}
                        />
                      )}
                      {deletable && (
                        <ActionIconButton
                          icon={<Trash2 />}
                          label="Delete session"
                          variant="error"
                          onClick={() => onDelete(session)}
                        />
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
