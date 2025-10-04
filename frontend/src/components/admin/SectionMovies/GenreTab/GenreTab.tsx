import React, { useState, useEffect } from 'react';
import type { GenreDto, GenreFormData } from '@/types/Genre';
import { genreApi } from '@/api/genreApi';
import { useNotification } from '@/hooks/useNotification';
import { Notification } from '@/components/ui/Notification';
import styles from './GenreTab.module.css';

export const GenreTab: React.FC = () => {
  const [genres, setGenres] = useState<GenreDto[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingGenre, setEditingGenre] = useState<GenreDto | null>(null);
  const [deletingGenre, setDeletingGenre] = useState<GenreDto | null>(null);
  const [formData, setFormData] = useState<GenreFormData>({ name: '' });
  const [isDeleting, setIsDeleting] = useState(false);

  const { notifications, showNotification, hideNotification } = useNotification();

  useEffect(() => {
    loadGenres();
  }, []);

  const loadGenres = async () => {
    try {
      setIsLoading(true);
      const data = await genreApi.getAll();
      setGenres(data);
    } catch (error) {
      console.error('Error loading genres:', error);
      showNotification('Failed to load genres', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingGenre?.id) {
        await genreApi.update(editingGenre.id, formData);
        showNotification('Genre updated successfully!', 'success');
      } else {
        await genreApi.create(formData);
        showNotification('Genre created successfully!', 'success');
      }
      resetForm();
      loadGenres();
    } catch (error) {
      console.error('Error saving genre:', error);
      showNotification('Error saving genre. Please try again.', 'error');
    }
  };

  const handleDeleteClick = (genre: GenreDto) => {
    setDeletingGenre(genre);
    setIsDeleteModalOpen(true);
  };

  const handleDeleteConfirm = async () => {
    if (!deletingGenre?.id) return;

    try {
      setIsDeleting(true);
      await genreApi.delete(deletingGenre.id);
      showNotification('Genre deleted successfully!', 'success');
      loadGenres();
    } catch (error) {
      console.error('Error deleting genre:', error);
      showNotification('Cannot delete genre. It might be used in movies.', 'error');
    } finally {
      setIsDeleting(false);
      setIsDeleteModalOpen(false);
      setDeletingGenre(null);
    }
  };

  const handleDeleteCancel = () => {
    setIsDeleteModalOpen(false);
    setDeletingGenre(null);
  };

  const resetForm = () => {
    setIsModalOpen(false);
    setEditingGenre(null);
    setFormData({ name: '' });
  };

  const handleEdit = (genre: GenreDto) => {
    setEditingGenre(genre);
    setFormData({ name: genre.name });
    setIsModalOpen(true);
  };

  if (isLoading) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
        <p>Loading genres...</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h2>Genre Management</h2>
        <button
          className={styles.primaryButton}
          onClick={() => setIsModalOpen(true)}
        >
          Add New Genre
        </button>
      </div>

      <div className={styles.grid}>
        {genres.length === 0 ? (
          <div className={styles.empty}>
            <div className={styles.emptyIcon}>📚</div>
            <h3>No genres found</h3>
            <p>Create your first genre to get started!</p>
            <button
              className={styles.primaryButton}
              onClick={() => setIsModalOpen(true)}
            >
              Create First Genre
            </button>
          </div>
        ) : (
          genres.map(genre => (
            <div key={genre.id} className={styles.card}>
              <div className={styles.info}>
                <h3 className={styles.name}>{genre.name}</h3>
                {genre.movies && (
                  <span className={styles.count}>
                    {genre.movies.length} movie{genre.movies.length !== 1 ? 's' : ''}
                  </span>
                )}
              </div>
              <div className={styles.actions}>
                <button
                  className={styles.editButton}
                  onClick={() => handleEdit(genre)}
                >
                  Edit
                </button>
                <button
                  className={styles.deleteButton}
                  onClick={() => handleDeleteClick(genre)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {isModalOpen && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h3>{editingGenre ? 'Edit Genre' : 'Add New Genre'}</h3>
            <form onSubmit={handleSubmit} className={styles.form}>
              <div className={styles.formGroup}>
                <input
                  type="text"
                  placeholder="Genre name (e.g., Action, Drama, Comedy)"
                  value={formData.name}
                  onChange={(e) => setFormData({ name: e.target.value })}
                  required
                  maxLength={50}
                />
                <small>Maximum 50 characters</small>
              </div>

              <div className={styles.formActions}>
                <button type="submit" className={styles.primaryButton}>
                  {editingGenre ? 'Update' : 'Create'} Genre
                </button>
                <button
                  type="button"
                  className={styles.cancelButton}
                  onClick={resetForm}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {isDeleteModalOpen && (
        <div className={styles.modalOverlay}>
          <div className={styles.confirmModal}>
            <div className={styles.confirmIcon}>🗑️</div>
            <h3 className={styles.confirmTitle}>Delete Genre</h3>
            <p className={styles.confirmMessage}>
              Are you sure you want to delete the genre
            </p>
            <p className={styles.confirmWarning}>
              "{deletingGenre?.name}"?
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