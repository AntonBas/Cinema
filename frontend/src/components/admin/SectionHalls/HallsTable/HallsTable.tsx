import React, { useCallback } from "react";
import type { CinemaHallListResponse } from "@/types/cinemaHall";
import { Button } from "@/components/ui/Button/Button";
import { Badge } from "@/components/ui/Badge/Badge";
import styles from "./HallsTable.module.css";

interface HallsTableProps {
  halls: CinemaHallListResponse[];
  onDelete: (hall: CinemaHallListResponse) => void;
  onShowLayout: (hall: CinemaHallListResponse) => void;
  onEdit?: (hall: CinemaHallListResponse) => void;
}

export const HallsTable: React.FC<HallsTableProps> = React.memo(
  ({ halls, onDelete, onShowLayout, onEdit }) => {
    const handleDelete = useCallback(
      (hall: CinemaHallListResponse) => {
        onDelete(hall);
      },
      [onDelete],
    );

    const handleShowLayout = useCallback(
      (hall: CinemaHallListResponse) => {
        onShowLayout(hall);
      },
      [onShowLayout],
    );

    const handleEdit = useCallback(
      (hall: CinemaHallListResponse) => {
        onEdit?.(hall);
      },
      [onEdit],
    );

    if (!halls.length) {
      return (
        <div className={styles.empty}>
          <h3>No Cinema Halls</h3>
          <p>Create your first cinema hall to get started</p>
        </div>
      );
    }

    return (
      <div className={styles.tableWrapper}>
        <div className={styles.tableContainer}>
          <table className={styles.table}>
            <thead>
              <tr>
                <th>Name</th>
                <th>Capacity</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {halls.map((hall) => (
                <tr key={hall.id}>
                  <td className={styles.nameCell} data-label="Name">
                    <span className={styles.name}>{hall.name}</span>
                  </td>
                  <td className={styles.capacityCell} data-label="Capacity">
                    <Badge variant="primary">{hall.capacity} seats</Badge>
                  </td>
                  <td className={styles.actionsCell} data-label="Actions">
                    <div className={styles.actions}>
                      <Button
                        variant="primary"
                        size="small"
                        onClick={() => handleShowLayout(hall)}
                        className={styles.actionButton}
                      >
                        Layout
                      </Button>
                      {onEdit && (
                        <Button
                          variant="success"
                          size="small"
                          onClick={() => handleEdit(hall)}
                          className={styles.actionButton}
                        >
                          Edit
                        </Button>
                      )}
                      <Button
                        variant="error"
                        size="small"
                        onClick={() => handleDelete(hall)}
                        className={styles.actionButton}
                      >
                        Delete
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    );
  },
);

HallsTable.displayName = "HallsTable";
