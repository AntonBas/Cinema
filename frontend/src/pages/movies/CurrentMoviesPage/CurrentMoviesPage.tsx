import React, { useEffect, useState, useCallback } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies/MovieList/MovieList';
import type { MovieCardResponse } from '@/types/movie';
import type { PageResponse } from '@/types/pagination';
import styles from './CurrentMoviesPage.module.css';

export const CurrentMoviesPage: React.FC = () => {
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<MovieCardResponse> | undefined>(undefined);
    const [error, setError] = useState<Error | null>(null);

    const { getCurrentlyShowing, loading } = useMovies();

    const loadMovies = useCallback(async (page: number) => {
        try {
            const response = await getCurrentlyShowing({ page, size: 12 });
            if (response) {
                if (page === 0) {
                    setMovies(response.content);
                } else {
                    setMovies(prev => [...prev, ...response.content]);
                }
                setPagination(response);
                setError(null);
            }
        } catch (err) {
            setError(err as Error);
        }
    }, [getCurrentlyShowing]);

    useEffect(() => {
        loadMovies(0);
    }, [loadMovies]);

    const handleLoadMore = () => {
        if (pagination && pagination.number < pagination.totalPages - 1) {
            loadMovies(pagination.number + 1);
        }
    };

    return (
        <div className={styles.page}>
            <MovieList
                movies={movies}
                pagination={pagination}
                loading={loading}
                error={error}
                emptyMessage="No movies currently playing"
                onRetry={() => loadMovies(0)}
                onLoadMore={handleLoadMore}
                variant="load-more"
            />
        </div>
    );
};