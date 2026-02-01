import React, { useEffect } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui';
import styles from './UpcomingMoviesPage.module.css';

export const UpcomingMoviesPage: React.FC = () => {
    const { movies, loading, getUpcoming } = useMovies();
    const { notifications, showNotification, hideNotification } = useNotification();

    useEffect(() => {
        const loadMovies = async () => {
            try {
                await getUpcoming();
            } catch (err) {
                showNotification('Failed to load upcoming movies', 'error');
            }
        };
        loadMovies();
    }, [getUpcoming, showNotification]);

    return (
        <div className={styles.page}>
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