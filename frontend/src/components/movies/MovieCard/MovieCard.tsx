import React, { useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import type { MovieCardResponse } from '@/types/movie';
import { AgeRatingDisplay } from '@/types/movie';
import { Button } from '@/components/ui/Button/Button';
import styles from './MovieCard.module.css';

const AGE_RATING_COLORS: Record<string, string> = {
    PEGI_3: styles.ageBadgeGreen,
    PEGI_7: styles.ageBadgeBlue,
    PEGI_12: styles.ageBadgeYellow,
    PEGI_16: styles.ageBadgeOrange,
    PEGI_18: styles.ageBadgeRed,
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

    const ageRatingDisplay = AgeRatingDisplay[movie.ageRating] || movie.ageRating;
    const ageBadgeClass = AGE_RATING_COLORS[movie.ageRating] || styles.ageBadgeRed;

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
                        <div className={styles.durationBadge}>{movie.durationMinutes}m</div>
                        <div className={`${styles.ageBadge} ${ageBadgeClass}`}>{ageRatingDisplay}</div>
                    </div>
                    <div className={styles.overlay}>
                        <div className={styles.buttonContainer}>
                            <Button variant="primary" size="medium" onClick={handleDetailsClick}>
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