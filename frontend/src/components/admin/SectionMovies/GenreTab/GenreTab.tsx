import React, { useState, useEffect } from 'react';
import type { GenreDto, GenreRequest } from '@/types/genre';
import { useGenreSearch, useGenreMutation } from '@/hooks/features/genres';
import { useNotification } from '@/hooks/common/useNotification';
import { Notification } from '@/components/ui/Notification';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import styles from './GenreTab.module.css';

export const GenreTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingGenre, setEditingGenre] = useState<GenreDto | null>(null);
  const [deletingGenre, setDeletingGenre] = useState<GenreDto | null>(null);
  const [formData, setFormData] = useState<GenreRequest>({ name: '' });
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [debouncedSearch, setDebouncedSearch] = useState('');

  const {
    genres,
    pagination,
    loading,
    error: searchError,
    searchGenres
  } = useGenreSearch();

  const {
    createGenre,
    updateGenre,
    deleteGenre,
    loading: mutationLoading,
    error: mutationError
  } = useGenreMutation();

  const { notifications, showNotification, hideNotification } = useNotification();

  useEffect(() => {
    const timer = setTimeout(() => {
      setDebouncedSearch(searchQuery);
    }, 300);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  useEffect(() => {
    searchGenres({ query: debouncedSearch, page: currentPage, size: 10 });
  }, [debouncedSearch, currentPage, searchGenres]);

  useEffect(() => {
    if (searchError) {
      showNotification(searchError, 'error');
    }
  }, [searchError, showNotification]);

  useEffect(() => {
    if (mutationError) {
      showNotification(mutationError, 'error');
    }
  }, [mutationError, showNotification]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingGenre?.id) {
        await updateGenre(editingGenre.id, formData);
        showNotification('Genre updated successfully!', 'success');
      } else {
        await createGenre(formData);
        showNotification('Genre created successfully!', 'success');
      }
      resetForm();
      searchGenres({ query: debouncedSearch, page: currentPage, size: 10 });
    } catch (err) { }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingGenre?.id) return;
    try {
      await deleteGenre(deletingGenre.id);
      showNotification('Genre deleted successfully!', 'success');
      if (genres.length === 1 && currentPage > 0) {
        setCurrentPage(currentPage - 1);
      } else {
        searchGenres({ query: debouncedSearch, page: currentPage, size: 10 });
      }
    } catch (err) {
    } finally {
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

  const handleSearchChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const renderPagination = () => {
    if (!pagination || pagination.totalPages <= 1) return null;
    const totalPages = pagination.totalPages;
    const current = currentPage;
    let startPage = Math.max(0, current - 2);
    let endPage = Math.min(totalPages - 1, current + 2);

    if (current < 2) {
      endPage = Math.min(4, totalPages - 1);
    }
    if (current > totalPages - 3) {
      startPage = Math.max(0, totalPages - 5);
    }

    const pages = [];
    for (let i = startPage; i <= endPage; i++) {
      pages.push(
        <button
          key={i}
          className={`${styles.pageButton} ${currentPage === i ? styles.activePage : ''}`}
          onClick={() => handlePageChange(i)}
        >
          {i + 1}
        </button>
      );
    }

    return (
      <div className={styles.pagination}>
        <button
          className={styles.pageButton}
          disabled={currentPage === 0}
          onClick={() => handlePageChange(currentPage - 1)}
        >
          ←
        </button>

        {startPage > 0 && (
          <>
            <button
              className={styles.pageButton}
              onClick={() => handlePageChange(0)}
            >
              1
            </button>
            {startPage > 1 && <span className={styles.ellipsis}>...</span>}
          </>
        )}

        {pages}

        {endPage < totalPages - 1 && (
          <>
            {endPage < totalPages - 2 && <span className={styles.ellipsis}>...</span>}
            <button
              className={styles.pageButton}
              onClick={() => handlePageChange(totalPages - 1)}
            >
              {totalPages}
            </button>
          </>
        )}

        <button
          className={styles.pageButton}
          disabled={currentPage >= totalPages - 1}
          onClick={() => handlePageChange(currentPage + 1)}
        >
          →
        </button>

        <span className={styles.pageInfo}>
          {currentPage + 1} / {totalPages}
        </span>
      </div>
    );
  };

  if (loading && genres.length === 0) {
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

      <div className={styles.searchContainer}>
        <div className={styles.searchWrapper}>
          <input
            type="text"
            placeholder="Search genres..."
            value={searchQuery}
            onChange={handleSearchChange}
            className={styles.searchInput}
          />
          {loading && <div className={styles.searchSpinner}></div>}
        </div>
        {searchQuery && (
          <button
            className={styles.clearSearch}
            onClick={() => setSearchQuery('')}
          >
            Clear
          </button>
        )}
      </div>

      {pagination && (
        <div className={styles.resultsInfo}>
          {genres.length} of {pagination.totalElements} genres
          {debouncedSearch && ` for "${debouncedSearch}"`}
        </div>
      )}

      <div className={styles.grid}>
        {genres.length === 0 && !loading ? (
          <div className={styles.empty}>
            <div className={styles.emptyIcon}>📚</div>
            <h3>No genres found</h3>
            <p>
              {debouncedSearch
                ? `No genres found for "${debouncedSearch}"`
                : 'Create your first genre to get started!'
              }
            </p>
            {!debouncedSearch && (
              <button
                className={styles.primaryButton}
                onClick={() => setIsModalOpen(true)}
              >
                Create First Genre
              </button>
            )}
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
                  onClick={() => {
                    setDeletingGenre(genre);
                    setIsDeleteModalOpen(true);
                  }}
                >
                  Delete
                </button>
              </div>
            </div>
          ))
        )}
      </div>

      {renderPagination()}

      {isModalOpen && (
        <div className={styles.modalOverlay}>
          <div className={styles.modal}>
            <h3>{editingGenre ? 'Edit Genre' : 'Add New Genre'}</h3>
            <form onSubmit={handleSubmit} className={styles.form}>
              <div className={styles.formGroup}>
                <input
                  type="text"
                  placeholder="Genre name"
                  value={formData.name}
                  onChange={(e) => setFormData({ name: e.target.value })}
                  required
                  maxLength={50}
                  className={styles.formInput}
                />
                <small>Maximum 50 characters</small>
              </div>
              <div className={styles.formActions}>
                <button
                  type="submit"
                  className={styles.primaryButton}
                  disabled={mutationLoading}
                >
                  {mutationLoading ? 'Saving...' : (editingGenre ? 'Update' : 'Create')} Genre
                </button>
                <button
                  type="button"
                  className={styles.cancelButton}
                  onClick={resetForm}
                  disabled={mutationLoading}
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={handleDeleteCancel}
        itemName={deletingGenre?.name}
        itemType="genre"
        isDeleting={mutationLoading}
      />

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