import React, { useState, useEffect } from 'react';
import type { MovieResponse, MovieDto } from '@/types/Movie';
import { MovieStatus } from '@/types/Movie';
import { movieApi } from '@/api/movieApi';
import { MovieList } from './MovieList';
import { MovieForm } from './MovieForm';
import { Notification } from '@/components/ui/Notification/Notification';
import { useNotification } from '@/hooks/useNotification';
import styles from './MovieTab.module.css';

type MovieTabType = 'CURRENT' | 'UPCOMING' | 'ARCHIVED';

export const MovieTab: React.FC = () => {
  const [movies, setMovies] = useState<MovieResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingMovie, setEditingMovie] = useState<MovieDto | null>(null);
  const [deletingMovie, setDeletingMovie] = useState<MovieDto | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [activeTab, setActiveTab] = useState<MovieTabType>('CURRENT');

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

  const handleEdit = async (movie: MovieResponse) => {
    try {
      const fullMovie = await movieApi.getById(movie.id);
      setEditingMovie(fullMovie);
      setIsModalOpen(true);
    } catch (error) {
      console.error('Error loading movie details:', error);
      showNotification('Failed to load movie details', 'error');
    }
  };

  const handleDeleteClick = (movie: MovieResponse) => {
    setDeletingMovie(movie as MovieDto);
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

  const filteredMovies = movies.filter(movie => movie.status === activeTab);

  const getTabStats = () => {
    return {
      CURRENT: movies.filter(m => m.status === MovieStatus.CURRENT).length,
      UPCOMING: movies.filter(m => m.status === MovieStatus.UPCOMING).length,
      ARCHIVED: movies.filter(m => m.status === MovieStatus.ARCHIVED).length
    };
  };

  const tabStats = getTabStats();

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

      <div className={styles.tabs}>
        <button
          className={`${styles.tab} ${activeTab === 'CURRENT' ? styles.active : ''}`}
          onClick={() => setActiveTab('CURRENT')}
        >
          <span className={styles.tabLabel}>Currently Showing</span>
          <span className={styles.tabCount}>{tabStats.CURRENT}</span>
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'UPCOMING' ? styles.active : ''}`}
          onClick={() => setActiveTab('UPCOMING')}
        >
          <span className={styles.tabLabel}>Upcoming</span>
          <span className={styles.tabCount}>{tabStats.UPCOMING}</span>
        </button>
        <button
          className={`${styles.tab} ${activeTab === 'ARCHIVED' ? styles.active : ''}`}
          onClick={() => setActiveTab('ARCHIVED')}
        >
          <span className={styles.tabLabel}>Archived</span>
          <span className={styles.tabCount}>{tabStats.ARCHIVED}</span>
        </button>
      </div>

      <div className={styles.tabContent}>
        <MovieList
          movies={filteredMovies}
          onEdit={handleEdit}
          onDelete={handleDeleteClick}
        />
      </div>

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