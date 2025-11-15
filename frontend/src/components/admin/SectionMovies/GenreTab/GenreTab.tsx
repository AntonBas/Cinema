import React, { useState, useEffect } from 'react';
import type { GenreResponse, GenreRequest } from '@/types/genre';
import { useGenreSearch, useGenreMutation } from '@/hooks/features/genres';
import { useNotification } from '@/hooks/common/useNotification';
import { usePagination } from '@/hooks/common/usePagination';
import {
  Notification,
  DeleteConfirmModal,
  SearchInput,
  Button,
  Input,
  Modal,
  Pagination
} from '@/components/ui';
import styles from './GenreTab.module.css';

export const GenreTab: React.FC = () => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingGenre, setEditingGenre] = useState<GenreResponse | null>(null);
  const [deletingGenre, setDeletingGenre] = useState<GenreResponse | null>(null);
  const [formData, setFormData] = useState<GenreRequest>({ name: '' });
  const [searchQuery, setSearchQuery] = useState('');

  const { page: currentPage, setPage: setCurrentPage } = usePagination(0, 12);

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
    searchGenres({ query: searchQuery, page: currentPage, size: 12 });
  }, [searchQuery, currentPage]);

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

    if (!formData.name.trim()) {
      showNotification('Genre name is required', 'error');
      return;
    }

    try {
      if (editingGenre?.id) {
        await updateGenre(editingGenre.id, formData);
        showNotification('Genre updated successfully!', 'success');
      } else {
        await createGenre(formData);
        showNotification('Genre created successfully!', 'success');
      }

      resetForm();
      await searchGenres({ query: searchQuery, page: currentPage, size: 12 });
    } catch (err) {
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingGenre?.id) return;

    try {
      await deleteGenre(deletingGenre.id);
      showNotification('Genre deleted successfully!', 'success');

      if (genres.length === 1 && currentPage > 0) {
        setCurrentPage(currentPage - 1);
      } else {
        await searchGenres({ query: searchQuery, page: currentPage, size: 12 });
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

  const handleEdit = (genre: GenreResponse) => {
    setEditingGenre(genre);
    setFormData({ name: genre.name });
    setIsModalOpen(true);
  };

  const handleSearch = (query: string) => {
    if (query !== searchQuery) {
      setSearchQuery(query);
      setCurrentPage(0);
    }
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
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
        <Button
          variant="primary"
          onClick={() => setIsModalOpen(true)}
          className={styles.addButton}
        >
          Add Genre
        </Button>
      </div>

      <div className={styles.searchSection}>
        <SearchInput
          onSearch={handleSearch}
          placeholder="Search genres..."
          delay={500}
          className={styles.searchInput}
        />

        {pagination && (
          <div className={styles.resultsInfo}>
            <span>
              Showing {genres.length} of {pagination.totalElements} genres
              {searchQuery && ` for "${searchQuery}"`}
            </span>
          </div>
        )}
      </div>

      <div className={styles.grid}>
        {genres.length === 0 && !loading ? (
          <div className={styles.empty}>
            <div className={styles.emptyIcon}>📚</div>
            <h3>No genres found</h3>
            <p>
              {searchQuery
                ? `No genres found for "${searchQuery}"`
                : 'Create your first genre to get started!'
              }
            </p>
            {!searchQuery && (
              <Button
                variant="primary"
                onClick={() => setIsModalOpen(true)}
                className={styles.addButton}
              >
                Create First Genre
              </Button>
            )}
          </div>
        ) : (
          genres.map(genre => (
            <div key={genre.id} className={styles.card}>
              <div className={styles.cardContent}>
                <h3 className={styles.name}>{genre.name}</h3>
                <div className={styles.actions}>
                  <Button
                    variant="success"
                    size="small"
                    onClick={() => handleEdit(genre)}
                  >
                    Edit
                  </Button>
                  <Button
                    variant="error"
                    size="small"
                    onClick={() => {
                      setDeletingGenre(genre);
                      setIsDeleteModalOpen(true);
                    }}
                  >
                    Delete
                  </Button>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={pagination.currentPage}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pagination.pageSize}
            onPageChange={handlePageChange}
            className={styles.pagination}
            showInfo={false}
          />
        </div>
      )}

      <Modal
        isOpen={isModalOpen}
        onClose={resetForm}
        title={editingGenre ? 'Edit Genre' : 'Add New Genre'}
        size="small"
      >
        <form onSubmit={handleSubmit} className={styles.form}>
          <Input
            type="text"
            placeholder="Genre name"
            value={formData.name}
            onChange={(value) => setFormData({ name: value })}
            required
            maxLength={50}
            disabled={mutationLoading}
            className={styles.formInput}
          />
          <div className={styles.formHint}>Maximum 50 characters</div>

          <div className={styles.formActions}>
            <Button
              type="submit"
              variant="primary"
              loading={mutationLoading}
              disabled={mutationLoading}
            >
              {editingGenre ? 'Update' : 'Create'} Genre
            </Button>
            <Button
              type="button"
              variant="secondary"
              onClick={resetForm}
              disabled={mutationLoading}
            >
              Cancel
            </Button>
          </div>
        </form>
      </Modal>

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