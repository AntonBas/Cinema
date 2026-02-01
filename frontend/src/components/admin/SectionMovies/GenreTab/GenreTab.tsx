import React, { useState, useEffect, useCallback } from 'react';
import type { GenreResponse, GenreRequest } from '@/types/genre';
import { useGenres } from '@/hooks/features/genres/useGenres';
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
  const [currentPage, setCurrentPage] = useState(0);
  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');

  const {
    genres,
    pagination,
    loading,
    search,
    getAllPaginated,
    create,
    update,
    remove
  } = useGenres();

  const loadGenres = useCallback(async () => {
    try {
      if (searchQuery.trim()) {
        await search({
          query: searchQuery,
          page: currentPage,
          size: 12
        });
      } else {
        await getAllPaginated({
          page: currentPage,
          size: 12
        });
      }
      setErrorMessage('');
    } catch (error) {
      setErrorMessage('Failed to load genres');
    }
  }, [searchQuery, currentPage, search, getAllPaginated]);

  useEffect(() => {
    loadGenres();
  }, [loadGenres]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.name.trim()) {
      setErrorMessage('Genre name is required');
      return;
    }

    try {
      if (editingGenre?.id) {
        await update(editingGenre.id, formData);
        setSuccessMessage('Genre updated successfully!');
      } else {
        await create(formData);
        setSuccessMessage('Genre created successfully!');
      }

      resetForm();
      await loadGenres();
    } catch (err) {
      setErrorMessage('Failed to save genre');
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingGenre?.id) return;

    try {
      await remove(deletingGenre.id);
      setSuccessMessage('Genre deleted successfully!');

      if (genres.length === 1 && currentPage > 0) {
        setCurrentPage(currentPage - 1);
      } else {
        await loadGenres();
      }
    } catch (err) {
      setErrorMessage('Failed to delete genre');
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

  const handleSearch = useCallback((query: string) => {
    if (query !== searchQuery) {
      setSearchQuery(query);
      setCurrentPage(0);
    }
  }, [searchQuery]);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleCloseNotification = () => {
    setErrorMessage('');
    setSuccessMessage('');
  };

  if (loading && genres.length === 0) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
        <p>Loading genres...</p>
      </div>
    );
  }

  const getDisplayRange = () => {
    if (!pagination) return { start: 0, end: 0 };

    const startItem = currentPage * 12 + 1;
    const endItem = Math.min((currentPage + 1) * 12, pagination.totalElements);

    return { start: startItem, end: endItem };
  };

  const { start, end } = getDisplayRange();

  return (
    <div className={styles.container}>
      {successMessage && (
        <Notification
          id="success"
          message={successMessage}
          type="success"
          isVisible={true}
          onClose={handleCloseNotification}
          duration={4000}
        />
      )}

      {errorMessage && (
        <Notification
          id="error"
          message={errorMessage}
          type="error"
          isVisible={true}
          onClose={handleCloseNotification}
          duration={4000}
        />
      )}

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
              Showing {start}-{end} of {pagination.totalElements} genres
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
            currentPage={currentPage}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pagination.size}
            onPageChange={handlePageChange}
            variant="pages"
            loading={loading}
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
            disabled={loading}
            className={styles.formInput}
          />
          <div className={styles.formHint}>Maximum 50 characters</div>

          <div className={styles.formActions}>
            <Button
              type="button"
              variant="cancel"
              onClick={resetForm}
              disabled={loading}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              variant="primary"
              loading={loading}
              disabled={loading}
            >
              {editingGenre ? 'Update' : 'Create'} Genre
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
        isDeleting={loading}
      />
    </div>
  );
};