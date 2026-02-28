import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { sessionApi } from '@/api/sessionApi';
import type { SessionScheduleResponse } from '@/types/session';
import type { SearchParams } from '@/types/pagination';
import { Layout } from '@/components/layout/Layout/Layout';
import { DateFilter } from '@/components/sessions/DateFilter/DateFilter';
import { MovieFilter } from '@/components/sessions/MovieFilter/MovieFilter';
import { SessionList } from '@/components/sessions/SessionList/SessionList';
import { Button } from '@/components/ui/Button/Button';
import { Notification } from '@/components/ui/Notification/Notification';
import { useNotification } from '@/hooks/common/useNotification';
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
    const [availableDates, setAvailableDates] = useState<string[]>([]);
    const [initialLoad, setInitialLoad] = useState(true);

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
            const params: SearchParams = {
                page,
                size: 50,
                sort: 'startTime,asc',
                dateFrom: selectedDate,
                dateTo: selectedDate
            };

            if (selectedMovieId) {
                params.movieId = selectedMovieId;
            }

            const response = await sessionApi.public.getSessions(
                params.search || '',
                params.dateFrom,
                {
                    page: params.page,
                    size: params.size,
                    sort: params.sort
                }
            );

            const data = response?.data || null;

            setSessions(data?.content || []);
            setTotalPages(data?.totalPages || 0);
            setTotalElements(data?.totalElements || 0);
        } catch (err) {
            const errorMessage = err instanceof Error ? err.message : 'Failed to load sessions';
            setError(errorMessage);
            showNotification(errorMessage, 'error');
            console.error('Error fetching sessions:', err);
        } finally {
            setLoading(false);
        }
    }, [selectedDate, selectedMovieId, page, showNotification]);

    const fetchAvailableDates = useCallback(async () => {
        try {
            const params: SearchParams = {
                page: 0,
                size: 1000,
                sort: 'startTime,asc'
            };

            if (selectedMovieId) {
                params.movieId = selectedMovieId;
            }

            const response = await sessionApi.public.getSessions(
                params.search || '',
                params.dateFrom,
                {
                    page: params.page,
                    size: params.size,
                    sort: params.sort
                }
            );

            const data = response?.data || null;
            const sessionsData = data?.content || [];

            const dates = sessionsData
                .map(session => new Date(session.startTime).toISOString().split('T')[0])
                .filter((date, index, self) => self.indexOf(date) === index)
                .sort();

            setAvailableDates(dates);
        } catch (err) {
            console.error('Error fetching available dates:', err);
        }
    }, [selectedMovieId]);

    useEffect(() => {
        fetchSessions();
        fetchAvailableDates();
        setInitialLoad(false);
    }, [fetchSessions, fetchAvailableDates]);

    useEffect(() => {
        if (!initialLoad) {
            const params = new URLSearchParams();
            params.set('date', selectedDate);
            if (selectedMovieId) {
                params.set('movieId', selectedMovieId.toString());
            }
            if (page > 0) {
                params.set('page', page.toString());
            }
            setSearchParams(params, { replace: true });
        }
    }, [selectedDate, selectedMovieId, page, setSearchParams, initialLoad]);

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
        fetchAvailableDates();
    };

    const hasFilters = selectedMovieId !== undefined || selectedDate !== new Date().toISOString().split('T')[0];
    const hasSessions = sessions.length > 0;

    const uniqueMoviesCount = useMemo(() => {
        const movieIds = new Set(sessions.map(session => session.movieId));
        return movieIds.size;
    }, [sessions]);

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
                            sessionDates={availableDates}
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
                                {uniqueMoviesCount} movie{uniqueMoviesCount !== 1 ? 's' : ''} • {totalElements} session{totalElements !== 1 ? 's' : ''}
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
        </Layout>
    );
};

export default SessionsPage;