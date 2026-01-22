import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { sessionApi } from '@/api/sessionApi';
import type { SessionScheduleResponse } from '@/types/session';
import type { PageResponse } from '@/types/pagination';
import { Layout } from '@/components/layout/Layout';
import { DateFilter } from '@/components/sessions/DateFilter';
import { MovieFilter } from '@/components/sessions/MovieFilter';
import { SessionList } from '@/components/sessions/SessionList';
import { Button } from '@/components/ui';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui/Notification';
import styles from './SessionsPage.module.css';

const SessionsPage: React.FC = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const navigate = useNavigate();
    const [sessions, setSessions] = useState<SessionScheduleResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [totalElements, setTotalElements] = useState(0);

    const { notifications, showNotification, hideNotification } = useNotification();

    const dateParam = searchParams.get('date');
    const movieIdParam = searchParams.get('movieId');

    const [selectedDate, setSelectedDate] = useState<string>(() => {
        return dateParam || new Date().toISOString().split('T')[0];
    });

    const [selectedMovieId, setSelectedMovieId] = useState<number | undefined>(() => {
        return movieIdParam ? parseInt(movieIdParam) : undefined;
    });

    const fetchSessions = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const response: PageResponse<SessionScheduleResponse> = await sessionApi.public.getSchedule(
                page,
                20,
                'startTime,asc',
                selectedDate,
                selectedMovieId,
                7
            );

            setSessions(response.content);
            setTotalPages(response.totalPages);
            setTotalElements(response.totalElements);
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to load sessions';
            setError(errorMessage);
            showNotification(errorMessage, 'error');
            console.error('Error fetching sessions:', err);
        } finally {
            setLoading(false);
        }
    }, [selectedDate, selectedMovieId, page, showNotification]);

    useEffect(() => {
        fetchSessions();
    }, [fetchSessions]);

    useEffect(() => {
        const params = new URLSearchParams();
        params.set('date', selectedDate);
        if (selectedMovieId) {
            params.set('movieId', selectedMovieId.toString());
        }
        if (page > 0) {
            params.set('page', page.toString());
        }
        setSearchParams(params, { replace: true });
    }, [selectedDate, selectedMovieId, page, setSearchParams]);

    const handleDateChange = (date: string) => {
        setSelectedDate(date);
        setPage(0);
    };

    const handleMovieChange = (movieId: number | undefined) => {
        setSelectedMovieId(movieId);
        setPage(0);
    };

    const handleClearFilters = () => {
        setSelectedDate(new Date().toISOString().split('T')[0]);
        setSelectedMovieId(undefined);
        setPage(0);
    };

    const handlePreviousPage = () => {
        if (page > 0) {
            setPage(page - 1);
        }
    };

    const handleNextPage = () => {
        if (page < totalPages - 1) {
            setPage(page + 1);
        }
    };

    const handleRefresh = () => {
        fetchSessions();
    };

    const hasFilters = selectedMovieId !== undefined || selectedDate !== new Date().toISOString().split('T')[0];
    const hasSessions = sessions.length > 0;

    return (
        <Layout>
            <div className={styles.container}>
                <div className={styles.header}>
                    <div>
                        <h1 className={styles.title}>Movie Sessions</h1>
                        <p className={styles.subtitle}>
                            Browse available movie sessions and book your tickets
                        </p>
                    </div>
                    <div className={styles.headerActions}>
                        {hasFilters && (
                            <Button
                                variant="secondary"
                                onClick={handleClearFilters}
                                className={styles.headerButton}
                            >
                                Clear Filters
                            </Button>
                        )}
                        <Button
                            variant="secondary"
                            onClick={handleRefresh}
                            disabled={loading}
                            className={styles.headerButton}
                        >
                            Refresh
                        </Button>
                    </div>
                </div>

                <div className={styles.filtersSection}>
                    <div className={styles.filterGroup}>
                        <DateFilter
                            selectedDate={selectedDate}
                            onDateChange={handleDateChange}
                        />
                    </div>
                    <div className={styles.filterGroup}>
                        <MovieFilter
                            selectedMovieId={selectedMovieId}
                            onMovieChange={handleMovieChange}
                        />
                    </div>
                </div>

                <div className={styles.resultsHeader}>
                    {hasSessions ? (
                        <div className={styles.resultsInfo}>
                            <span className={styles.resultsCount}>
                                Found {totalElements} session{totalElements !== 1 ? 's' : ''}
                            </span>
                            {selectedMovieId && (
                                <Button
                                    variant="secondary"
                                    onClick={() => navigate('/movies/current')}
                                    className={styles.browseMoviesButton}
                                >
                                    Browse All Movies
                                </Button>
                            )}
                        </div>
                    ) : !loading && !error ? (
                        <div className={styles.resultsInfo}>
                            <span className={styles.noResultsText}>No sessions found</span>
                        </div>
                    ) : null}
                </div>

                {error && !loading ? (
                    <div className={styles.errorContainer}>
                        <div className={styles.errorMessage}>
                            <p className={styles.errorText}>{error}</p>
                            <div className={styles.errorActions}>
                                <Button variant="primary" onClick={fetchSessions}>
                                    Try Again
                                </Button>
                                <Button variant="secondary" onClick={handleClearFilters}>
                                    Clear Filters
                                </Button>
                            </div>
                        </div>
                    </div>
                ) : loading ? (
                    <div className={styles.loadingContainer}>
                        <div className={styles.loadingSpinner}></div>
                        <p className={styles.loadingText}>Loading sessions...</p>
                    </div>
                ) : hasSessions ? (
                    <>
                        <SessionList sessions={sessions} />

                        {totalPages > 1 && (
                            <div className={styles.pagination}>
                                <Button
                                    variant="secondary"
                                    onClick={handlePreviousPage}
                                    disabled={page === 0}
                                    className={styles.paginationButton}
                                >
                                    ← Previous
                                </Button>
                                <div className={styles.paginationInfo}>
                                    <span className={styles.pageInfo}>
                                        Page {page + 1} of {totalPages}
                                    </span>
                                    <span className={styles.totalInfo}>
                                        {totalElements} total sessions
                                    </span>
                                </div>
                                <Button
                                    variant="secondary"
                                    onClick={handleNextPage}
                                    disabled={page >= totalPages - 1}
                                    className={styles.paginationButton}
                                >
                                    Next →
                                </Button>
                            </div>
                        )}
                    </>
                ) : (
                    <div className={styles.emptyState}>
                        <div className={styles.emptyIllustration}>🎬</div>
                        <h3 className={styles.emptyTitle}>No Sessions Available</h3>
                        <p className={styles.emptyText}>
                            There are no movie sessions for the selected date and filters.
                        </p>
                        <div className={styles.emptyActions}>
                            <Button variant="primary" onClick={handleClearFilters}>
                                Show All Sessions
                            </Button>
                            <Button variant="secondary" onClick={() => navigate('/movies/current')}>
                                Browse Movies
                            </Button>
                        </div>
                    </div>
                )}
            </div>

            {notifications.map((notification, index) => (
                <Notification
                    key={notification.id}
                    {...notification}
                    onClose={hideNotification}
                    position={index}
                />
            ))}
        </Layout>
    );
};

export default SessionsPage;