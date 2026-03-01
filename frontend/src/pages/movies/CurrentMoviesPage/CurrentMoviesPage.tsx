import React, { useEffect, useRef, useState } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies/MovieList/MovieList';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui/Notification/Notification';
import type { MovieCardResponse } from '@/types/movie';
import styles from './CurrentMoviesPage.module.css';

export const CurrentMoviesPage: React.FC = () => {
    const { publicCurrent, loading, error, getPublicCurrent } = useMovies();
    const { notifications, showNotification, hideNotification } = useNotification();
    const [moviesList, setMoviesList] = useState<MovieCardResponse[]>([]);
    const hasLoaded = useRef(false);

    console.log('🎬 CurrentMoviesPage render');
    console.log('🎬 publicCurrent from hook:', publicCurrent);
    console.log('🎬 moviesList state:', moviesList);
    console.log('🎬 loading:', loading);

    useEffect(() => {
        console.log('🎬 useEffect - publicCurrent changed:', publicCurrent);
        if (publicCurrent && Array.isArray(publicCurrent)) {
            console.log('🎬 Setting moviesList with:', publicCurrent);
            setMoviesList(publicCurrent);
        }
    }, [publicCurrent]);

    useEffect(() => {
        if (!hasLoaded.current) {
            console.log('🎬 First load - calling getPublicCurrent');
            hasLoaded.current = true;
            getPublicCurrent().catch(() => {
                showNotification('Failed to load movies', 'error');
            });
        }
    }, [getPublicCurrent, showNotification]);

    const errorObject = error ? new Error('Failed to load movies') : null;

    const handleRetry = () => {
        console.log('🎬 Retry clicked');
        hasLoaded.current = false;
        getPublicCurrent();
    };

    return (
        <div className={styles.page}>
            <MovieList
                movies={moviesList}
                loading={loading}
                error={errorObject}
                emptyMessage="No movies currently playing"
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