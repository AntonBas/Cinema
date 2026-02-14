import React, { useState, useEffect, useRef, useCallback } from 'react';
import type { GenreResponse } from '@/types/genre';
import type { SearchParams } from '@/types/pagination';
import { useGenres } from '@/hooks/features/genres/useGenres';
import { Notification, SearchInput, Button, Pagination, DeleteConfirmModal } from '@/components/ui';
import { GenreTable } from './GenreTable/GenreTable';
import { GenreFormModal } from './GenreFormModal/GenreFormModal';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './GenreTab.module.css';

export const GenreTab: React.FC = () => {
  const {
    allGenres,
    pagination,
    loading,
    getAll,
    create,
    update,
    remove,
    currentPage,
    totalPages,
    pageSize,
    totalElements,
  } = useGenres();

  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingGenre, setEditingGenre] = useState<GenreResponse | null>(null);
  const [deletingGenre, setDeletingGenre] = useState<GenreResponse | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [currentParams, setCurrentParams] = useState<SearchParams>({
    page: 0,
    size: 20
  });

  const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const isMountedRef = useRef(false);

  useEffect(() => {
    isMountedRef.current = true;
    getAll(currentParams);

    return () => {
      isMountedRef.current = false;
    };
  }, []);

  useEffect(() => {
    if (isMountedRef.current) {
      getAll(currentParams);
    }
  }, [currentParams]);

  const handleSubmit = async (name: string) => {
    if (!name.trim()) {
      setErrorMessage('Genre name is required');
      return;
    }

    try {
      if (editingGenre?.id) {
        const result = await update(editingGenre.id, { name });
        setSuccessMessage(`Genre "${result.name}" updated successfully!`);
      } else {
        const result = await create({ name });
        setSuccessMessage(`Genre "${result.name}" created successfully!`);
      }

      closeFormModal();

      setCurrentParams(prev => ({
        ...prev,
        page: 0
      }));
    } catch (err) {
      if (isApiErrorException(err)) {
        const validationError = err.getFirstValidationError();
        if (validationError) {
          setErrorMessage(validationError);
        } else {
          setErrorMessage(err.message);
        }
      } else {
        setErrorMessage(err instanceof Error ? err.message : 'Failed to save genre');
      }
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingGenre?.id) return;

    try {
      await remove(deletingGenre.id);
      setSuccessMessage(`Genre "${deletingGenre.name}" deleted successfully!`);

      if (allGenres.length === 1 && currentPage > 0) {
        setCurrentParams(prev => ({
          ...prev,
          page: currentPage - 1
        }));
      } else {
        getAll(currentParams);
      }
    } catch (err) {
      if (isApiErrorException(err)) {
        if (err.isConflict()) {
          setErrorMessage(`Cannot delete genre "${deletingGenre.name}" because it has associated movies.`);
        } else {
          setErrorMessage(err.message);
        }
      } else {
        setErrorMessage(err instanceof Error ? err.message : 'Failed to delete genre');
      }
    } finally {
      setIsDeleteModalOpen(false);
      setDeletingGenre(null);
    }
  };

  const handleEdit = useCallback((genre: GenreResponse) => {
    setEditingGenre(genre);
    setIsFormModalOpen(true);
  }, []);

  const handleDelete = useCallback((genre: GenreResponse) => {
    setDeletingGenre(genre);
    setIsDeleteModalOpen(true);
  }, []);

  const handleSearch = (query: string) => {
    setSearchQuery(query);

    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    searchTimeoutRef.current = setTimeout(() => {
      const newParams = {
        ...currentParams,
        page: 0,
        ...(query.trim() && { search: query })
      };

      if (!query.trim()) {
        delete newParams.search;
      }

      setCurrentParams(newParams);
    }, 500);
  };

  const handlePageChange = (page: number) => {
    setCurrentParams(prev => ({
      ...prev,
      page
    }));
  };

  const openFormModal = () => {
    setEditingGenre(null);
    setIsFormModalOpen(true);
  };

  const closeFormModal = () => {
    setIsFormModalOpen(false);
    setEditingGenre(null);
  };

  const handleCloseNotification = () => {
    setErrorMessage('');
    setSuccessMessage('');
  };

  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  if (loading && !allGenres.length) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
        <p>Loading genres...</p>
      </div>
    );
  }

  const getDisplayRange = () => {
    if (!pagination) return { start: 0, end: 0 };
    const startItem = currentPage * pageSize + 1;
    const endItem = Math.min((currentPage + 1) * pageSize, totalElements);
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
        <div className={styles.headerContent}>
          <h2>Genre Management</h2>
          <p className={styles.subtitle}>
            Manage movie genres and their statistics
          </p>
        </div>
        <Button
          variant="primary"
          onClick={openFormModal}
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
      </div>

      {pagination && (
        <div className={styles.resultsInfo}>
          Showing {start}-{end} of {totalElements} genres
          {searchQuery && ` for "${searchQuery}"`}
        </div>
      )}

      <GenreTable
        genres={allGenres}
        onEdit={handleEdit}
        onDelete={handleDelete}
        loading={loading}
      />

      {pagination && totalPages > 1 && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            totalElements={totalElements}
            pageSize={pageSize}
            onPageChange={handlePageChange}
            variant="pages"
            loading={loading}
            className={styles.pagination}
            showInfo={false}
          />
        </div>
      )}

      <GenreFormModal
        isOpen={isFormModalOpen}
        onClose={closeFormModal}
        onSubmit={handleSubmit}
        initialName={editingGenre?.name || ''}
        loading={loading}
        isEditing={!!editingGenre}
      />

      <DeleteConfirmModal
        isOpen={isDeleteModalOpen}
        onConfirm={handleDeleteConfirm}
        onCancel={() => {
          setIsDeleteModalOpen(false);
          setDeletingGenre(null);
        }}
        itemName={deletingGenre?.name}
        itemType="genre"
        isDeleting={loading}
      />
    </div>
  );
};