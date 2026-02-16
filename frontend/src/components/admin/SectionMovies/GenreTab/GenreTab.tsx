import React, { useState, useEffect, useRef, useCallback } from 'react';
import type { GenreResponse } from '@/types/genre';
import type { SearchParams } from '@/types/pagination';
import { useGenres } from '@/hooks/features/genres/useGenres';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { SearchInput, Button, Pagination, DeleteConfirmModal, LoadingSpinner } from '@/components/ui';
import { Notification } from '@/components/ui/Notification';
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

  const { notifications, showNotification, hideNotification } = useNotification();
  const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

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
  const initialLoadRef = useRef(false);

  useEffect(() => {
    if (!initialLoadRef.current) {
      initialLoadRef.current = true;
      getAll(currentParams);
    }
  }, []);

  useEffect(() => {
    if (initialLoadRef.current) {
      const timer = setTimeout(() => {
        getAll(currentParams);
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [currentParams]);

  const handleSubmit = async (name: string) => {
    if (!name.trim()) {
      showNotification('Genre name is required', 'error');
      return;
    }

    try {
      if (editingGenre?.id) {
        const result = await update(editingGenre.id, { name });
        showNotification(`Genre "${result.name}" updated successfully!`, 'success');
      } else {
        const result = await create({ name });
        showNotification(`Genre "${result.name}" created successfully!`, 'success');
      }

      closeFormModal();
      setCurrentParams(prev => ({ ...prev, page: 0 }));
    } catch (err) {
      if (isApiErrorException(err)) {
        const validationError = err.getFirstValidationError();
        showNotification(validationError || err.message, 'error');
      } else {
        showNotification(err instanceof Error ? err.message : 'Failed to save genre', 'error');
      }
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingGenre?.id) return;

    try {
      await remove(deletingGenre.id);
      showNotification(`Genre "${deletingGenre.name}" deleted successfully!`, 'success');

      if (allGenres.length === 1 && currentPage > 0) {
        setCurrentParams(prev => ({ ...prev, page: currentPage - 1 }));
      } else {
        getAll(currentParams);
      }
    } catch (err) {
      if (isApiErrorException(err)) {
        if (err.isConflict()) {
          showNotification(`Cannot delete genre "${deletingGenre.name}" because it has associated movies.`, 'error');
        } else {
          showNotification(err.message, 'error');
        }
      } else {
        showNotification(err instanceof Error ? err.message : 'Failed to delete genre', 'error');
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

  const handleSearch = useCallback((query: string) => {
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
  }, [currentParams]);

  const handlePageChange = useCallback((page: number) => {
    setCurrentParams(prev => ({ ...prev, page }));
  }, []);

  const openFormModal = useCallback(() => {
    setEditingGenre(null);
    setIsFormModalOpen(true);
  }, []);

  const closeFormModal = useCallback(() => {
    setIsFormModalOpen(false);
    setEditingGenre(null);
  }, []);

  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  if (showDelayedLoading && !allGenres.length) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading genres" />
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
      {notifications.map((notification) => (
        <Notification
          key={notification.id}
          id={notification.id}
          message={notification.message}
          type={notification.type}
          isVisible={notification.isVisible}
          onClose={hideNotification}
          duration={notification.duration}
        />
      ))}

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
        />
      </div>

      {pagination && totalElements > 0 && (
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