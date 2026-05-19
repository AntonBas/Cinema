import React from "react";
import type { MovieCardResponse } from "@/types/movie";
import { MovieCard } from "./MovieCard/MovieCard";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import styles from "./MovieList.module.css";

interface MovieListProps {
  movies: MovieCardResponse[];
  onEdit: (movie: MovieCardResponse) => void;
  onDelete: (movie: MovieCardResponse) => void;
  onCreateNew?: () => void;
  loading?: boolean;
  emptyMessage?: string;
}

export const MovieList: React.FC<MovieListProps> = React.memo(
  ({
    movies,
    onEdit,
    onDelete,
    loading = false,
    emptyMessage = "No movies found",
  }) => {
    if (loading) {
      return (
        <div className={styles.loading}>
          <LoadingSpinner text="Loading movies..." />
        </div>
      );
    }

    if (!movies.length) {
      return (
        <div className={styles.empty}>
          <h3>{emptyMessage}</h3>
          <p>Get started by creating your first movie</p>
        </div>
      );
    }

    return (
      <div className={styles.grid}>
        {movies.map((movie) => (
          <MovieCard
            key={movie.id}
            movie={movie}
            onEdit={onEdit}
            onDelete={onDelete}
          />
        ))}
      </div>
    );
  },
);

MovieList.displayName = "MovieList";
