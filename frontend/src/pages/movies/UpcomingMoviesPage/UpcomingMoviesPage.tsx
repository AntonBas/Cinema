import React, { useEffect, useRef, useState, useCallback } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies/MovieList/MovieList';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui/Notification/Notification';
import type { MovieCardResponse } from '@/types/movie';
import styles from './UpcomingMoviesPage.module.css';

export const UpcomingMoviesPage: React.FC = () => {
    const { publicUpcoming, loading, error, getPublicUpcoming } = useMovies();
    const { notifications, showNotification, hideNotification } = useNotification();
    const [moviesList, setMoviesList] = useState<MovieCardResponse[]>([]);
    const hasLoaded = useRef(false);
    const prevMoviesRef = useRef<MovieCardResponse[]>([]);

    useEffect(() => {
        const hasChanged =
            publicUpcoming?.length !== prevMoviesRef.current.length ||
            publicUpcoming?.some((movie, index) => movie.id !== prevMoviesRef.current[index]?.id);

        if (hasChanged && Array.isArray(publicUpcoming)) {
            setMoviesList(publicUpcoming);
            prevMoviesRef.current = publicUpcoming;
        }
    }, [publicUpcoming]);

    const handleRetry = useCallback(() => {
        hasLoaded.current = false;
        getPublicUpcoming();
    }, [getPublicUpcoming]);

    useEffect(() => {
        if (!hasLoaded.current) {
            hasLoaded.current = true;
            getPublicUpcoming().catch(() => {
                showNotification('Failed to load upcoming movies', 'error');
            });
        }
    }, [getPublicUpcoming, showNotification]);

    const errorObject = error ? new Error('Failed to load upcoming movies') : null;

    return (
        <div className={styles.page}>
            <MovieList
                movies={moviesList}
                loading={loading}
                error={errorObject}
                emptyMessage="No upcoming movies"
                onRetry={handleRetry}
            />

            {notifications.map((notification) => (
                <Notification
                    key={notification.id}
                    id={notification.id}
                    message={notification.message}
                    type={notification.type}
                    isVisible={notification.isVisible}
                    onClose={hideNotification}
                    duration={notification.duration}
                />
            ))}
        </div>
    );
};