import React from 'react';
import type { MovieCardResponse } from '@/types/movie';
import type { PageResponse } from '@/types/pagination';
import { MovieCard } from '../MovieCard/MovieCard';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { Button } from '@/components/ui/Button/Button';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import styles from './MovieList.module.css';

interface MovieListProps {
    movies: MovieCardResponse[];
    pagination?: PageResponse<MovieCardResponse>;
    loading?: boolean;
    error?: Error | null;
    emptyMessage?: string;
    onRetry?: () => void;
    onPageChange?: (page: number) => void;
    onLoadMore?: () => void;
    variant?: 'pages' | 'load-more';
}

export const MovieList: React.FC<MovieListProps> = React.memo(({
    movies,
    pagination,
    loading = false,
    error = null,
    emptyMessage = "No movies found",
    onRetry,
    onPageChange,
    onLoadMore,
    variant = 'pages'
}) => {
    if (error) {
        return (
            <div className={styles.error} role="alert">
                <div className={styles.errorIcon}>⚠️</div>
                <h3>Error loading movies</h3>
                <p>{error.message}</p>
                {onRetry && (
                    <Button
                        variant="primary"
                        onClick={onRetry}
                        aria-label="Try again"
                    >
                        Try Again
                    </Button>
                )}
            </div>
        );
    }

    if (loading && movies.length === 0) {
        return (
            <div className={styles.loading} aria-live="polite" aria-busy="true">
                <LoadingSpinner text="Loading movies..." />
            </div>
        );
    }

    if (!movies || movies.length === 0) {
        return (
            <div className={styles.empty} role="status">
                <div className={styles.emptyIcon}>🎬</div>
                <h3>{emptyMessage}</h3>
                <p>Try checking back later for new releases.</p>
            </div>
        );
    }

    const currentPage = pagination?.number ?? 0;
    const totalPages = pagination?.totalPages ?? 0;
    const hasMore = pagination && currentPage < totalPages - 1;

    return (
        <div className={styles.container}>
            <div className={styles.grid} role="list" aria-label="Movies list">
                {movies.map(movie => (
                    <div key={movie.id} role="listitem">
                        <MovieCard movie={movie} />
                    </div>
                ))}
            </div>

            {loading && movies.length > 0 && (
                <div className={styles.loadingMore}>
                    <LoadingSpinner text="Loading more movies..." />
                </div>
            )}

            {pagination && totalPages > 1 && (
                <div className={styles.paginationContainer}>
                    {variant === 'load-more' && onLoadMore && hasMore && !loading && (
                        <Pagination
                            variant="load-more"
                            currentPage={currentPage}
                            totalPages={totalPages}
                            totalElements={pagination.totalElements}
                            pageSize={pagination.size}
                            onLoadMore={onLoadMore}
                            loading={loading}
                        />
                    )}

                    {variant === 'pages' && onPageChange && (
                        <Pagination
                            variant="pages"
                            currentPage={currentPage}
                            totalPages={totalPages}
                            totalElements={pagination.totalElements}
                            pageSize={pagination.size}
                            onPageChange={onPageChange}
                            loading={loading}
                        />
                    )}
                </div>
            )}
        </div>
    );
});

MovieList.displayName = 'MovieList';