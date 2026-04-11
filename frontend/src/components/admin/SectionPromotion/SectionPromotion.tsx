import React, { useState, useEffect, useCallback } from 'react';
import { Button } from '@/components/ui/Button/Button';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import { ConfirmModal } from '@/components/ui/ConfirmModal/ConfirmModal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import type { PromotionResponse, PromotionListResponse } from '@/types/promotion';
import PromotionTable from './PromotionTable/PromotionTable';
import { PromotionFilters } from './PromotionFilters/PromotionFilters';
import CreatePromotionModal from './PromotionModal/CreatePromotionModal';
import EditPromotionModal from './PromotionModal/EditPromotionModal';
import styles from './SectionPromotion.module.css';

const SectionPromotion: React.FC = () => {
    const [statusFilter, setStatusFilter] = useState<string>('');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingPromotion, setEditingPromotion] = useState<PromotionResponse | null>(null);
    const [deletingPromotion, setDeletingPromotion] = useState<{ id: number; title: string } | null>(null);

    const { params, setPage } = usePagination({ size: 10 });
    const { adminPromotions, pagination, getAll, remove, loading } = usePromotion();
    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const currentPage = params.page ?? 0;
    const pageSize = params.size ?? 10;

    useEffect(() => {
        getAll({ page: currentPage, size: pageSize });
    }, [currentPage, pageSize, getAll]);

    const handleCreateSuccess = useCallback(() => {
        setShowCreateModal(false);
        getAll({ page: currentPage, size: pageSize });
    }, [getAll, currentPage, pageSize]);

    const handleUpdateSuccess = useCallback(() => {
        setEditingPromotion(null);
        getAll({ page: currentPage, size: pageSize });
    }, [getAll, currentPage, pageSize]);

    const handleDeleteConfirm = async () => {
        if (!deletingPromotion) return;

        await remove(deletingPromotion.id);
        setDeletingPromotion(null);

        if (adminPromotions.length === 1 && currentPage > 0) {
            setPage(currentPage - 1);
        } else {
            getAll({ page: currentPage, size: pageSize });
        }
    };

    const handleEdit = (id: number) => {
        const promotion = adminPromotions.find(p => p.id === id);
        if (promotion) {
            setEditingPromotion(promotion as PromotionResponse);
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
                    <SearchInput onSearch={() => { }} placeholder="Search promotions..." />
                </div>
                <PromotionFilters selectedStatus={statusFilter} onStatusChange={setStatusFilter} />
            </div>

            {pagination && pagination.totalElements > 0 && (
                <div className={styles.resultsInfo}>
                    Showing {pagination.number * pagination.size + 1}-
                    {Math.min((pagination.number + 1) * pagination.size, pagination.totalElements)} of{' '}
                    {pagination.totalElements} promotions
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
                        onPageChange={setPage}
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