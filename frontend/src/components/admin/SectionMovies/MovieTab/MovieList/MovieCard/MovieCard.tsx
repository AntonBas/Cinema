import React, { useState, useEffect } from 'react';
import type { MovieCardResponse } from '@/types/movie';
import { movieApi } from '@/api/movieApi';
import { getAgeRatingDisplay } from '@/types/movie';
import { Button, Badge } from '@/components/ui';
import styles from './MovieCard.module.css';

interface MovieCardProps {
  movie: MovieCardResponse;
  onEdit: (movie: MovieCardResponse) => void;
  onDelete: (movie: MovieCardResponse) => void;
}

export const MovieCard: React.FC<MovieCardProps> = ({
  movie,
  onEdit,
  onDelete
}) => {
  const [posterUrl, setPosterUrl] = useState<string>('/images/default-poster.jpg');
  const [imageLoading, setImageLoading] = useState(true);
  const [imageError, setImageError] = useState(false);

  useEffect(() => {
    let newPosterUrl = '/images/default-poster.jpg';

    if (movie.posterUrl) {
      newPosterUrl = movie.posterUrl;
    } else if (movie.id) {
      newPosterUrl = movieApi.public.getPosterUrl(movie.id);
    }

    setPosterUrl(newPosterUrl);
    setImageLoading(true);
    setImageError(false);
  }, [movie.id, movie.posterUrl]);

  const handleImageLoad = () => {
    setImageLoading(false);
  };

  const handleImageError = () => {
    setImageLoading(false);
    setImageError(true);
    setPosterUrl('/images/default-poster.jpg');
  };

  const getStatusVariant = (status: string) => {
    const variantMap: { [key: string]: 'success' | 'warning' | 'secondary' } = {
      'CURRENT': 'success',
      'UPCOMING': 'warning',
      'ARCHIVED': 'secondary'
    };
    return variantMap[status] || 'secondary';
  };

  const getStatusDisplay = (status: string) => {
    const statusMap: { [key: string]: string } = {
      'CURRENT': 'Now Showing',
      'UPCOMING': 'Coming Soon',
      'ARCHIVED': 'Archived'
    };
    return statusMap[status] || status;
  };

  return (
    <div className={styles.card}>
      <div className={styles.posterContainer}>
        {imageLoading && (
          <div className={styles.posterPlaceholder}>
            <div className={styles.loadingSpinner}></div>
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
          <div className={styles.posterError}>
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
            <Badge variant="success">
              {movie.durationMinutes} min
            </Badge>
            <Badge variant="warning">
              {getAgeRatingDisplay(movie.ageRating)}
            </Badge>
          </div>

          <div className={styles.metaRow}>
            <Badge variant={getStatusVariant(movie.status)}>
              {getStatusDisplay(movie.status)}
            </Badge>
          </div>

          {movie.releaseDate && (
            <div className={styles.releaseDate}>
              {new Date(movie.releaseDate).toLocaleDateString()}
            </div>
          )}
        </div>
      </div>

      <div className={styles.actions}>
        <Button
          variant="success"
          size="small"
          onClick={() => onEdit(movie)}
          className={styles.editButton}
        >
          Edit
        </Button>
        <Button
          variant="error"
          size="small"
          onClick={() => onDelete(movie)}
          className={styles.deleteButton}
        >
          Delete
        </Button>
      </div>
    </div>
  );
};