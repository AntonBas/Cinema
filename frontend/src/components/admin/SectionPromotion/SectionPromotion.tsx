import React, { useState, useEffect } from 'react';
import { Button, SearchInput, ConfirmModal } from '@/components/ui';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import type { PromotionResponse } from '@/types/promotion';
import PromotionStats from './PromotionStats';
import PromotionTable from './PromotionTable';
import PromotionFilters from './PromotionFilters';
import { CreatePromotionModal, EditPromotionModal } from './PromotionModal';
import styles from './SectionPromotion.module.css';

const SectionPromotion: React.FC = () => {
    const [search, setSearch] = useState('');
    const [statusFilter, setStatusFilter] = useState<string>('');
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingPromotion, setEditingPromotion] = useState<number | null>(null);
    const [deletingPromotion, setDeletingPromotion] = useState<{ id: number; title: string } | null>(null);
    const [filteredPromotions, setFilteredPromotions] = useState<PromotionResponse[]>([]);
    const [isLoading, setIsLoading] = useState(false);

    const {
        adminPromotions: promotions,
        getPromotionStatus,
        getStatusDisplay,
        fetchAdminPromotions,
        remove
    } = usePromotion();

    useEffect(() => {
        loadPromotions();
    }, []);

    useEffect(() => {
        filterPromotions();
    }, [search, statusFilter, promotions]);

    const loadPromotions = async () => {
        setIsLoading(true);
        try {
            await fetchAdminPromotions();
        } catch (error) {
            console.error('Failed to load promotions');
        } finally {
            setIsLoading(false);
        }
    };

    const filterPromotions = () => {
        const filtered = promotions.filter(promotion => {
            const matchesSearch = search === '' ||
                promotion.title.toLowerCase().includes(search.toLowerCase()) ||
                promotion.description?.toLowerCase().includes(search.toLowerCase());

            if (!statusFilter) return matchesSearch;

            const status = getPromotionStatus(promotion);
            return matchesSearch && status === statusFilter;
        });
        setFilteredPromotions(filtered);
    };

    const handleDeleteConfirm = async () => {
        if (!deletingPromotion) return;

        try {
            await remove(deletingPromotion.id);
            setDeletingPromotion(null);
            await fetchAdminPromotions();
        } catch (error) {
            console.error('Failed to delete promotion');
        }
    };

    const handleRefresh = () => {
        fetchAdminPromotions();
    };

    return (
        <div className={styles.container}>
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

            <PromotionStats />

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
                    loading={isLoading}
                    onEdit={setEditingPromotion}
                    onDelete={(id, title) => setDeletingPromotion({ id, title })}
                    getPromotionStatus={getPromotionStatus}
                    getStatusDisplay={getStatusDisplay}
                />
            </div>

            {showCreateModal && (
                <CreatePromotionModal
                    onClose={() => setShowCreateModal(false)}
                    onSuccess={handleRefresh}
                />
            )}

            {editingPromotion && (
                <EditPromotionModal
                    promotionId={editingPromotion}
                    onClose={() => setEditingPromotion(null)}
                    onSuccess={handleRefresh}
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