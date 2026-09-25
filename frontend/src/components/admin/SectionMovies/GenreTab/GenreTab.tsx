import React, { useState, useEffect, useCallback, useMemo } from "react";
import type { GenreListResponse } from "@/types/genre";
import { useGenre } from "@/hooks/features/genre/useGenre";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { useUrlParams } from "@/hooks/common/useUrlParams";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { Button } from "@/components/ui/Button/Button";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { DeleteConfirmModal } from "@/components/ui/DeleteConfirmModal/DeleteConfirmModal";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { DEFAULT_PAGE_SIZE_COMPACT } from "@/utils/paginationUtils";
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

  const { page, query, setPage, setSearch } = useUrlParams();
  const { loading, getAll, create, update, remove } = useGenre();
  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const loadGenres = useCallback(async () => {
    const response = await getAll({
      query,
      page,
      size: DEFAULT_PAGE_SIZE_COMPACT,
    });
    if (response) {
      setTabData({
        data: response.content,
        total: response.totalElements,
        totalPages: response.totalPages,
      });
    }
  }, [getAll, query, page]);

  useEffect(() => {
    loadGenres().catch(() => {});
  }, [loadGenres]);

  const handleSubmit = useCallback(
    async (name: string) => {
      try {
        if (editingGenre) {
          await update(editingGenre.id, { name });
        } else {
          await create({ name });
        }
      } catch {
        return;
      }
      setIsFormModalOpen(false);
      setEditingGenre(null);
      loadGenres().catch(() => {});
    },
    [editingGenre, create, update, loadGenres],
  );

  const handleDeleteConfirm = useCallback(async () => {
    if (!deletingGenre) return;
    try {
      await remove(deletingGenre.id);
    } catch {
      return;
    } finally {
      setIsDeleteModalOpen(false);
      setDeletingGenre(null);
    }
    if (tabData.data.length === 1 && page > 0) {
      setPage(page - 1);
    } else {
      loadGenres().catch(() => {});
    }
  }, [deletingGenre, remove, loadGenres, tabData.data.length, page, setPage]);

  const paginationInfo = useMemo(() => {
    const total = tabData.total;
    const start = total > 0 ? page * DEFAULT_PAGE_SIZE_COMPACT + 1 : 0;
    const end = Math.min(start + DEFAULT_PAGE_SIZE_COMPACT - 1, total);
    return {
      total,
      start,
      end,
      totalPages: tabData.totalPages,
      showPagination: tabData.totalPages > 1,
    };
  }, [tabData.total, tabData.totalPages, page]);

  if (showDelayedLoading && !tabData.data.length) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading genres..." />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <PageHeader
        title="Genres"
        subtitle="Manage movie genres and their statistics"
        divider
        actions={
          <Button
            variant="primary"
            onClick={() => {
              setEditingGenre(null);
              setIsFormModalOpen(true);
            }}
          >
            Add Genre
          </Button>
        }
      />

      <div className={styles.searchSection}>
        <SearchInput
          onSearch={setSearch}
          value={query}
          placeholder="Search genres..."
          delay={300}
        />
      </div>

      {paginationInfo.total > 0 && (
        <div className={styles.resultsInfo}>
          Showing {paginationInfo.start}-{paginationInfo.end} of{" "}
          {paginationInfo.total} genres
          {query && ` for "${query}"`}
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
            currentPage={page}
            totalPages={paginationInfo.totalPages}
            totalElements={paginationInfo.total}
            pageSize={DEFAULT_PAGE_SIZE_COMPACT}
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
        genre={editingGenre}
        loading={loading}
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
