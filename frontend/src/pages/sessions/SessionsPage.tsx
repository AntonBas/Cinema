import React, { useState, useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { sessionApi } from '@/api/sessionApi';
import type { SessionScheduleResponse } from '@/types/session';
import { Layout } from '@/components/layout/Layout/Layout';
import { DateFilter } from '@/components/sessions/DateFilter/DateFilter';
import { MovieFilter } from '@/components/sessions/MovieFilter/MovieFilter';
import { SessionList } from '@/components/sessions/SessionList/SessionList';
import { Button } from '@/components/ui/Button/Button';
import styles from './SessionsPage.module.css';

const SessionsPage: React.FC = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [sessions, setSessions] = useState<SessionScheduleResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const dateParam = searchParams.get('date');
    const movieIdParam = searchParams.get('movieId');

    const today = new Date().toISOString().split('T')[0];
    const selectedDate = dateParam || today;
    const selectedMovieId = movieIdParam ? parseInt(movieIdParam) : undefined;

    useEffect(() => {
        const fetchSessions = async () => {
            setLoading(true);
            setError(null);

            try {
                const response = await sessionApi.public.getSchedule({ date: selectedDate });
                const data = response?.data || [];
                const filteredData = selectedMovieId
                    ? data.filter(s => s.movieId === selectedMovieId)
                    : data;
                setSessions(filteredData);
            } catch (err) {
                setError(err instanceof Error ? err.message : 'Failed to load sessions');
            } finally {
                setLoading(false);
            }
        };

        fetchSessions();
    }, [selectedDate, selectedMovieId]);

    const handleDateChange = (date: string) => {
        const params = new URLSearchParams(searchParams);
        params.set('date', date);
        setSearchParams(params);
    };

    const handleMovieChange = (movieId: number | undefined) => {
        const params = new URLSearchParams(searchParams);
        if (movieId) {
            params.set('movieId', movieId.toString());
        } else {
            params.delete('movieId');
        }
        setSearchParams(params);
    };

    const handleClearFilters = () => {
        setSearchParams(new URLSearchParams());
    };

    const availableDates = useMemo(() => {
        return sessions
            .map(s => s.startTime.split('T')[0])
            .filter((date, i, arr) => arr.indexOf(date) === i)
            .sort();
    }, [sessions]);

    const hasFilters = selectedMovieId !== undefined || selectedDate !== today;
    const uniqueMoviesCount = new Set(sessions.map(s => s.movieId)).size;

    return (
        <Layout>
            <div className={styles.container}>
                <div className={styles.header}>
                    <div>
                        <h1 className={styles.title}>Movie Sessions</h1>
                        <p className={styles.subtitle}>Browse available movie sessions and book your tickets</p>
                    </div>
                    {hasFilters && (
                        <Button variant="secondary" onClick={handleClearFilters}>
                            Clear Filters
                        </Button>
                    )}
                </div>

                <div className={styles.filtersSection}>
                    <DateFilter
                        selectedDate={selectedDate}
                        onDateChange={handleDateChange}
                        sessionDates={availableDates}
                    />
                    <MovieFilter selectedMovieId={selectedMovieId} onMovieChange={handleMovieChange} />
                </div>

                {error ? (
                    <div className={styles.errorContainer}>
                        <p className={styles.errorText}>{error}</p>
                        <Button variant="primary" onClick={() => window.location.reload()}>
                            Try Again
                        </Button>
                    </div>
                ) : loading ? (
                    <div className={styles.loadingContainer}>
                        <div className={styles.loadingSpinner}></div>
                        <p>Loading sessions...</p>
                    </div>
                ) : sessions.length > 0 ? (
                    <>
                        <div className={styles.resultsHeader}>
                            <span className={styles.resultsCount}>
                                {uniqueMoviesCount} movie{uniqueMoviesCount !== 1 ? 's' : ''} • {sessions.length} session{sessions.length !== 1 ? 's' : ''}
                            </span>
                        </div>
                        <SessionList sessions={sessions} />
                    </>
                ) : (
                    <div className={styles.emptyState}>
                        <div className={styles.emptyIllustration}>🎬</div>
                        <h3>No Sessions Available</h3>
                        <p>There are no movie sessions for the selected date and filters.</p>
                        <Button variant="primary" onClick={handleClearFilters}>
                            Show All Sessions
                        </Button>
                    </div>
                )}
            </div>
        </Layout>
    );
};

export default SessionsPage;