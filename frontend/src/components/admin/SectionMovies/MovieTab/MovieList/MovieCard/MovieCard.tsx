import React, { useState, useEffect, useCallback, useMemo } from 'react';
import type { MovieCardResponse, MovieStatus } from '@/types/movie';
import { movieApi } from '@/api/movieApi';
import { getAgeRatingDisplay } from '@/types/movie';
import { Button, Badge, LoadingSpinner } from '@/components/ui';
import styles from './MovieCard.module.css';

interface MovieCardProps {
  movie: MovieCardResponse;
  onEdit: (movie: MovieCardResponse) => void;
  onDelete: (movie: MovieCardResponse) => void;
}

const DEFAULT_POSTER = '/images/default-poster.jpg';

const STATUS_VARIANTS: Record<MovieStatus, 'success' | 'warning' | 'secondary'> = {
  CURRENT: 'success',
  UPCOMING: 'warning',
  ARCHIVED: 'secondary',
  UNKNOWN: 'secondary'
};

const STATUS_DISPLAY: Record<MovieStatus, string> = {
  CURRENT: 'Now Showing',
  UPCOMING: 'Coming Soon',
  ARCHIVED: 'Archived',
  UNKNOWN: 'Unknown'
};

export const MovieCard: React.FC<MovieCardProps> = React.memo(({
  movie,
  onEdit,
  onDelete
}) => {
  const [posterUrl, setPosterUrl] = useState<string>(DEFAULT_POSTER);
  const [imageLoading, setImageLoading] = useState(true);
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    let newPosterUrl = DEFAULT_POSTER;

    if (movie.posterUrl) {
      newPosterUrl = movie.posterUrl;
    } else if (movie.id) {
      newPosterUrl = movieApi.public.getPosterUrl(movie.id);
    }

    setPosterUrl(newPosterUrl);
    setImageLoading(true);
    setImageError(false);
  }, [movie.id, movie.posterUrl]);

  const handleImageLoad = useCallback(() => {
    setImageLoading(false);
  }, []);

  const handleImageError = useCallback(() => {
    setImageLoading(false);
    setImageError(true);
    setPosterUrl(DEFAULT_POSTER);
  }, []);

  const handleEdit = useCallback(() => {
    onEdit(movie);
  }, [onEdit, movie]);

  const handleDelete = useCallback(() => {
    onDelete(movie);
  }, [onDelete, movie]);

  const statusVariant = useMemo(() =>
    STATUS_VARIANTS[movie.status] || 'secondary',
    [movie.status]
  );

  const statusDisplay = useMemo(() =>
    STATUS_DISPLAY[movie.status] || movie.status,
    [movie.status]
  );

  const ageRatingDisplay = useMemo(() =>
    getAgeRatingDisplay(movie.ageRating),
    [movie.ageRating]
  );

  return (
    <div className={styles.card}>
      <div className={styles.posterContainer}>
        {imageLoading && (
          <div className={styles.posterPlaceholder}>
            <LoadingSpinner text="" />
          </div>
        )}
        <img
          src={posterUrl}
          alt={`Poster for ${movie.title}`}
          className={`${styles.poster} ${imageLoading ? styles.hidden : ''}`}
          onLoad={handleImageLoad}
          onError={handleImageError}
          loading="lazy"
        />
        {imageError && !imageLoading && (
          <div className={styles.posterError} role="alert">
            <span>No Image</span>
          </div>
        )}
      </div>

      <div className={styles.info}>
        <h3 className={styles.title} title={movie.title}>
          {movie.title}
        </h3>

        <div className={styles.meta}>
          <div className={styles.metaRow}>
            <Badge variant="success" aria-label={`Duration: ${movie.durationMinutes} minutes`}>
              {movie.durationMinutes} min
            </Badge>
            <Badge variant="warning" aria-label={`Age rating: ${ageRatingDisplay}`}>
              {ageRatingDisplay}
            </Badge>
          </div>

          <div className={styles.metaRow}>
            <Badge
              variant={statusVariant}
              aria-label={`Status: ${statusDisplay}`}
            >
              {statusDisplay}
            </Badge>
          </div>
        </div>
      </div>

      <div className={styles.actions}>
        <Button
          variant="success"
          size="small"
          onClick={handleEdit}
          className={styles.editButton}
          aria-label={`Edit ${movie.title}`}
        >
          Edit
        </Button>
        <Button
          variant="error"
          size="small"
          onClick={handleDelete}
          className={styles.deleteButton}
          aria-label={`Delete ${movie.title}`}
        >
          Delete
        </Button>
      </div>
    </div>
  );
});

MovieCard.displayName = 'MovieCard';