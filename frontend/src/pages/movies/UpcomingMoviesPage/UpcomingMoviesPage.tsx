import React, { useEffect, useState, useCallback } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies/MovieList/MovieList';
import type { MovieCardResponse } from '@/types/movie';
import type { PageResponse } from '@/types/pagination';
import styles from './UpcomingMoviesPage.module.css';

export const UpcomingMoviesPage: React.FC = () => {
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<MovieCardResponse> | undefined>(undefined);
    const [error, setError] = useState<Error | null>(null);

    const { getUpcoming, loading } = useMovies();

    const loadMovies = useCallback(async (page: number) => {
        try {
            const response = await getUpcoming({ page, size: 12 });
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
    }, [getUpcoming]);

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
                emptyMessage="No upcoming movies"
                onRetry={() => loadMovies(0)}
                onLoadMore={handleLoadMore}
                variant="load-more"
            />
        </div>
    );
};