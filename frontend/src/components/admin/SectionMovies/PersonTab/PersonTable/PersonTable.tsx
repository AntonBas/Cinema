import React from "react";
import { Pencil, Trash2 } from "lucide-react";
import type { PersonListResponse } from "@/types/person";
import { PersonRoleDisplay } from "@/types/person";
import { Badge } from "@/components/ui";
import { ActionIconButton } from "@/components/admin/shared/ActionIconButton/ActionIconButton";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
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
        <div className={tableStyles.empty}>
          <h3>No persons found</h3>
          <p>Add actors, directors, or screenwriters to get started!</p>
        </div>
      );
    }

    return (
      <div className={tableStyles.wrapper}>
        <div className={tableStyles.container}>
          <table className={tableStyles.table}>
            <colgroup>
              <col style={{ width: "34%" }} />
              <col style={{ width: "20%" }} />
              <col style={{ width: "18%" }} />
              <col style={{ width: "28%" }} />
            </colgroup>
            <thead>
              <tr>
                <th>Name</th>
                <th>Role</th>
                <th>Movies</th>
                <th className={tableStyles.actionsCol}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {persons.map((person) => (
                <tr key={person.id}>
                  <td data-label="Name">
                    <span className={styles.name}>{person.name}</span>
                  </td>
                  <td data-label="Role">
                    <Badge variant={ROLE_VARIANTS[person.role] || "secondary"}>
                      {PersonRoleDisplay[person.role]}
                    </Badge>
                  </td>
                  <td data-label="Movies">
                    <Badge variant="primary">
                      {getMovieCountText(person.movieCount || 0)}
                    </Badge>
                  </td>
                  <td data-label="Actions">
                    <div className={tableStyles.actions}>
                      <ActionIconButton
                        icon={<Pencil />}
                        label="Edit person"
                        variant="success"
                        onClick={() => onEdit(person)}
                      />
                      <ActionIconButton
                        icon={<Trash2 />}
                        label="Delete person"
                        variant="error"
                        onClick={() => onDelete(person)}
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

PersonTable.displayName = "PersonTable";
