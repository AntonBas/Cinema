import React, { useEffect } from 'react';
import { useMovieStatus } from '@/hooks/features/movies';
import { MovieList } from '@/components/movies';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui';

export const UpcomingMoviesPage: React.FC = () => {
    const { movies, loading, error, fetchMoviesByStatus } = useMovieStatus();
    const { notifications, showNotification, hideNotification } = useNotification();

    useEffect(() => {
        fetchMoviesByStatus('UPCOMING');
    }, [fetchMoviesByStatus]);

    useEffect(() => {
        if (error) {
            showNotification(error, 'error');
        }
    }, [error, showNotification]);

    return (
        <div className="page">
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