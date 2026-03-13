import React, { useState, useEffect, useCallback, useMemo } from 'react';
import { Button } from '@/components/ui/Button/Button';
import { SearchInput } from '@/components/ui/SearchInput/SearchInput';
import { ConfirmModal } from '@/components/ui/ConfirmModal/ConfirmModal';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { Notification } from '@/components/ui/Notification/Notification';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import type { PromotionResponse, PromotionAdminResponse } from '@/types/promotion';
import PromotionTable from './PromotionTable/PromotionTable';
import PromotionFilters from './PromotionFilters/PromotionFilters';
import CreatePromotionModal from './PromotionModal/CreatePromotionModal';
import EditPromotionModal from './PromotionModal/EditPromotionModal';
import styles from './SectionPromotion.module.css';

const SectionPromotion: React.FC = () => {
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState<string>('');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingPromotion, setEditingPromotion] = useState<number | null>(null);
    const [deletingPromotion, setDeletingPromotion] = useState<{ id: number; title: string } | null>(null);
    const [isLoading, setIsLoading] = useState(false);
    const [promotionsList, setPromotionsList] = useState<PromotionAdminResponse[]>([]);

    const { notifications, showNotification, hideNotification } = useNotification();

    const {
        promotionsPage,
        getAll,
        remove
    } = usePromotion();

    const showDelayedLoading = useDelayedLoading(isLoading, { delay: 150, minDisplayTime: 300 });

    useEffect(() => {
        if (promotionsPage?.content) {
            setPromotionsList(promotionsPage.content);
        }
    }, [promotionsPage]);

    const getPromotionStatus = useCallback((promotion: PromotionAdminResponse): string => {
        const now = new Date();

        if (!promotion.startDate && !promotion.endDate) return 'active';

        if (promotion.startDate) {
            const startDate = new Date(promotion.startDate);
            if (now < startDate) return 'upcoming';
        }

        if (promotion.endDate) {
            const endDate = new Date(promotion.endDate);
            if (now > endDate) return 'expired';
        }

        return 'active';
    }, []);

    const getStatusDisplay = useCallback((status: string): string => {
        const displayMap: Record<string, string> = {
            'active': 'Active',
            'upcoming': 'Upcoming',
            'expired': 'Expired'
        };
        return displayMap[status] || status;
    }, []);

    useEffect(() => {
        loadPromotions();
    }, []);

    const filteredPromotions = useMemo(() => {
        return promotionsList.filter(promotion => {
            const matchesSearch = search === '' ||
                promotion.title.toLowerCase().includes(search.toLowerCase());

            if (!statusFilter) return matchesSearch;

            const status = getPromotionStatus(promotion);
            return matchesSearch && status === statusFilter;
        });
    }, [promotionsList, search, statusFilter, getPromotionStatus]);

    const loadPromotions = async () => {
        setIsLoading(true);
        try {
            await getAll({ page: 0, size: 100 });
        } catch (error) {
            showNotification('Failed to load promotions', 'error');
        } finally {
            setIsLoading(false);
        }
    };

    const handleCreateSuccess = useCallback((result?: PromotionResponse) => {
        setShowCreateModal(false);
        if (result) {
            showNotification(`Promotion "${result.title}" created successfully!`, 'success');
        }
        loadPromotions();
    }, [showNotification]);

    const handleUpdateSuccess = useCallback((result?: PromotionResponse) => {
        setEditingPromotion(null);
        if (result) {
            showNotification(`Promotion "${result.title}" updated successfully!`, 'success');
        }
        loadPromotions();
    }, [showNotification]);

    const handleDeleteConfirm = async () => {
        if (!deletingPromotion) return;

        try {
            await remove(deletingPromotion.id);
            showNotification(`Promotion "${deletingPromotion.title}" deleted successfully!`, 'success');
            setDeletingPromotion(null);
            await loadPromotions();
        } catch (error) {
            showNotification('Failed to delete promotion', 'error');
        }
    };

    if (showDelayedLoading && !promotionsList.length) {
        return (
            <div className={styles.loading}>
                <LoadingSpinner text="Loading promotions..." />
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
                <div>
                    <h1 className={styles.title}>Promotion Management</h1>
                    <p className={styles.subtitle}>Create and manage promotions</p>
                </div>
                <Button
                    onClick={() => setShowCreateModal(true)}
                    variant="primary"
                    disabled={isLoading}
                >
                    Create Promotion
                </Button>
            </div>

            <div className={styles.filtersContainer}>
                <div className={styles.searchWrapper}>
                    <SearchInput
                        onSearch={setSearch}
                        placeholder="Search promotions..."
                    />
                </div>
                <PromotionFilters
                    selectedStatus={statusFilter}
                    onStatusChange={setStatusFilter}
                />
            </div>

            <div className={styles.tableContainer}>
                <PromotionTable
                    promotions={filteredPromotions}
                    onEdit={setEditingPromotion}
                    onDelete={(id, title) => setDeletingPromotion({ id, title })}
                    getPromotionStatus={getPromotionStatus}
                    getStatusDisplay={getStatusDisplay}
                />
            </div>

            {showCreateModal && (
                <CreatePromotionModal
                    onClose={() => setShowCreateModal(false)}
                    onSuccess={handleCreateSuccess}
                />
            )}

            {editingPromotion && (
                <EditPromotionModal
                    promotionId={editingPromotion}
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