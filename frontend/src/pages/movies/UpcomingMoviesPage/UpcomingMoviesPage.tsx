import React, { useEffect, useRef, useCallback } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies/MovieList/MovieList';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui';
import styles from './UpcomingMoviesPage.module.css';

export const UpcomingMoviesPage: React.FC = () => {
    const { upcoming, loading, error, getUpcoming } = useMovies();
    const { notifications, showNotification, hideNotification } = useNotification();
    const hasLoaded = useRef(false);

    const handleRetry = useCallback(() => {
        hasLoaded.current = false;
        getUpcoming();
    }, [getUpcoming]);

    useEffect(() => {
        if (!hasLoaded.current) {
            hasLoaded.current = true;
            getUpcoming().catch(() => {
                showNotification('Failed to load upcoming movies', 'error');
            });
        }
    }, [getUpcoming, showNotification]);

    return (
        <div className={styles.page}>
            <MovieList
                movies={upcoming}
                loading={loading}
                error={error}
                emptyMessage="No upcoming movies"
                onRetry={handleRetry}
            />

            {notifications.map((notification) => (
                <Notification
                    key={notification.id}
                    {...notification}
                    onClose={hideNotification}
                />
            ))}
        </div>
    );
};