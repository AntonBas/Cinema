import React from "react";
import type { MovieCardResponse } from "@/types/movie";
import type { PageResponse } from "@/types/pagination";
import { MovieCard } from "../MovieCard/MovieCard";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { Button } from "@/components/ui/Button/Button";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import styles from "./MovieList.module.css";

interface MovieListProps {
  movies: MovieCardResponse[];
  pagination?: PageResponse<MovieCardResponse>;
  loading?: boolean;
  error?: Error | null;
  emptyMessage?: string;
  onRetry?: () => void;
  onPageChange?: (page: number) => void;
  onLoadMore?: () => void;
  variant?: "pages" | "load-more";
}

export const MovieList: React.FC<MovieListProps> = React.memo(
  ({
    movies,
    pagination,
    loading = false,
    error = null,
    emptyMessage = "No movies found",
    onRetry,
    onPageChange,
    onLoadMore,
    variant = "pages",
  }) => {
    if (error) {
      return (
        <EmptyState
          variant="error"
          title="Error loading movies"
          message={error.message}
          action={
            onRetry && (
              <Button variant="primary" onClick={onRetry}>
                Try Again
              </Button>
            )
          }
        />
      );
    }

    if (loading && movies.length === 0) {
      return (
        <div className={styles.loading}>
          <LoadingSpinner text="Loading movies..." />
        </div>
      );
    }

    if (!movies.length) {
      return (
        <EmptyState
          title={emptyMessage}
          message="Check back soon for new movies."
        />
      );
    }

    return (
      <div className={styles.container}>
        <div className={styles.grid}>
          {movies.map((movie) => (
            <MovieCard key={movie.id} movie={movie} />
          ))}
        </div>

        {loading && movies.length > 0 && (
          <div className={styles.loadingMore}>
            <LoadingSpinner text="Loading more movies..." />
          </div>
        )}

        {pagination && pagination.totalPages > 1 && (
          <div className={styles.paginationContainer}>
            {variant === "pages" && onPageChange && (
              <Pagination
                variant="pages"
                currentPage={pagination.number}
                totalPages={pagination.totalPages}
                totalElements={pagination.totalElements}
                pageSize={pagination.size}
                onPageChange={onPageChange}
                loading={loading}
              />
            )}
            {variant === "load-more" && onLoadMore && (
              <Pagination
                variant="load-more"
                currentPage={pagination.number}
                totalPages={pagination.totalPages}
                totalElements={pagination.totalElements}
                pageSize={pagination.size}
                onLoadMore={onLoadMore}
                loading={loading}
              />
            )}
          </div>
        )}
      </div>
    );
  },
);

MovieList.displayName = "MovieList";
