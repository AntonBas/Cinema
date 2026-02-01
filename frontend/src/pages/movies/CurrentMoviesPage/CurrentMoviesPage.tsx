import React, { useEffect, useRef, useState } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import type { MovieCardResponse } from '@/types/movie';
import { MovieList } from '@/components/movies';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui';
import styles from './CurrentMoviesPage.module.css';

export const CurrentMoviesPage: React.FC = () => {
    const { loading, getCurrentlyShowing } = useMovies();
    const [movies, setMovies] = useState<MovieCardResponse[]>([]);
    const { notifications, showNotification, hideNotification } = useNotification();

    const getCurrentlyShowingRef = useRef(getCurrentlyShowing);
    const showNotificationRef = useRef(showNotification);

    useEffect(() => {
        getCurrentlyShowingRef.current = getCurrentlyShowing;
        showNotificationRef.current = showNotification;
    }, [getCurrentlyShowing, showNotification]);

    useEffect(() => {
        let isMounted = true;

        const loadMovies = async () => {
            try {
                const data = await getCurrentlyShowingRef.current();
                if (isMounted) {
                    setMovies(data);
                }
            } catch (err) {
                if (isMounted) {
                    showNotificationRef.current('Failed to load movies', 'error');
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