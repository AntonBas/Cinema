import React, { useState, useEffect, useCallback } from "react";
import { Button } from "@/components/ui/Button/Button";
import { SearchInput } from "@/components/ui/SearchInput/SearchInput";
import { Pagination } from "@/components/ui/Pagination/Pagination";
import { ConfirmModal } from "@/components/ui/ConfirmModal/ConfirmModal";
import LoadingSpinner from "@/components/ui/LoadingSpinner/LoadingSpinner";
import { usePromotion } from "@/hooks/features/promotion/usePromotion";
import { useDelayedLoading } from "@/hooks/common/useDelayedLoading";
import { usePagination } from "@/hooks/common/usePagination";
import type {
  PromotionResponse,
  PromotionListResponse,
} from "@/types/promotion";
import PromotionTable from "./PromotionTable/PromotionTable";
import CreatePromotionModal from "./PromotionModal/CreatePromotionModal";
import EditPromotionModal from "./PromotionModal/EditPromotionModal";
import styles from "./SectionPromotion.module.css";

const SectionPromotion: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState<string>("");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingPromotion, setEditingPromotion] =
    useState<PromotionResponse | null>(null);
  const [deletingPromotion, setDeletingPromotion] = useState<{
    id: number;
    title: string;
  } | null>(null);

  const { params, setPage } = usePagination({ size: 10 });
  const { adminPromotions, pagination, getById, getAll, remove, loading } =
    usePromotion();
  const showDelayedLoading = useDelayedLoading(loading, {
    delay: 150,
    minDisplayTime: 300,
  });

  const currentPage = params.page ?? 0;
  const pageSize = params.size ?? 10;

  const loadPromotions = useCallback(
    (page: number = currentPage) => {
      getAll({
        query: searchQuery || undefined,
        page: page,
        size: pageSize,
      });
    },
    [searchQuery, pageSize, getAll],
  );

  useEffect(() => {
    loadPromotions(0);
  }, []);

  const handleSearch = useCallback(
    (query: string) => {
      setSearchQuery(query);
      setPage(0);
      getAll({
        query: query || undefined,
        page: 0,
        size: pageSize,
      });
    },
    [pageSize, getAll, setPage],
  );

  const handlePageChange = useCallback(
    (page: number) => {
      setPage(page);
      getAll({
        query: searchQuery || undefined,
        page: page,
        size: pageSize,
      });
    },
    [searchQuery, pageSize, getAll, setPage],
  );

  const handleCreateSuccess = useCallback(() => {
    setShowCreateModal(false);
    loadPromotions(currentPage);
  }, [currentPage, loadPromotions]);

  const handleUpdateSuccess = useCallback(() => {
    setEditingPromotion(null);
    loadPromotions(currentPage);
  }, [currentPage, loadPromotions]);

  const handleDeleteConfirm = async () => {
    if (!deletingPromotion) return;

    await remove(deletingPromotion.id);
    setDeletingPromotion(null);

    if (adminPromotions.length === 1 && currentPage > 0) {
      setPage(currentPage - 1);
      getAll({
        query: searchQuery || undefined,
        page: currentPage - 1,
        size: pageSize,
      });
    } else {
      loadPromotions(currentPage);
    }
  };

  const handleEdit = async (id: number) => {
    const promotion = await getById(id);
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
      <div className={styles.header}>
        <div>
          <h1 className={styles.title}>Promotion Management</h1>
          <p className={styles.subtitle}>Create and manage promotions</p>
        </div>
        <Button onClick={() => setShowCreateModal(true)} variant="primary">
          Create Promotion
        </Button>
      </div>

      <div className={styles.filtersContainer}>
        <div className={styles.searchWrapper}>
          <SearchInput
            onSearch={handleSearch}
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
          {searchQuery && ` for "${searchQuery}"`}
        </div>
      )}

      <div className={styles.tableContainer}>
        <PromotionTable
          promotions={adminPromotions as PromotionListResponse[]}
          onEdit={handleEdit}
          onDelete={(id, title) => setDeletingPromotion({ id, title })}
        />
      </div>

      {pagination && pagination.totalPages > 1 && (
        <div className={styles.paginationWrapper}>
          <Pagination
            currentPage={pagination.number}
            totalPages={pagination.totalPages}
            totalElements={pagination.totalElements}
            pageSize={pagination.size}
            onPageChange={handlePageChange}
            variant="pages"
            showInfo={false}
          />
        </div>
      )}

      {showCreateModal && (
        <CreatePromotionModal
          onClose={() => setShowCreateModal(false)}
          onSuccess={handleCreateSuccess}
        />
      )}

      {editingPromotion && (
        <EditPromotionModal
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

export default SectionPromotion;
