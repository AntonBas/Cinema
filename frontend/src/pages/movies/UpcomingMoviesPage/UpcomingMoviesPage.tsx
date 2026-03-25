import React, { useEffect, useState } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { MovieList } from '@/components/movies/MovieList/MovieList';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui/Notification/Notification';
import styles from './UpcomingMoviesPage.module.css';

export const UpcomingMoviesPage: React.FC = () => {
    const [currentPage, setCurrentPage] = useState(0);
    const [allMovies, setAllMovies] = useState<any[]>([]);
    const [pagination, setPagination] = useState<any>(null);
    const { loading, error, getPublicUpcoming } = useMovies();
    const { notifications, showNotification, hideNotification } = useNotification();

    useEffect(() => {
        loadMovies(0);
    }, []);

    const loadMovies = async (page: number) => {
        try {
            const response = await getPublicUpcoming({ page, size: 12 });
            if (response) {
                if (page === 0) {
                    setAllMovies(response.content);
                } else {
                    setAllMovies(prev => [...prev, ...response.content]);
                }
                setPagination(response);
                setCurrentPage(page);
            }
        } catch (err) {
            showNotification('Failed to load upcoming movies', 'error');
        }
    };

    const handleLoadMore = () => {
        if (pagination && currentPage < pagination.totalPages - 1) {
            loadMovies(currentPage + 1);
        }
    };

    const handleRetry = () => {
        setAllMovies([]);
        loadMovies(0);
    };

    const errorObject = error ? new Error('Failed to load upcoming movies') : null;

    return (
        <div className={styles.page}>
            <MovieList
                movies={allMovies}
                pagination={pagination}
                loading={loading}
                error={errorObject}
                emptyMessage="No upcoming movies"
                onRetry={handleRetry}
                onLoadMore={handleLoadMore}
                variant="load-more"
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