import React, { useState } from 'react';
import { Button, SearchInput, ConfirmModal } from '@/components/ui';
import { useAdminPromotionList } from '@/hooks/features/promotion/useAdminPromotionList';
import { useAdminPromotion } from '@/hooks/features/promotion/useAdminPromotion';
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

    const { promotions, loading, refresh } = useAdminPromotionList({ autoFetch: true });
    const { remove } = useAdminPromotion();

    const filteredPromotions = promotions.filter(promotion => {
        const matchesSearch = search === '' ||
            promotion.title.toLowerCase().includes(search.toLowerCase()) ||
            promotion.description?.toLowerCase().includes(search.toLowerCase());

        if (!statusFilter) return matchesSearch;
        return matchesSearch;
    });

    const handleDeleteConfirm = async () => {
        if (!deletingPromotion) return;

        try {
            await remove(deletingPromotion.id);
            setDeletingPromotion(null);
            refresh();
        } catch (error) {
        }
    };

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <div>
                    <h1 className={styles.title}>Promotion Management</h1>
                    <p className={styles.subtitle}>Create and manage promotions</p>
                </div>
                <Button onClick={() => setShowCreateModal(true)} variant="primary">Create Promotion</Button>
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
                    loading={loading}
                    onEdit={setEditingPromotion}
                    onDelete={(id, title) => setDeletingPromotion({ id, title })}
                />
            </div>

            {showCreateModal && (
                <CreatePromotionModal
                    onClose={() => setShowCreateModal(false)}
                    onSuccess={refresh}
                />
            )}

            {editingPromotion && (
                <EditPromotionModal
                    promotionId={editingPromotion}
                    onClose={() => setEditingPromotion(null)}
                    onSuccess={refresh}
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