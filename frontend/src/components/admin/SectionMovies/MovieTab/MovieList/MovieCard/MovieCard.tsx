import React, { useState, useCallback } from 'react';
import type { MovieCardResponse, MovieStatus } from '@/types/movie';
import { AgeRatingDisplay, MovieStatusDisplay } from '@/types/movie';
import { Button } from '@/components/ui/Button/Button';
import { Badge } from '@/components/ui/Badge/Badge';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './MovieCard.module.css';

interface MovieCardProps {
  movie: MovieCardResponse;
  onEdit: (movie: MovieCardResponse) => void;
  onDelete: (movie: MovieCardResponse) => void;
}

const STATUS_VARIANTS: Record<MovieStatus, 'success' | 'warning' | 'secondary'> = {
  CURRENT: 'success',
  UPCOMING: 'warning',
  ARCHIVED: 'secondary',
  UNKNOWN: 'secondary',
};

export const MovieCard: React.FC<MovieCardProps> = React.memo(({ movie, onEdit, onDelete }) => {
  const [imageStatus, setImageStatus] = useState<'loading' | 'loaded' | 'error'>('loading');

  const handleEdit = useCallback(() => onEdit(movie), [onEdit, movie]);
  const handleDelete = useCallback(() => onDelete(movie), [onDelete, movie]);

  const statusVariant = STATUS_VARIANTS[movie.status] || 'secondary';
  const statusDisplay = MovieStatusDisplay[movie.status] || movie.status;
  const ageRatingDisplay = AgeRatingDisplay[movie.ageRating] || movie.ageRating;

  const renderPoster = () => {
    if (!movie.posterUrl) {
      return (
        <div className={styles.posterError}>
          <span>No Image</span>
        </div>
      );
    }

    return (
      <>
        {imageStatus === 'loading' && (
          <div className={styles.posterPlaceholder}>
            <LoadingSpinner text="" />
          </div>
        )}
        <img
          src={movie.posterUrl}
          alt={`Poster for ${movie.title}`}
          className={`${styles.poster} ${imageStatus !== 'loaded' ? styles.hidden : ''}`}
          onLoad={() => setImageStatus('loaded')}
          onError={() => setImageStatus('error')}
          loading="lazy"
        />
        {imageStatus === 'error' && (
          <div className={styles.posterError}>
            <span>No Image</span>
          </div>
        )}
      </>
    );
  };

  return (
    <div className={styles.card}>
      <div className={styles.posterContainer}>{renderPoster()}</div>

      <div className={styles.info}>
        <h3 className={styles.title} title={movie.title}>
          {movie.title}
        </h3>
        <div className={styles.meta}>
          <div className={styles.metaRow}>
            <Badge variant="success">{movie.durationMinutes} min</Badge>
            <Badge variant="warning">{ageRatingDisplay}</Badge>
          </div>
          <div className={styles.metaRow}>
            <Badge variant={statusVariant}>{statusDisplay}</Badge>
          </div>
        </div>
      </div>

      <div className={styles.actions}>
        <Button variant="success" size="small" onClick={handleEdit}>
          Edit
        </Button>
        <Button variant="error" size="small" onClick={handleDelete}>
          Delete
        </Button>
      </div>
    </div>
  );
});

MovieCard.displayName = 'MovieCard';