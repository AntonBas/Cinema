import React, { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import type { GenreResponse } from '@/types/genre';
import { useGenres } from '@/hooks/features/genres/useGenres';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import { Button } from '@/components/ui/Button/Button';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal/DeleteConfirmModal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { Notification } from '@/components/ui/Notification/Notification';
import { GenreTable } from './GenreTable/GenreTable';
import { GenreFormModal } from './GenreFormModal/GenreFormModal';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './GenreTab.module.css';

interface TabData {
  data: GenreResponse[];
  total: number;
}

export const GenreTab: React.FC = () => {
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingGenre, setEditingGenre] = useState<GenreResponse | null>(null);
  const [deletingGenre, setDeletingGenre] = useState<GenreResponse | null>(null);
  const [tabData, setTabData] = useState<TabData>({
    data: [],
    total: 0
  });

  const { notifications, showNotification, hideNotification } = useNotification();
  const { params, setPage, setSearch } = usePagination({ size: 20 });

  const searchTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const initialLoadRef = useRef(false);
  const loadingDataRef = useRef(false);

  const {
    loading,
    getAll,
    create,
    update,
    remove,
  } = useGenres();

  const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

  const loadTabData = useCallback(async (page: number, search?: string) => {
    if (loadingDataRef.current) return;

    loadingDataRef.current = true;

    try {
      const params = {
        page,
        size: 20,
        ...(search?.trim() && { search: search.trim() })
      };

      const response = await getAll(params);

      setTabData({
        data: response?.content || [],
        total: response?.totalElements || 0
      });
    } catch (error) {
      if (isApiErrorException(error)) {
        showNotification(error.message, 'error');
      } else {
        showNotification('Failed to load genres', 'error');
      }
    } finally {
      loadingDataRef.current = false;
    }
  }, [getAll, showNotification]);

  useEffect(() => {
    if (!initialLoadRef.current) {
      initialLoadRef.current = true;
      loadTabData(params.page || 0, params.search);
    }
  }, [loadTabData, params.page, params.search]);

  useEffect(() => {
    if (initialLoadRef.current) {
      const timer = setTimeout(() => {
        loadTabData(params.page || 0, params.search);
      }, 100);

      return () => clearTimeout(timer);
    }
  }, [params.page, params.search, loadTabData]);

  const handleSearch = useCallback((query: string) => {
    if (searchTimeoutRef.current) {
      clearTimeout(searchTimeoutRef.current);
    }

    searchTimeoutRef.current = setTimeout(() => {
      setSearch(query);
    }, 300);
  }, [setSearch]);

  const handlePageChange = useCallback((page: number) => {
    setPage(page);
  }, [setPage]);

  const handleSubmit = async (name: string) => {
    if (!name.trim()) {
      showNotification('Genre name is required', 'error');
      return;
    }

    try {
      if (editingGenre) {
        const response = await update(editingGenre.id, { name });
        if (response) {
          showNotification(`Genre "${response.name}" updated successfully!`, 'success');
        }
      } else {
        const response = await create({ name });
        if (response) {
          showNotification(`Genre "${response.name}" created successfully!`, 'success');
        }
      }
      setIsFormModalOpen(false);
      setEditingGenre(null);
      await loadTabData(0, params.search);
      setPage(0);
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
    if (!deletingGenre) return;

    try {
      await remove(deletingGenre.id);
      showNotification(`Genre "${deletingGenre.name}" deleted successfully!`, 'success');

      const newPage = tabData.data.length === 1 && params.page && params.page > 0
        ? params.page - 1
        : params.page || 0;

      setPage(newPage);
      await loadTabData(newPage, params.search);
    } catch (err) {
      if (isApiErrorException(err) && err.isConflict()) {
        showNotification(`Cannot delete genre "${deletingGenre.name}" because it has associated movies.`, 'error');
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

  const handleAddNew = useCallback(() => {
    setEditingGenre(null);
    setIsFormModalOpen(true);
  }, []);

  useEffect(() => {
    return () => {
      if (searchTimeoutRef.current) {
        clearTimeout(searchTimeoutRef.current);
      }
    };
  }, []);

  const paginationInfo = useMemo(() => {
    const total = tabData.total;
    const page = params.page || 0;
    const pageSize = params.size || 20;
    const start = total > 0 ? page * pageSize + 1 : 0;
    const end = Math.min(start + pageSize - 1, total);
    const totalPages = Math.ceil(total / pageSize);

    return { total, start, end, totalPages, showPagination: totalPages > 1 };
  }, [tabData.total, params.page, params.size]);

  if (showDelayedLoading && !tabData.data.length && !params.search) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading genres" />
      </div>
    );
  }

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
          onClick={handleAddNew}
          className={styles.addButton}
        >
          Add Genre
        </Button>
      </div>

      <div className={styles.searchSection}>
        <SearchInput
          onSearch={handleSearch}
          placeholder="Search genres..."
          delay={300}
        />
      </div>

      {paginationInfo.total > 0 && (
        <div className={styles.resultsInfo}>
          Showing {paginationInfo.start}-{paginationInfo.end} of {paginationInfo.total} genres
          {params.search && ` for "${params.search}"`}
        </div>
      )}

      <GenreTable
        genres={tabData.data}
        onEdit={handleEdit}
        onDelete={handleDelete}
      />

      {paginationInfo.showPagination && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={params.page || 0}
            totalPages={paginationInfo.totalPages}
            totalElements={paginationInfo.total}
            pageSize={params.size || 20}
            onPageChange={handlePageChange}
            variant="pages"
            showInfo={false}
          />
        </div>
      )}

      <GenreFormModal
        isOpen={isFormModalOpen}
        onClose={() => {
          setIsFormModalOpen(false);
          setEditingGenre(null);
        }}
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