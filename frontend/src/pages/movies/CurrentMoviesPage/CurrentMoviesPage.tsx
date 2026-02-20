import React, { useEffect, useRef } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies/MovieList/MovieList';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui';
import styles from './CurrentMoviesPage.module.css';

export const CurrentMoviesPage: React.FC = () => {
    const { currentlyShowing, loading, error, getCurrentlyShowing } = useMovies();
    const { notifications, showNotification, hideNotification } = useNotification();
    const hasLoaded = useRef(false);

    useEffect(() => {
        if (!hasLoaded.current) {
            hasLoaded.current = true;
            getCurrentlyShowing().catch(() => {
                showNotification('Failed to load movies', 'error');
            });
        }
    }, [getCurrentlyShowing, showNotification]);

    return (
        <div className={styles.page}>
            <MovieList
                movies={currentlyShowing}
                loading={loading}
                error={error}
                emptyMessage="No movies currently playing"
                onRetry={() => {
                    hasLoaded.current = false;
                    getCurrentlyShowing();
                }}
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