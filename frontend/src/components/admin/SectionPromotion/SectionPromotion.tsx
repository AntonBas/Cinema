import React, { useState, useEffect, useCallback } from "react";
import { Button } from "@/components/ui/Button/Button";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { ConfirmModal } from "@/components/ui/ConfirmModal/ConfirmModal";
import { LoadingSpinner } from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { usePromotion } from "@/hooks/features/promotion/usePromotion";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { useUrlParams } from "@/hooks/common/useUrlParams";
import { DEFAULT_PAGE_SIZE_COMPACT } from "@/utils/paginationUtils";
import type {
  PromotionResponse,
  PromotionListResponse,
} from "@/types/promotion";
import { PromotionTable } from "./PromotionTable/PromotionTable";
import { PromotionFormModal } from "./PromotionFormModal/PromotionFormModal";
import { PageHeader } from "@/components/ui/PageHeader/PageHeader";
import styles from "./SectionPromotion.module.css";

export const SectionPromotion: React.FC = () => {
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingPromotion, setEditingPromotion] =
    useState<PromotionResponse | null>(null);
  const [deletingPromotion, setDeletingPromotion] = useState<{
    id: number;
    title: string;
  } | null>(null);

  const { page, query, setPage, setSearch } = useUrlParams();
  const {
    adminPromotions,
    pagination,
    getAdminById,
    getAdminPromotions,
    remove,
    loading,
  } = usePromotion();
  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const loadPromotions = useCallback(() => {
    getAdminPromotions({
      query,
      page,
      size: DEFAULT_PAGE_SIZE_COMPACT,
    });
  }, [query, page, getAdminPromotions]);

  useEffect(() => {
    loadPromotions();
  }, [loadPromotions]);

  const handleCreateSuccess = useCallback(() => {
    setShowCreateModal(false);
    loadPromotions();
  }, [loadPromotions]);

  const handleUpdateSuccess = useCallback(() => {
    setEditingPromotion(null);
    loadPromotions();
  }, [loadPromotions]);

  const handleDeleteConfirm = async () => {
    if (!deletingPromotion) return;

    await remove(deletingPromotion.id);
    setDeletingPromotion(null);

    if (adminPromotions.length === 1 && page > 0) {
      setPage(page - 1);
    } else {
      loadPromotions();
    }
  };

  const handleEdit = async (id: number) => {
    const promotion = await getAdminById(id);
    if (promotion) {
      setEditingPromotion(promotion);
    }
  };

  if (showDelayedLoading && !adminPromotions.length) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading promotions..." />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <PageHeader
        title="Promotions"
        subtitle="Run time-limited campaigns that award bonus points"
        actions={
          <Button onClick={() => setShowCreateModal(true)} variant="primary">
            Create Promotion
          </Button>
        }
      />

      <div className={styles.filtersContainer}>
        <div className={styles.searchWrapper}>
          <SearchInput
            value={query}
            onSearch={setSearch}
            placeholder="Search promotions..."
            delay={300}
          />
        </div>
      </div>

      {pagination && pagination.totalElements > 0 && (
        <div className={styles.resultsInfo}>
          Showing {pagination.number * pagination.size + 1}-
          {Math.min(
            (pagination.number + 1) * pagination.size,
            pagination.totalElements,
          )}{" "}
          of {pagination.totalElements} promotions
          {query && ` for "${query}"`}
        </div>
      )}

      <PromotionTable
        promotions={adminPromotions as PromotionListResponse[]}
        onEdit={handleEdit}
        onDelete={(id, title) => setDeletingPromotion({ id, title })}
      />

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={pagination.number}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pagination.size}
            onPageChange={setPage}
            variant="pages"
            showInfo={false}
          />
        </div>
      )}

      {showCreateModal && (
        <PromotionFormModal
          onClose={() => setShowCreateModal(false)}
          onSuccess={handleCreateSuccess}
        />
      )}

      {editingPromotion && (
        <PromotionFormModal
          promotion={editingPromotion}
          onClose={() => setEditingPromotion(null)}
          onSuccess={handleUpdateSuccess}
        />
      )}

      {deletingPromotion && (
        <ConfirmModal
          isOpen={true}
          onConfirm={handleDeleteConfirm}
          onCancel={() => setDeletingPromotion(null)}
          title="Delete Promotion"
          message={`Are you sure you want to delete promotion "${deletingPromotion.title}"?`}
          confirmText="Delete"
          variant="error"
        />
      )}
    </div>
  );
};
