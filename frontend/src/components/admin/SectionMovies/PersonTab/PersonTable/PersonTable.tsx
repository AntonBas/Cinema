import React from "react";
import type { PersonListResponse } from "@/types/person";
import { PersonRoleDisplay } from "@/types/person";
import { Button, Badge } from "@/components/ui";
import styles from "./PersonTable.module.css";

interface PersonTableProps {
  persons: PersonListResponse[];
  onEdit: (person: PersonListResponse) => void;
  onDelete: (person: PersonListResponse) => void;
}

const ROLE_VARIANTS: Record<
  string,
  "success" | "primary" | "warning" | "secondary"
> = {
  ACTOR: "success",
  DIRECTOR: "primary",
  SCREENWRITER: "warning",
};

const getMovieCountText = (count: number): string =>
  `${count} ${count === 1 ? "movie" : "movies"}`;

export const PersonTable: React.FC<PersonTableProps> = React.memo(
  ({ persons, onEdit, onDelete }) => {
    if (persons.length === 0) {
      return (
        <div className={styles.empty}>
          <h3>No persons found</h3>
          <p>Add actors, directors, or screenwriters to get started!</p>
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
                <th>Role</th>
                <th>Movies</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {persons.map((person) => (
                <tr key={person.id}>
                  <td className={styles.nameCell} data-label="Name">
                    <span className={styles.name}>{person.name}</span>
                  </td>
                  <td className={styles.roleCell} data-label="Role">
                    <Badge variant={ROLE_VARIANTS[person.role] || "secondary"}>
                      {PersonRoleDisplay[person.role]}
                    </Badge>
                  </td>
                  <td className={styles.movieCountCell} data-label="Movies">
                    <Badge variant="primary">
                      {getMovieCountText(person.movieCount || 0)}
                    </Badge>
                  </td>
                  <td className={styles.actionsCell} data-label="Actions">
                    <div className={styles.actions}>
                      <Button
                        variant="success"
                        size="small"
                        onClick={() => onEdit(person)}
                        className={styles.actionButton}
                      >
                        Edit
                      </Button>
                      <Button
                        variant="error"
                        size="small"
                        onClick={() => onDelete(person)}
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

PersonTable.displayName = "PersonTable";
