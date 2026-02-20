import React, { useCallback, useMemo } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import type { MovieCardResponse } from '@/types/movie';
import { Button } from '@/components/ui';
import styles from './MovieCard.module.css';

const AGE_RATING_MAP: Record<string, string> = {
    'PEGI_3': '3+',
    'PEGI_7': '7+',
    'PEGI_12': '12+',
    'PEGI_16': '16+',
    'PEGI_18': '18+'
};

const PLACEHOLDER_IMAGE = '/placeholder-poster.jpg';

interface MovieCardProps {
    movie: MovieCardResponse;
}

export const MovieCard: React.FC<MovieCardProps> = React.memo(({ movie }) => {
    const navigate = useNavigate();

    const handleImageError = useCallback((e: React.SyntheticEvent<HTMLImageElement>) => {
        e.currentTarget.src = PLACEHOLDER_IMAGE;
    }, []);

    const handleDetailsClick = useCallback((e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        navigate(`/movies/${movie.slug}`);
    }, [navigate, movie.slug]);

    const ageRatingDisplay = useMemo(() =>
        AGE_RATING_MAP[movie.ageRating] || movie.ageRating,
        [movie.ageRating]
    );

    return (
        <div className={styles.cardWrapper}>
            <Link to={`/movies/${movie.slug}`} className={styles.card}>
                <div className={styles.posterContainer}>
                    <img
                        src={movie.posterUrl}
                        alt={movie.title}
                        className={styles.poster}
                        loading="lazy"
                        onError={handleImageError}
                    />
                    <div className={styles.cornerInfo}>
                        <div className={styles.durationBadge}>
                            {movie.durationMinutes}m
                        </div>
                        <div className={styles.ageBadge}>
                            {ageRatingDisplay}
                        </div>
                    </div>
                    <div className={styles.overlay}>
                        <div className={styles.buttonContainer}>
                            <Button
                                variant="primary"
                                size="medium"
                                onClick={handleDetailsClick}
                                aria-label={`View details for ${movie.title}`}
                            >
                                View Details
                            </Button>
                        </div>
                    </div>
                </div>

                <div className={styles.content}>
                    <h3 className={styles.title}>{movie.title}</h3>
                </div>
            </Link>
        </div>
    );
});

MovieCard.displayName = 'MovieCard';