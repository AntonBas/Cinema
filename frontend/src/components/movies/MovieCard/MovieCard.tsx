import React from 'react';
import { Link } from 'react-router-dom';
import type { MovieCardResponse } from '@/types/movie';
import { movieApi } from '@/api/movieApi';
import { Badge, Button } from '@/components/ui';
import styles from './MovieCard.module.css';

interface MovieCardProps {
    movie: MovieCardResponse;
}

export const MovieCard: React.FC<MovieCardProps> = ({ movie }) => {
    const handleBuyTickets = (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        console.log('Buy tickets for:', movie.id);
    };

    const getStatusBadge = () => {
        if (movie.currentlyShowing) {
            return <Badge variant="success" size="small">Now Playing</Badge>;
        }
        if (movie.status === 'UPCOMING') {
            return <Badge variant="info" size="small">Coming Soon</Badge>;
        }
        return null;
    };

    return (
        <Link to={`/movies/${movie.slug}`} className={styles.card}>
            <div className={styles.posterContainer}>
                <img
                    src={movieApi.public.getPosterUrl(movie.id)}
                    alt={movie.title}
                    className={styles.poster}
                    onError={(e) => {
                        (e.target as HTMLImageElement).src = '/placeholder-poster.jpg';
                    }}
                />
                <div className={styles.overlay}>
                    <div className={styles.badges}>
                        {getStatusBadge()}
                    </div>
                    <div className={styles.buyButtonContainer}>
                        <Button
                            variant="primary"
                            size="small"
                            onClick={handleBuyTickets}
                            disabled={!movie.currentlyShowing}
                        >
                            {movie.currentlyShowing ? 'Buy Tickets' : 'Coming Soon'}
                        </Button>
                    </div>
                </div>
            </div>

            <div className={styles.content}>
                <h3 className={styles.title}>{movie.title}</h3>
                <div className={styles.meta}>
                    <span>{movie.durationMinutes} min</span>
                    <span>•</span>
                    <span>{movie.ageRating}</span>
                </div>
                <div className={styles.year}>
                    {new Date(movie.releaseDate).getFullYear()}
                </div>
            </div>
        </Link>
    );
};