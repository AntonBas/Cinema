import React from 'react';
import type { MovieDto } from '@/types/Movie';
import { movieApi } from '@/api/movieApi';
import styles from './MovieCard.module.css';

interface MovieCardProps {
  movie: MovieDto;
  onEdit: (movie: MovieDto) => void;
  onDelete: (movie: MovieDto) => void;
}

export const MovieCard: React.FC<MovieCardProps> = ({
  movie,
  onEdit,
  onDelete
}) => {
  const handleImageError = (e: React.SyntheticEvent<HTMLImageElement>) => {
    e.currentTarget.src = '/images/default-poster.jpg';
  };

  return (
    <div className={styles.card}>
      <div className={styles.posterContainer}>
        <img
          src={movie.posterFileName ? `${movieApi.getPosterUrl(movie.id!)}?t=${Date.now()}` : '/images/default-poster.jpg'}
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
        <p className={styles.description}>
          {movie.description && movie.description.length > 100
            ? `${movie.description.substring(0, 100)}...`
            : movie.description
          }
        </p>
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