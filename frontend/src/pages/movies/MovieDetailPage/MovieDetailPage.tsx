import React, { useState, useEffect, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { movieApi } from '@/api/movieApi';
import type { MovieDetailResponse } from '@/types/movie';
import type { SessionMovieInfoResponse } from '@/types/session';
import {
    AgeRatingDisplay,
    AgeRatingDescription,
    MovieStatusDisplay
} from '@/types/movie';
import { useNotification } from '@/hooks/common/useNotification';
import { Button } from '@/components/ui/Button/Button';
import { Notification } from '@/components/ui/Notification/Notification';
import { Tooltip } from '@/components/ui/Tooltip/Tooltip';
import { Layout } from '@/components/layout/Layout/Layout';
import { SessionSection } from '@/components/movies/SessionSection/SessionSection';
import styles from './MovieDetailPage.module.css';

const AGE_RATING_COLORS: Record<string, string> = {
    'PEGI_3': styles.ageRatingGreen,
    'PEGI_7': styles.ageRatingBlue,
    'PEGI_12': styles.ageRatingYellow,
    'PEGI_16': styles.ageRatingOrange,
    'PEGI_18': styles.ageRatingRed
};

export const MovieDetailPage: React.FC = () => {
    const { slug } = useParams<{ slug: string }>();
    const navigate = useNavigate();
    const [movie, setMovie] = useState<MovieDetailResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [selectedDate, setSelectedDate] = useState<string | null>(null);
    const [dateList, setDateList] = useState<string[]>([]);
    const [sessionsByDate, setSessionsByDate] = useState<Record<string, SessionMovieInfoResponse[]>>({});
    const [dateScrollIndex, setDateScrollIndex] = useState(0);
    const datesPerView = 5;
    const isMounted = useRef(true);

    const { notifications, showNotification, hideNotification } = useNotification();
    const showNotificationRef = useRef(showNotification);

    useEffect(() => {
        showNotificationRef.current = showNotification;
    }, [showNotification]);

    useEffect(() => {
        isMounted.current = true;

        const fetchMovie = async () => {
            if (!slug) return;

            try {
                setLoading(true);
                const response = await movieApi.public.getBySlug(slug);
                const movieData = response?.data || null;

                if (isMounted.current && movieData) {
                    setMovie(movieData);

                    if (movieData.sessions && movieData.sessions.length > 0) {
                        const grouped: Record<string, SessionMovieInfoResponse[]> = {};
                        movieData.sessions.forEach(session => {
                            const date = session.startTime.split('T')[0];
                            if (!grouped[date]) {
                                grouped[date] = [];
                            }
                            grouped[date].push(session);
                        });

                        const sortedDates = Object.keys(grouped).sort();
                        setDateList(sortedDates);
                        setSessionsByDate(grouped);
                        setSelectedDate(sortedDates[0]);
                    }
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
    }, [slug]);

    const formatDate = (dateString: string): string => {
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
        });
    };

    const scrollDates = (direction: 'left' | 'right') => {
        const maxIndex = Math.max(0, dateList.length - datesPerView);
        if (direction === 'left') {
            setDateScrollIndex(Math.max(0, dateScrollIndex - 1));
        } else {
            setDateScrollIndex(Math.min(maxIndex, dateScrollIndex + 1));
        }
    };

    const getAgeRatingClass = useCallback((ageRating: string) => {
        return AGE_RATING_COLORS[ageRating] || styles.ageRatingRed;
    }, []);

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
                    <Button variant="primary" onClick={() => navigate('/movies/currently-showing')}>
                        Browse Current Movies
                    </Button>
                </div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className={styles.container}>
                <div className={styles.content}>
                    <div className={styles.leftColumn}>
                        <div className={styles.posterContainer}>
                            <img
                                src={movie.posterUrl}
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

                        {movie.trailerUrl && (
                            <div className={styles.actionButtons}>
                                <Button
                                    variant="secondary"
                                    onClick={() => window.open(movie.trailerUrl, '_blank')}
                                    className={styles.trailerButton}
                                >
                                    Watch Trailer
                                </Button>
                            </div>
                        )}
                    </div>

                    <div className={styles.rightColumn}>
                        <div className={styles.titleSection}>
                            <div className={styles.titleContent}>
                                <h1 className={styles.title}>{movie.title}</h1>
                                <div className={styles.metaInfo}>
                                    <Tooltip
                                        content={AgeRatingDescription[movie.ageRating] || 'Age rating'}
                                        position="top"
                                    >
                                        <div className={`${styles.ageRating} ${getAgeRatingClass(movie.ageRating)}`}>
                                            {AgeRatingDisplay[movie.ageRating]}
                                        </div>
                                    </Tooltip>
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
                                    <div className={styles.dateValue}>{formatDate(movie.releaseDate)}</div>
                                </div>
                            </div>

                            <div className={styles.infoRow}>
                                <span className={styles.infoLabel}>End Showing:</span>
                                <div className={styles.infoValue}>
                                    <div className={styles.dateValue}>{formatDate(movie.endShowingDate)}</div>
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

                        <SessionSection
                            dateList={dateList}
                            sessionsByDate={sessionsByDate}
                            selectedDate={selectedDate}
                            onDateSelect={setSelectedDate}
                            dateScrollIndex={dateScrollIndex}
                            datesPerView={datesPerView}
                            onScrollDates={scrollDates}
                        />
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