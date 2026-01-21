import React from 'react';
import { Link } from 'react-router-dom';
import type { MovieCardResponse } from '@/types/movie';
import { Button } from '@/components/ui';
import styles from './MovieCard.module.css';

interface MovieCardProps {
    movie: MovieCardResponse;
}

export const MovieCard: React.FC<MovieCardProps> = ({ movie }) => {
    const handleButtonClick = (e: React.MouseEvent) => {
        e.preventDefault();
        e.stopPropagation();
        console.log('View details for:', movie.id);
    };

    const getAgeRatingDisplay = () => {
        const ratingMap: Record<string, string> = {
            'PEGI_3': '3+',
            'PEGI_7': '7+',
            'PEGI_12': '12+',
            'PEGI_16': '16+',
            'PEGI_18': '18+'
        };
        return ratingMap[movie.ageRating] || movie.ageRating;
    };

    return (
        <div className={styles.cardWrapper}>
            <Link to={`/movies/${movie.slug}`} className={styles.card}>
                <div className={styles.posterContainer}>
                    <img
                        src={movie.posterUrl}
                        alt={movie.title}
                        className={styles.poster}
                        onError={(e) => {
                            (e.target as HTMLImageElement).src = '/placeholder-poster.jpg';
                        }}
                    />
                    <div className={styles.cornerInfo}>
                        <div className={styles.durationBadge}>
                            {movie.durationMinutes}m
                        </div>
                        <div className={styles.ageBadge}>
                            {getAgeRatingDisplay()}
                        </div>
                    </div>
                    <div className={styles.overlay}>
                        <div className={styles.buttonContainer}>
                            <Button
                                variant="primary"
                                size="medium"
                                onClick={handleButtonClick}
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
};