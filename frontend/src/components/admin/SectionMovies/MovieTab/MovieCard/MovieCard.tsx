import React from 'react';
import type { MovieResponse } from '@/types/Movie';
import { movieApi } from '@/api/movieApi';
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
  const handleImageError = (e: React.SyntheticEvent<HTMLImageElement>) => {
    e.currentTarget.src = '/images/default-poster.jpg';
    e.currentTarget.onerror = null;
  };

  const getPosterUrl = () => {
    if (movie.posterUrl) {
      return movie.posterUrl;
    }

    if (movie.id) {
      return movieApi.getPosterUrl(movie.id);
    }

    return '/images/default-poster.jpg';
  };

  return (
    <div className={styles.card}>
      <div className={styles.posterContainer}>
        <img
          src={getPosterUrl()}
          alt={movie.title}
          className={styles.poster}
          onError={handleImageError}
        />
      </div>

      <div className={styles.info}>
        <h3 className={styles.title}>{movie.title}</h3>
        <div className={styles.meta}>
          <span className={styles.duration}>{movie.durationMinutes}min</span>
          <span className={styles.rating}>{movie.ageRating}</span>
          <span className={styles.status}>{movie.status}</span>
        </div>
      </div>

      <div className={styles.actions}>
        <button
          className={styles.editButton}
          onClick={() => onEdit(movie)}
        >
          Edit
        </button>
        <button
          className={styles.deleteButton}
          onClick={() => onDelete(movie)}
        >
          Delete
        </button>
      </div>
    </div>
  );
};