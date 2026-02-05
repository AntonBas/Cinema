import React, { useState, useEffect, useRef } from 'react';
import type { GenreResponse, GenreStatsResponse } from '@/types/genre';
import { useGenres } from '@/hooks/features/genres/useGenres';
import { Notification, SearchInput, Button, Pagination, DeleteConfirmModal } from '@/components/ui';
import { GenreTable } from './GenreTable/GenreTable';
import { GenreFormModal } from './GenreFormModal/GenreFormModal';
import styles from './GenreTab.module.css';

export const GenreTab: React.FC = () => {
  const {
    statsGenres,
    pagination,
    loading,
    getAllWithStats,
    create,
    update,
    remove,
    currentPage: hookCurrentPage,
    totalPages: hookTotalPages,
    pageSize: hookPageSize,
    nextPage,
    prevPage,
    refresh,
    currentSearch
  } = useGenres();

  const [successMessage, setSuccessMessage] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingGenre, setEditingGenre] = useState<GenreResponse | null>(null);
  const [deletingGenre, setDeletingGenre] = useState<GenreResponse | null>(null);
  const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const isInitialLoad = useRef(false);

  useEffect(() => {
    if (!isInitialLoad.current) {
      isInitialLoad.current = true;
      getAllWithStats({
        page: 0,
        size: 20
      });
    }
  }, [getAllWithStats]);

  const handleSubmit = async (name: string) => {
    if (!name.trim()) {
      setErrorMessage('Genre name is required');
      return;
    }

    try {
      if (editingGenre?.id) {
        await update(editingGenre.id, { name });
        setSuccessMessage('Genre updated successfully!');
      } else {
        await create({ name });
        setSuccessMessage('Genre created successfully!');
      }

      closeFormModal();
      await getAllWithStats({
        page: 0,
        size: hookPageSize,
        search: currentSearch
      });
    } catch (err) {
      setErrorMessage('Failed to save genre');
    }
  };

  const handleDeleteConfirm = async () => {
    if (!deletingGenre?.id) return;

    try {
      await remove(deletingGenre.id);
      setSuccessMessage('Genre deleted successfully!');

      if (statsGenres.length === 1 && hookCurrentPage > 0) {
        await getAllWithStats({
          page: hookCurrentPage - 1,
          size: hookPageSize,
          search: currentSearch
        });
      } else {
        await refresh();
      }
    } catch (err) {
      setErrorMessage('Failed to delete genre');
    } finally {
      setIsDeleteModalOpen(false);
      setDeletingGenre(null);
    }
  };

  const handleEdit = (genre: GenreStatsResponse) => {
    setEditingGenre({ id: genre.id, name: genre.name });
    setIsFormModalOpen(true);
  };

  const handleDelete = (genre: GenreStatsResponse) => {
    setDeletingGenre({ id: genre.id, name: genre.name });
    setIsDeleteModalOpen(true);
  };

  const handleSearch = (query: string) => {
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    searchTimeoutRef.current = setTimeout(async () => {
      try {
        await getAllWithStats({
          page: 0,
          size: hookPageSize,
          ...(query.trim() && { search: query })
        });
      } catch (error) {
        setErrorMessage('Failed to search genres');
      }
    }, 500);
  };

  const handlePageChange = async (page: number) => {
    try {
      if (page === hookCurrentPage + 1) {
        await nextPage();
      } else if (page === hookCurrentPage - 1) {
        await prevPage();
      } else {
        await getAllWithStats({
          page: page,
          size: hookPageSize,
          search: currentSearch
        });
      }
    } catch (error) {
      setErrorMessage('Failed to change page');
    }
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

  if (loading.getAllWithStats && !statsGenres.length) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner}></div>
        <p>Loading genres...</p>
      </div>
    );
  }

  const getDisplayRange = () => {
    if (!pagination) return { start: 0, end: 0 };
    const startItem = hookCurrentPage * hookPageSize + 1;
    const endItem = Math.min((hookCurrentPage + 1) * hookPageSize, pagination.totalElements);
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
          Showing {start}-{end} of {pagination.totalElements} genres
          {currentSearch && ` for "${currentSearch}"`}
        </div>
      )}

      <GenreTable
        genres={statsGenres}
        onEdit={handleEdit}
        onDelete={handleDelete}
        loading={loading.getAllWithStats}
      />

      {pagination && hookTotalPages > 1 && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={hookCurrentPage}
            totalPages={hookTotalPages}
            totalElements={pagination.totalElements}
            pageSize={hookPageSize}
            onPageChange={handlePageChange}
            variant="pages"
            loading={loading.getAllWithStats}
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
        loading={loading.create || loading.update}
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
        isDeleting={loading.remove}
      />
    </div>
  );
};