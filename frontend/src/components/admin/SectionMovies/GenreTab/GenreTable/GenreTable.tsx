import React from "react";
import { Pencil, Trash2 } from "lucide-react";
import type { GenreListResponse } from "@/types/genre";
import { Badge } from "@/components/ui";
import { ActionIconButton } from "@/components/admin/shared/ActionIconButton/ActionIconButton";
import tableStyles from "@/components/admin/shared/AdminTable/AdminTable.module.css";
import styles from "./GenreTable.module.css";

interface GenreTableProps {
  genres: GenreListResponse[];
  onEdit: (genre: GenreListResponse) => void;
  onDelete: (genre: GenreListResponse) => void;
}

const getMovieCountText = (count: number): string =>
  `${count} ${count === 1 ? "movie" : "movies"}`;

export const GenreTable: React.FC<GenreTableProps> = React.memo(
  ({ genres, onEdit, onDelete }) => {
    if (genres.length === 0) {
      return (
        <div className={tableStyles.empty}>
          <h3>No genres found</h3>
          <p>Create your first genre to get started!</p>
        </div>
      );
    }

    return (
      <div className={tableStyles.wrapper}>
        <div className={tableStyles.container}>
          <table className={tableStyles.table}>
            <colgroup>
              <col style={{ width: "45%" }} />
              <col style={{ width: "25%" }} />
              <col style={{ width: "30%" }} />
            </colgroup>
            <thead>
              <tr>
                <th>Name</th>
                <th>Movies</th>
                <th className={tableStyles.actionsCol}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {genres.map((genre) => (
                <tr key={genre.id}>
                  <td data-label="Name">
                    <span className={styles.name}>{genre.name}</span>
                  </td>
                  <td data-label="Movies">
                    <Badge variant="primary">
                      {getMovieCountText(genre.movieCount)}
                    </Badge>
                  </td>
                  <td data-label="Actions">
                    <div className={tableStyles.actions}>
                      <ActionIconButton
                        icon={<Pencil />}
                        label="Edit genre"
                        variant="success"
                        onClick={() => onEdit(genre)}
                      />
                      <ActionIconButton
                        icon={<Trash2 />}
                        label="Delete genre"
                        variant="error"
                        onClick={() => onDelete(genre)}
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

GenreTable.displayName = "GenreTable";
