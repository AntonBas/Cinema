import React, { useState, useEffect, useCallback } from 'react';
import type { GenreListResponse } from '@/types/genre';
import { useGenres } from '@/hooks/features/genres/useGenres';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import { Button } from '@/components/ui/Button/Button';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal/DeleteConfirmModal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { GenreTable } from './GenreTable/GenreTable';
import { GenreFormModal } from './GenreFormModal/GenreFormModal';
import styles from './GenreTab.module.css';

export const GenreTab: React.FC = () => {
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingGenre, setEditingGenre] = useState<GenreListResponse | null>(null);
  const [deletingGenre, setDeletingGenre] = useState<GenreListResponse | null>(null);

  const { setSearch } = usePagination({ size: 10 });

  const {
    genres,
    loading,
    getAll,
    create,
    update,
    remove,
  } = useGenres();

  const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

  useEffect(() => {
    getAll({});
  }, []);

  const handleSearch = useCallback((query: string) => {
    setSearch(query);
    getAll({ query: query });
  }, [setSearch]);

  const handleSubmit = useCallback(async (name: string) => {
    if (editingGenre) {
      await update(editingGenre.id, { name });
    } else {
      await create({ name });
    }
    setIsFormModalOpen(false);
    setEditingGenre(null);
    getAll({});
  }, [editingGenre]);

  const handleDeleteConfirm = useCallback(async () => {
    if (!deletingGenre) return;

    await remove(deletingGenre.id);
    setIsDeleteModalOpen(false);
    setDeletingGenre(null);
    getAll({});
  }, [deletingGenre]);

  const handleEdit = useCallback((genre: GenreListResponse) => {
    setEditingGenre(genre);
    setIsFormModalOpen(true);
  }, []);

  const handleDelete = useCallback((genre: GenreListResponse) => {
    setDeletingGenre(genre);
    setIsDeleteModalOpen(true);
  }, []);

  const handleAddNew = useCallback(() => {
    setEditingGenre(null);
    setIsFormModalOpen(true);
  }, []);

  if (showDelayedLoading && !genres.length) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading genres" />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div className={styles.headerContent}>
          <h2>Genre Management</h2>
          <p className={styles.subtitle}>Manage movie genres and their statistics</p>
        </div>
        <Button variant="primary" onClick={handleAddNew} className={styles.addButton}>
          Add Genre
        </Button>
      </div>

      <div className={styles.searchSection}>
        <SearchInput onSearch={handleSearch} placeholder="Search genres..." delay={300} />
      </div>

      <GenreTable genres={genres} onEdit={handleEdit} onDelete={handleDelete} />

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