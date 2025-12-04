import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { movieApi } from '@/api/movieApi';
import type { MovieDetailResponse } from '@/types/movie';
import { useNotification } from '@/hooks/common/useNotification';
import { Button, Notification } from '@/components/ui';
import { Layout } from '@/components/layout/Layout';
import styles from './MovieDetailPage.module.css';

export const MovieDetailPage: React.FC = () => {
    const { slug } = useParams<{ slug: string }>();
    const navigate = useNavigate();
    const [movie, setMovie] = useState<MovieDetailResponse | null>(null);
    const [loading, setLoading] = useState(true);

    const { notifications, showNotification, hideNotification } = useNotification();

    useEffect(() => {
        const fetchMovie = async () => {
            if (!slug) return;

            try {
                setLoading(true);
                const movieData = await movieApi.getMovieBySlug(slug);
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
        console.log('Buy tickets for movie:', movie?.id);
    };

    if (loading) {
        return (
            <Layout>
                <div className={styles.loading}>Loading movie...</div>
            </Layout>
        );
    }

    if (!movie) {
        return (
            <Layout>
                <div className={styles.error}>
                    <h2>Movie not found</h2>
                    <Button variant="primary" onClick={() => navigate('/movies')}>
                        Back to Movies
                    </Button>
                </div>
            </Layout>
        );
    }

    return (
        <Layout>
            <div className={styles.container}>
                <div className={styles.header}>
                    <Button variant="secondary" onClick={() => navigate('/movies')}>
                        ← Back to Movies
                    </Button>
                </div>

                <div className={styles.content}>
                    <div className={styles.posterSection}>
                        <img
                            src={movieApi.getMoviePosterUrl(movie.id)}
                            alt={movie.title}
                            className={styles.poster}
                            onError={(e) => {
                                (e.target as HTMLImageElement).src = '/placeholder-poster.jpg';
                            }}
                        />
                    </div>

                    <div className={styles.detailsSection}>
                        <h1 className={styles.title}>{movie.title}</h1>

                        <div className={styles.meta}>
                            <span>{movie.durationMinutes} min</span>
                            <span>•</span>
                            <span>{movie.ageRating}</span>
                            <span>•</span>
                            <span>{new Date(movie.releaseDate).getFullYear()}</span>
                        </div>

                        {movie.trailerUrl && (
                            <div className={styles.trailer}>
                                <Button
                                    variant="primary"
                                    onClick={() => window.open(movie.trailerUrl, '_blank')}
                                >
                                    Watch Trailer
                                </Button>
                            </div>
                        )}

                        <div className={styles.description}>
                            <h3>Overview</h3>
                            <p>{movie.description}</p>
                        </div>

                        <div className={styles.actions}>
                            <Button variant="primary" onClick={handleBuyTickets}>
                                Buy Tickets
                            </Button>
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