import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { movieApi } from '@/api/movieApi';
import { sessionApi } from '@/api/sessionApi';
import type { MovieDetailResponse } from '@/types/movie';
import {
    AgeRatingDisplay,
    AgeRatingDescription,
    MovieStatusDisplay
} from '@/types/movie';
import { useNotification } from '@/hooks/common/useNotification';
import { Button, Notification } from '@/components/ui';
import { Layout } from '@/components/layout/Layout';
import styles from './MovieDetailPage.module.css';

export const MovieDetailPage: React.FC = () => {
    const { slug } = useParams<{ slug: string }>();
    const navigate = useNavigate();
    const [movie, setMovie] = useState<MovieDetailResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [findingSession, setFindingSession] = useState(false);
    const [nextSessionDate, setNextSessionDate] = useState<string | null>(null);

    const { notifications, showNotification, hideNotification } = useNotification();

    useEffect(() => {
        const fetchMovie = async () => {
            if (!slug) return;

            try {
                setLoading(true);
                const movieData = await movieApi.public.getBySlug(slug);
                setMovie(movieData);

                if (movieData.id) {
                    await findNextSessionDate(movieData.id);
                }
            } catch (err) {
                showNotification('Failed to load movie', 'error');
                console.error('Error fetching movie:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchMovie();
    }, [slug, showNotification]);

    const findNextSessionDate = async (movieId: number) => {
        try {
            const today = new Date();
            today.setHours(0, 0, 0, 0);

            const response = await sessionApi.public.getSchedule(
                0,
                100,
                'startTime,asc',
                undefined,
                movieId,
                30
            );

            const upcomingSessions = response.content.filter((session: any) => {
                const sessionDate = new Date(session.startTime);
                return sessionDate >= today;
            });

            if (upcomingSessions.length > 0) {
                const nextSession = upcomingSessions[0];
                const sessionDate = new Date(nextSession.startTime).toISOString().split('T')[0];
                setNextSessionDate(sessionDate);
            } else {
                setNextSessionDate(null);
            }
        } catch (error) {
            console.error('Error finding next session:', error);
            setNextSessionDate(null);
        }
    };

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
        } catch (error) {
            showNotification('Error finding sessions', 'error');
            console.error('Error:', error);
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
        if (nextSessionDate) {
            const nextDate = new Date(nextSessionDate);
            const today = new Date();
            today.setHours(0, 0, 0, 0);
            const diffDays = Math.ceil((nextDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));

            if (diffDays === 0) return 'Find Sessions Today';
            if (diffDays === 1) return 'Find Sessions Tomorrow';
            return `Find Sessions on ${formatDate(nextSessionDate)}`;
        }
        return 'Find Sessions';
    };

    return (
        <Layout>
            <div className={styles.container}>
                <div className={styles.content}>
                    <div className={styles.leftColumn}>
                        <div className={styles.posterContainer}>
                            <img
                                src={movieApi.public.getPosterUrl(movie.id)}
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
                                            {movie.genres.map((genre) => (
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
                                            {movie.directors.map((person) => (
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
                                            {movie.screenwriters.map((person) => (
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
                                            {movie.actors.map((person) => (
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

                {notifications.map((notification, index) => (
                    <Notification
                        key={notification.id}
                        {...notification}
                        onClose={hideNotification}
                        position={index}
                    />
                ))}
            </div>
        </Layout>
    );
};