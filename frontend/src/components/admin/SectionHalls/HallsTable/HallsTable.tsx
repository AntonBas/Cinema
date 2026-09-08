import React, { useCallback } from "react";
import { LayoutGrid, Pencil, Trash2 } from "lucide-react";
import type { CinemaHallListResponse } from "@/types/cinemaHall";
import { Badge } from "@/components/ui/Badge/Badge";
import { ActionIconButton } from "@/components/admin/shared/ActionIconButton/ActionIconButton";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
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
        <div className={tableStyles.empty}>
          <h3>No Cinema Halls</h3>
          <p>Create your first cinema hall to get started</p>
        </div>
      );
    }

    return (
      <div className={tableStyles.wrapper}>
        <div className={tableStyles.container}>
          <table className={tableStyles.table}>
            <colgroup>
              <col style={{ width: "40%" }} />
              <col style={{ width: "28%" }} />
              <col style={{ width: "32%" }} />
            </colgroup>
            <thead>
              <tr>
                <th>Name</th>
                <th>Capacity</th>
                <th className={tableStyles.actionsCol}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {halls.map((hall) => (
                <tr key={hall.id}>
                  <td data-label="Name">
                    <span className={styles.name}>{hall.name}</span>
                  </td>
                  <td data-label="Capacity">
                    <Badge variant="primary">{hall.capacity} seats</Badge>
                  </td>
                  <td data-label="Actions">
                    <div className={tableStyles.actions}>
                      <ActionIconButton
                        icon={<LayoutGrid />}
                        label="Seat layout"
                        variant="primary"
                        onClick={() => handleShowLayout(hall)}
                      />
                      {onEdit && (
                        <ActionIconButton
                          icon={<Pencil />}
                          label="Edit hall"
                          variant="success"
                          onClick={() => handleEdit(hall)}
                        />
                      )}
                      <ActionIconButton
                        icon={<Trash2 />}
                        label="Delete hall"
                        variant="error"
                        onClick={() => handleDelete(hall)}
                      />
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
