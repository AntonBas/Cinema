import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { movieApi } from '@/api/movieApi';
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

    const { notifications, showNotification, hideNotification } = useNotification();

    useEffect(() => {
        const fetchMovie = async () => {
            if (!slug) return;

            try {
                setLoading(true);
                const movieData = await movieApi.public.getBySlug(slug);
                setMovie(movieData);
            } catch (err) {
                showNotification('Failed to load movie', 'error');
                console.error('Error fetching movie:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchMovie();
    }, [slug, showNotification]);

    const handleBuyTickets = () => {
        if (movie) {
            navigate(`/book/${movie.id}`);
        }
    };

    const handleFindSession = async () => {
        if (!movie) return;

        setFindingSession(true);
        try {
            const today = new Date().toISOString().split('T')[0];
            navigate(`/sessions?movieId=${movie.id}&date=${today}`);
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

    return (
        <Layout>
            <div className={styles.container}>
                <div className={styles.header}>
                    <Button
                        variant="secondary"
                        onClick={() => navigate('/movies/current')}
                        className={styles.backButton}
                    >
                        ← Back to Movies
                    </Button>
                </div>

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
                            {movie.currentlyShowing ? (
                                <Button
                                    variant="primary"
                                    onClick={handleBuyTickets}
                                    className={styles.actionButton}
                                >
                                    <span className={styles.buttonIcon}>🎟️</span>
                                    Buy Tickets
                                </Button>
                            ) : (
                                <Button
                                    variant="primary"
                                    onClick={handleFindSession}
                                    className={styles.actionButton}
                                    loading={findingSession}
                                >
                                    <span className={styles.buttonIcon}>🔍</span>
                                    {findingSession ? 'Finding...' : 'Find Session'}
                                </Button>
                            )}

                            {movie.trailerUrl && (
                                <Button
                                    variant="secondary"
                                    onClick={() => window.open(movie.trailerUrl, '_blank')}
                                    className={styles.trailerButton}
                                >
                                    <span className={styles.buttonIcon}>🎬</span>
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