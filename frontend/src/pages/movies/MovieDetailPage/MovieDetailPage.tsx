import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { movieApi } from '@/api/movieApi';
import { sessionApi } from '@/api/sessionApi';
import type { MovieDetailResponse } from '@/types/movie';
import type { SessionScheduleResponse } from '@/types/session';
import {
    AgeRatingDisplay,
    AgeRatingDescription,
    MovieStatusDisplay
} from '@/types/movie';
import { useNotification } from '@/hooks/common/useNotification';
import { Button } from '@/components/ui/Button/Button';
import { Notification } from '@/components/ui/Notification/Notification';
import { Layout } from '@/components/layout/Layout/Layout';
import styles from './MovieDetailPage.module.css';

export const MovieDetailPage: React.FC = () => {
    const { slug } = useParams<{ slug: string }>();
    const navigate = useNavigate();
    const [movie, setMovie] = useState<MovieDetailResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [findingSession, setFindingSession] = useState(false);
    const [nextSessionDate, setNextSessionDate] = useState<string | null>(null);
    const [nextSessionTime, setNextSessionTime] = useState<string | null>(null);
    const [hasSessions, setHasSessions] = useState<boolean>(true);
    const isMounted = useRef(true);

    const { notifications, showNotification, hideNotification } = useNotification();
    const showNotificationRef = useRef(showNotification);

    useEffect(() => {
        showNotificationRef.current = showNotification;
    }, [showNotification]);

    const findNextSessionDate = useCallback(async (movieId: number, movieTitle: string) => {
        try {
            const now = new Date();

            const response = await sessionApi.public.getSessions(
                movieTitle,
                undefined,
                {
                    page: 0,
                    size: 100,
                    sort: 'startTime,asc'
                }
            );

            const sessions = response?.data?.content || [];

            const upcomingSessions = sessions.filter((session: SessionScheduleResponse) => {
                const sessionTime = new Date(session.startTime);
                return session.movieId === movieId &&
                    sessionTime > now &&
                    session.status === 'SCHEDULED';
            });

            if (upcomingSessions.length > 0) {
                const nearestSession = upcomingSessions.reduce((nearest: SessionScheduleResponse, current: SessionScheduleResponse) => {
                    const nearestTime = new Date(nearest.startTime).getTime();
                    const currentTime = new Date(current.startTime).getTime();
                    return currentTime < nearestTime ? current : nearest;
                });

                const sessionDateTime = new Date(nearestSession.startTime);
                const sessionDate = sessionDateTime.toISOString().split('T')[0];
                const sessionTimeStr = sessionDateTime.toLocaleTimeString('en-US', {
                    hour: '2-digit',
                    minute: '2-digit',
                    hour12: false
                });

                if (isMounted.current) {
                    setNextSessionDate(sessionDate);
                    setNextSessionTime(sessionTimeStr);
                    setHasSessions(true);
                }
            } else {
                if (isMounted.current) {
                    setNextSessionDate(null);
                    setNextSessionTime(null);
                    setHasSessions(false);
                }
            }
        } catch {
            if (isMounted.current) {
                setNextSessionDate(null);
                setNextSessionTime(null);
                setHasSessions(false);
            }
        }
    }, []);

    useEffect(() => {
        isMounted.current = true;

        const fetchMovie = async () => {
            if (!slug) return;

            try {
                setLoading(true);
                const response = await movieApi.public.getBySlug(slug);
                const movieData = response?.data || null;

                if (isMounted.current) {
                    setMovie(movieData);
                }

                if (movieData?.id && isMounted.current) {
                    await findNextSessionDate(movieData.id, movieData.title);
                }
            } catch {
                if (isMounted.current) {
                    showNotificationRef.current('Failed to load movie', 'error');
                }
            } finally {
                if (isMounted.current) {
                    setLoading(false);
                }
            }
        };

        fetchMovie();

        return () => {
            isMounted.current = false;
        };
    }, [slug, findNextSessionDate]);

    const getPosterUrl = useCallback((movieId: number): string => {
        return `/api/movies/${movieId}/poster`;
    }, []);

    const handleFindSession = async () => {
        if (!movie) return;

        setFindingSession(true);
        try {
            if (nextSessionDate) {
                navigate(`/schedule?movieId=${movie.id}&date=${nextSessionDate}`);
            } else {
                const today = new Date().toISOString().split('T')[0];
                navigate(`/schedule?movieId=${movie.id}&date=${today}`);
            }
        } catch {
            showNotificationRef.current('Error finding sessions', 'error');
        } finally {
            setFindingSession(false);
        }
    };

    const formatDate = (dateString: string): string => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    const formatButtonDate = (dateString: string, timeString: string | null): string => {
        const date = new Date(dateString);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const dateOnly = new Date(date.getFullYear(), date.getMonth(), date.getDate());

        const diffDays = Math.ceil((dateOnly.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

        if (diffDays === 0) {
            return `Today at ${timeString}`;
        }
        if (diffDays === 1) {
            return `Tomorrow at ${timeString}`;
        }
        if (diffDays < 7) {
            const weekday = date.toLocaleDateString('en-US', { weekday: 'long' });
            return `${weekday} at ${timeString}`;
        }

        const formattedDate = date.toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric'
        });
        return `${formattedDate} at ${timeString}`;
    };

    if (loading) {
        return (
            <Layout>
                <div className={styles.loadingContainer}>
                    <div className={styles.loadingSpinner}></div>
                    <p>Loading movie details...</p>
                </div>
            </Layout>
        );
    }

    if (!movie) {
        return (
            <Layout>
                <div className={styles.errorContainer}>
                    <h2>Movie not found</h2>
                    <p>The movie you're looking for doesn't exist or has been removed.</p>
                    <Button variant="primary" onClick={() => navigate('/movies/current')}>
                        Browse Current Movies
                    </Button>
                </div>
            </Layout>
        );
    }

    const getFindSessionButtonText = () => {
        if (findingSession) return 'Finding...';
        if (nextSessionDate && nextSessionTime) {
            return `Book Session: ${formatButtonDate(nextSessionDate, nextSessionTime)}`;
        }
        if (!hasSessions) {
            return 'No sessions available';
        }
        return 'Find Available Sessions';
    };

    return (
        <Layout>
            <div className={styles.container}>
                <div className={styles.content}>
                    <div className={styles.leftColumn}>
                        <div className={styles.posterContainer}>
                            <img
                                src={getPosterUrl(movie.id)}
                                alt={movie.title}
                                className={styles.poster}
                                onError={(e) => {
                                    (e.target as HTMLImageElement).src = '/placeholder-poster.jpg';
                                }}
                            />
                            <div className={styles.statusBadge}>
                                <span className={`${styles.status} ${styles[movie.status.toLowerCase()]}`}>
                                    {MovieStatusDisplay[movie.status]}
                                </span>
                            </div>
                        </div>

                        <div className={styles.actionButtons}>
                            <Button
                                variant="primary"
                                onClick={handleFindSession}
                                className={styles.actionButton}
                                loading={findingSession}
                                disabled={!hasSessions && !nextSessionDate}
                            >
                                {getFindSessionButtonText()}
                            </Button>

                            {movie.trailerUrl && (
                                <Button
                                    variant="secondary"
                                    onClick={() => window.open(movie.trailerUrl, '_blank')}
                                    className={styles.trailerButton}
                                >
                                    Watch Trailer
                                </Button>
                            )}
                        </div>
                    </div>

                    <div className={styles.rightColumn}>
                        <div className={styles.titleSection}>
                            <div className={styles.titleContent}>
                                <h1 className={styles.title}>{movie.title}</h1>
                                <div className={styles.metaInfo}>
                                    <div
                                        className={styles.ageRating}
                                        title={AgeRatingDescription[movie.ageRating]}
                                    >
                                        {AgeRatingDisplay[movie.ageRating]}
                                    </div>
                                    <span className={styles.duration}>
                                        {movie.durationMinutes} min
                                    </span>
                                </div>
                            </div>
                        </div>

                        <div className={styles.infoSection}>
                            <div className={styles.infoRow}>
                                <span className={styles.infoLabel}>Release Date:</span>
                                <div className={styles.infoValue}>
                                    <div className={styles.dateItem}>
                                        <div className={styles.dateValue}>{formatDate(movie.releaseDate)}</div>
                                    </div>
                                </div>
                            </div>

                            <div className={styles.infoRow}>
                                <span className={styles.infoLabel}>End Showing:</span>
                                <div className={styles.infoValue}>
                                    <div className={styles.dateItem}>
                                        <div className={styles.dateValue}>{formatDate(movie.endShowingDate)}</div>
                                    </div>
                                </div>
                            </div>

                            {movie.genres && movie.genres.length > 0 && (
                                <div className={styles.infoRow}>
                                    <span className={styles.infoLabel}>Genres:</span>
                                    <div className={styles.infoValue}>
                                        <div className={styles.genreList}>
                                            {movie.genres.map((genre: any) => (
                                                <span key={genre.id} className={styles.genreItem}>
                                                    {genre.name}
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            )}

                            {movie.directors && movie.directors.length > 0 && (
                                <div className={styles.infoRow}>
                                    <span className={styles.infoLabel}>Director{movie.directors.length > 1 ? 's' : ''}:</span>
                                    <div className={styles.infoValue}>
                                        <div className={styles.peopleList}>
                                            {movie.directors.map((person: any) => (
                                                <span key={person.id} className={styles.personItem}>
                                                    {person.name}
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            )}

                            {movie.screenwriters && movie.screenwriters.length > 0 && (
                                <div className={styles.infoRow}>
                                    <span className={styles.infoLabel}>Screenwriter{movie.screenwriters.length > 1 ? 's' : ''}:</span>
                                    <div className={styles.infoValue}>
                                        <div className={styles.peopleList}>
                                            {movie.screenwriters.map((person: any) => (
                                                <span key={person.id} className={styles.personItem}>
                                                    {person.name}
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            )}

                            {movie.actors && movie.actors.length > 0 && (
                                <div className={styles.infoRow}>
                                    <span className={styles.infoLabel}>Cast:</span>
                                    <div className={styles.infoValue}>
                                        <div className={styles.peopleList}>
                                            {movie.actors.map((person: any) => (
                                                <span key={person.id} className={styles.personItem}>
                                                    {person.name}
                                                </span>
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            )}
                        </div>

                        <div className={styles.descriptionSection}>
                            <h2 className={styles.sectionTitle}>Overview</h2>
                            <p className={styles.description}>{movie.description}</p>
                        </div>
                    </div>
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
            </div>
        </Layout>
    );
};