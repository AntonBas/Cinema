import React, { useEffect } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui';
import styles from './CurrentMoviesPage.module.css';

export const CurrentMoviesPage: React.FC = () => {
    const { movies, loading, getCurrentlyShowing } = useMovies();
    const { notifications, showNotification, hideNotification } = useNotification();

    useEffect(() => {
        const loadMovies = async () => {
            try {
                await getCurrentlyShowing();
            } catch (err) {
                showNotification('Failed to load movies', 'error');
            }
        };
        loadMovies();
    }, [getCurrentlyShowing, showNotification]);

    return (
        <div className={styles.page}>
            <MovieList
                movies={movies}
                loading={loading}
                emptyMessage="No movies currently playing"
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