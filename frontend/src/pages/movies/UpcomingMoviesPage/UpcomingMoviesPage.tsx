import React, { useEffect, useRef, useState } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import type { MovieCardResponse } from '@/types/movie';
import { MovieList } from '@/components/movies';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui';
import styles from './UpcomingMoviesPage.module.css';

export const UpcomingMoviesPage: React.FC = () => {
    const { loading, getUpcoming } = useMovies();
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const { notifications, showNotification, hideNotification } = useNotification();

    const getUpcomingRef = useRef(getUpcoming);
    const showNotificationRef = useRef(showNotification);

    useEffect(() => {
        getUpcomingRef.current = getUpcoming;
        showNotificationRef.current = showNotification;
    }, [getUpcoming, showNotification]);

    useEffect(() => {
        let isMounted = true;

        const loadMovies = async () => {
            try {
                const data = await getUpcomingRef.current();
                if (isMounted) {
                    setMovies(data);
                }
            } catch (err) {
                if (isMounted) {
                    showNotificationRef.current('Failed to load upcoming movies', 'error');
                }
            }
        };

        if (isMounted) {
            loadMovies();
        }

        return () => {
            isMounted = false;
        };
    }, []);

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