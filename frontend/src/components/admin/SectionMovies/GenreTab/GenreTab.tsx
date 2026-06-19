import React, { useState, useEffect, useCallback, useMemo } from "react";
import type { GenreListResponse } from "@/types/genre";
import { useGenres } from "@/hooks/features/genres/useGenres";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { usePagination } from "@/hooks/common/usePagination";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { Button } from "@/components/ui/Button/Button";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { DeleteConfirmModal } from "@/components/ui/DeleteConfirmModal/DeleteConfirmModal";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { GenreTable } from "./GenreTable/GenreTable";
import { GenreFormModal } from "./GenreFormModal/GenreFormModal";
import styles from "./GenreTab.module.css";

export const GenreTab: React.FC = () => {
  const [isFormModalOpen, setIsFormModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [editingGenre, setEditingGenre] = useState<GenreListResponse | null>(
    null,
  );
  const [deletingGenre, setDeletingGenre] = useState<GenreListResponse | null>(
    null,
  );
  const [tabData, setTabData] = useState<{
    data: GenreListResponse[];
    total: number;
    totalPages: number;
  }>({
    data: [],
    total: 0,
    totalPages: 0,
  });

  const { params, setPage, setSearch } = usePagination({ size: 10 });
  const { loading, getAll, create, update, remove } = useGenres();
  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const loadGenres = useCallback(async () => {
    const response = await getAll({
      query: params.query,
      page: params.page || 0,
      size: params.size || 10,
    });
    if (response) {
      setTabData({
        data: response.content,
        total: response.totalElements,
        totalPages: response.totalPages,
      });
    }
  }, [getAll, params.query, params.page, params.size]);

  useEffect(() => {
    loadGenres();
  }, [loadGenres]);

  const handleSearch = useCallback(
    (query: string) => {
      setSearch(query);
      setPage(0);
    },
    [setSearch, setPage],
  );

  const handleSubmit = useCallback(
    async (name: string) => {
      if (editingGenre) {
        await update(editingGenre.id, { name });
      } else {
        await create({ name });
      }
      setIsFormModalOpen(false);
      setEditingGenre(null);
      loadGenres();
    },
    [editingGenre, create, update, loadGenres],
  );

  const handleDeleteConfirm = useCallback(async () => {
    if (!deletingGenre) return;
    await remove(deletingGenre.id);
    setIsDeleteModalOpen(false);
    setDeletingGenre(null);
    loadGenres();
  }, [deletingGenre, remove, loadGenres]);

  const paginationInfo = useMemo(() => {
    const total = tabData.total;
    const page = params.page || 0;
    const pageSize = params.size || 10;
    const start = total > 0 ? page * pageSize + 1 : 0;
    const end = Math.min(start + pageSize - 1, total);
    return {
      total,
      start,
      end,
      totalPages: tabData.totalPages,
      showPagination: tabData.totalPages > 1,
    };
  }, [tabData.total, tabData.totalPages, params.page, params.size]);

  if (showDelayedLoading && !tabData.data.length) {
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
          <p className={styles.subtitle}>
            Manage movie genres and their statistics
          </p>
        </div>
        <Button
          variant="primary"
          onClick={() => {
            setEditingGenre(null);
            setIsFormModalOpen(true);
          }}
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
          Showing {paginationInfo.start}-{paginationInfo.end} of{" "}
          {paginationInfo.total} genres
          {params.query && ` for "${params.query}"`}
        </div>
      )}

      <GenreTable
        genres={tabData.data}
        onEdit={(genre) => {
          setEditingGenre(genre);
          setIsFormModalOpen(true);
        }}
        onDelete={(genre) => {
          setDeletingGenre(genre);
          setIsDeleteModalOpen(true);
        }}
      />

      {paginationInfo.showPagination && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={params.page || 0}
            totalPages={paginationInfo.totalPages}
            totalElements={paginationInfo.total}
            pageSize={params.size || 10}
            onPageChange={setPage}
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
        initialName={editingGenre?.name || ""}
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
