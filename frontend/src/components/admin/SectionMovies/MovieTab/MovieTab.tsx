import React, { useState, useEffect } from 'react';
import type { MovieDto } from '@/types/Movie';
import { movieApi } from '@/api/movieApi';
import { MovieList } from './MovieList';
import { MovieForm } from './MovieForm';
import { Notification } from '@/components/ui/Notification/Notification';
import { useNotification } from '@/hooks/useNotification';
import styles from './MovieTab.module.css';

export const MovieTab: React.FC = () => {
  const [movies, setMovies] = useState<MovieDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<MovieDto | null>(null);
  const [deletingMovie, setDeletingMovie] = useState<MovieDto | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const { notifications, showNotification, hideNotification } = useNotification();

  useEffect(() => {
    loadMovies();
  }, []);

  const loadMovies = async () => {
    try {
      setIsLoading(true);
      const data = await movieApi.getAll();
      setMovies(data);
    } catch (error) {
      console.error('Error loading movies:', error);
      showNotification('Failed to load movies', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleEdit = (movie: MovieDto) => {
    setEditingMovie(movie);
    setIsModalOpen(true);
  };

  const handleDeleteClick = (movie: MovieDto) => {
    setDeletingMovie(movie);
    setIsDeleteModalOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!deletingMovie?.id) return;

    try {
      setIsDeleting(true);
      await movieApi.delete(deletingMovie.id);
      showNotification('Movie deleted successfully!', 'success');
      loadMovies();
    } catch (error) {
      console.error('Error deleting movie:', error);
      showNotification('Error deleting movie', 'error');
    } finally {
      setIsDeleting(false);
      setIsDeleteModalOpen(false);
      setDeletingMovie(null);
    }
  };

  const handleDeleteCancel = () => {
    setIsDeleteModalOpen(false);
    setDeletingMovie(null);
  };

  const handleFormSuccess = () => {
    resetForm();
    loadMovies();
  };

  const resetForm = () => {
    setIsModalOpen(false);
    setEditingMovie(null);
  };

  if (isLoading) return (
    <div className={styles.loading}>
      <div className={styles.loadingSpinner}></div>
      <p>Loading movies...</p>
    </div>
  );

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2 className={styles.title}>Movie Management</h2>
        <button
          className={styles.addButton}
          onClick={() => setIsModalOpen(true)}
        >
          Add New Movie
        </button>
      </div>

      <MovieList
        movies={movies}
        onEdit={handleEdit}
        onDelete={handleDeleteClick}
      />

      {isModalOpen && (
        <MovieForm
          movie={editingMovie}
          onSuccess={handleFormSuccess}
          onCancel={resetForm}
          showNotification={showNotification}
        />
      )}

      {isDeleteModalOpen && (
        <div className={styles.modalOverlay}>
          <div className={styles.confirmModal}>
            <div className={styles.confirmIcon}>🎬</div>
            <h3 className={styles.confirmTitle}>Delete Movie</h3>
            <p className={styles.confirmMessage}>
              Are you sure you want to delete the movie
            </p>
            <p className={styles.confirmWarning}>
              "{deletingMovie?.title}"?
            </p>
            <p className={styles.confirmMessage}>
              This action cannot be undone.
            </p>
            <div className={styles.confirmActions}>
              <button
                className={styles.cancelConfirmButton}
                onClick={handleDeleteCancel}
                disabled={isDeleting}
              >
                Cancel
              </button>
              <button
                className={styles.deleteConfirmButton}
                onClick={handleDeleteConfirm}
                disabled={isDeleting}
              >
                {isDeleting ? 'Deleting...' : 'Delete'}
              </button>
            </div>
          </div>
        </div>
      )}

      {notifications.map((notification, index) => (
        <Notification
          key={notification.id}
          id={notification.id}
          message={notification.message}
          type={notification.type}
          isVisible={notification.isVisible}
          onClose={hideNotification}
          duration={4000}
          position={index}
        />
      ))}
    </div>
  );
};