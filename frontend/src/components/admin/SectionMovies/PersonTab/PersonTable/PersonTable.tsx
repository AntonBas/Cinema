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
      <div className={styles.table}>
        <div className={styles.tableHeader}>
          <div>Name</div>
          <div>Role</div>
          <div>Movies</div>
          <div>Actions</div>
        </div>
        {persons.map((person) => (
          <div key={person.id} className={styles.tableRow}>
            <div className={styles.name}>
              <span>{person.name}</span>
            </div>
            <div className={styles.roleCell}>
              <Badge variant={ROLE_VARIANTS[person.role] || "secondary"}>
                {PersonRoleDisplay[person.role]}
              </Badge>
            </div>
            <div className={styles.movieCount}>
              <Badge variant="primary">
                {getMovieCountText(person.movieCount || 0)}
              </Badge>
            </div>
            <div className={styles.actions}>
              <Button
                variant="success"
                size="small"
                onClick={() => onEdit(person)}
              >
                Edit
              </Button>
              <Button
                variant="error"
                size="small"
                onClick={() => onDelete(person)}
              >
                Delete
              </Button>
            </div>
          </div>
        ))}
      </div>
    );
  },
);

PersonTable.displayName = "PersonTable";
