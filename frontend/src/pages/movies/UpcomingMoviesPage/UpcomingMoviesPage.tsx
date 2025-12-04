import React, { useEffect } from 'react';
import { useMovieStatus } from '@/hooks/features/movies';
import { MovieList } from '@/components/movies';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui';
import styles from './UpcomingMoviesPage.module.css';

export const UpcomingMoviesPage: React.FC = () => {
    const { movies, loading, error, fetchMoviesByStatus } = useMovieStatus();
    const { notifications, showNotification, hideNotification } = useNotification();

    useEffect(() => {
        fetchMoviesByStatus('upcoming');
    }, [fetchMoviesByStatus]);

    useEffect(() => {
        if (error) {
            showNotification(error, 'error');
        }
    }, [error, showNotification]);

    return (
        <div className={styles.page}>
            <div className={styles.header}>
                <h1>Coming Soon</h1>
                <p>Upcoming movies in cinemas</p>
            </div>

            <MovieList
                movies={movies}
                loading={loading}
                emptyMessage="No upcoming movies"
            />

            {notifications.map((notification, index) => (
                <Notification
                    key={notification.id}
                    {...notification}
                    onClose={hideNotification}
                    position={index}
                />
            ))}
        </div>
    );
};