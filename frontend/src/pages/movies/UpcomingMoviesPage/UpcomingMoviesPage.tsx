import React, { useEffect, useRef, useCallback } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies/MovieList/MovieList';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui/Notification/Notification';
import styles from './UpcomingMoviesPage.module.css';

export const UpcomingMoviesPage: React.FC = () => {
    const { publicUpcoming, loading, error, getPublicUpcoming } = useMovies();
    const { notifications, showNotification, hideNotification } = useNotification();
    const hasLoaded = useRef(false);

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
                movies={publicUpcoming}
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