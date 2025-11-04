import React, { useState, useEffect } from 'react';
import type { MovieResponse } from '@/types/movie';
import { movieApi } from '@/api/movieApi';
import { getAgeRatingDisplay } from '@/types/movie';
import styles from './MovieCard.module.css';

interface MovieCardProps {
  movie: MovieResponse;
  onEdit: (movie: MovieResponse) => void;
  onDelete: (movie: MovieResponse) => void;
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
      newPosterUrl = movieApi.getPosterUrlWithTimestamp?.(movie.id) || movieApi.getPosterUrl(movie.id);
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

  const getStatusDisplay = (status: string) => {
    const statusMap: { [key: string]: string } = {
      'CURRENT': 'Now Showing',
      'UPCOMING': 'Coming Soon',
      'ARCHIVED': 'Archived'
    };
    return statusMap[status] || status;
  };

  const getStatusClass = (status: string) => {
    const classMap: { [key: string]: string } = {
      'CURRENT': styles.statusCurrent,
      'UPCOMING': styles.statusUpcoming,
      'ARCHIVED': styles.statusArchived
    };
    return classMap[status] || styles.status;
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
            <span className={styles.duration}>{movie.durationMinutes} min</span>
            <span className={styles.rating}>
              {getAgeRatingDisplay(movie.ageRating)}
            </span>
          </div>

          <div className={styles.metaRow}>
            <span className={getStatusClass(movie.status)}>
              {getStatusDisplay(movie.status)}
            </span>
          </div>

          {movie.releaseDate && (
            <div className={styles.releaseDate}>
              {new Date(movie.releaseDate).toLocaleDateString()}
            </div>
          )}
        </div>
      </div>

      <div className={styles.actions}>
        <button
          className={styles.editButton}
          onClick={() => onEdit(movie)}
          type="button"
          aria-label={`Edit ${movie.title}`}
        >
          Edit
        </button>
        <button
          className={styles.deleteButton}
          onClick={() => onDelete(movie)}
          type="button"
          aria-label={`Delete ${movie.title}`}
        >
          Delete
        </button>
      </div>
    </div>
  );
};