import React from "react";
import type { MovieCardResponse } from "@/features/movie/types/movie";
import type { PageResponse } from "@/types/pagination";
import { MovieCard } from "../MovieCard/MovieCard";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { Button } from "@/components/ui/Button/Button";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { EmptyState } from "@/components/ui/EmptyState/EmptyState";
import { useGridColumns } from "@/hooks/common/useGridColumns";
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
    const { ref: gridRef, columns } = useGridColumns<HTMLDivElement>();

    if (error) {
      return (
        <EmptyState
          variant="error"
          title="Error Loading Movies"
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

    const hasMore =
      variant === "load-more" &&
      !!pagination &&
      pagination.number < pagination.totalPages - 1;
    const fullRowsCount =
      columns > 0 ? Math.floor(movies.length / columns) * columns : 0;
    const showAllInGrid = columns === 0 || (fullRowsCount === 0 && hasMore);
    const gridMovies = showAllInGrid ? movies : movies.slice(0, fullRowsCount);
    const lastRowMovies =
      hasMore || showAllInGrid ? [] : movies.slice(fullRowsCount);

    return (
      <div className={styles.container}>
        <div ref={gridRef} className={styles.grid}>
          {gridMovies.map((movie) => (
            <MovieCard key={movie.id} movie={movie} />
          ))}
        </div>

        {lastRowMovies.length > 0 && (
          <div
            className={styles.lastRow}
            style={{ "--columns": columns } as React.CSSProperties}
          >
            {lastRowMovies.map((movie) => (
              <div key={movie.id} className={styles.lastRowItem}>
                <MovieCard movie={movie} />
              </div>
            ))}
          </div>
        )}

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
