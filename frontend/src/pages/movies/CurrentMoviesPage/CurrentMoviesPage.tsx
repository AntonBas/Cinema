import React, { useEffect, useRef } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies/MovieList/MovieList';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui';
import styles from './CurrentMoviesPage.module.css';

export const CurrentMoviesPage: React.FC = () => {
    const { publicCurrent, loading, error, getPublicCurrent } = useMovies();
    const { notifications, showNotification, hideNotification } = useNotification();
    const hasLoaded = useRef(false);

    useEffect(() => {
        if (!hasLoaded.current) {
            hasLoaded.current = true;
            getPublicCurrent().catch(() => {
                showNotification('Failed to load movies', 'error');
            });
        }
    }, [getPublicCurrent, showNotification]);

    const errorObject = error ? new Error('Failed to load movies') : null;

    return (
        <div className={styles.page}>
            <MovieList
                movies={publicCurrent}
                loading={loading}
                error={errorObject}
                emptyMessage="No movies currently playing"
                onRetry={() => {
                    hasLoaded.current = false;
                    getPublicCurrent();
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