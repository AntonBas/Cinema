import React, { useState, useEffect, useCallback } from 'react';
import type { CinemaHallListResponse, CinemaHallResponse, CinemaHallRequest } from '@/types/cinemaHall';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal/DeleteConfirmModal';
import { Button } from '@/components/ui/Button/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import { CreateHallModal } from './HallModal/CreateHallModal';
import { EditHallModal } from './HallModal/EditHallModal';
import { HallsTable } from './HallsTable/HallsTable';
import { HallLayoutModal } from './HallLayoutModal/HallLayoutModal';
import { HallLayoutProvider, useHallLayout } from './HallLayoutContext';
import styles from './SectionHalls.module.css';

const SectionHallsContent: React.FC = () => {
    const { loading, getAllHalls, getHallById, createHall, updateHall, deleteHall } = useCinemaHalls();
    const { openLayout } = useHallLayout();

    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedHall, setSelectedHall] = useState<CinemaHallResponse | null>(null);
    const [deleteModal, setDeleteModal] = useState({
        isOpen: false,
        hall: null as CinemaHallListResponse | null,
        isDeleting: false
    });
    const [hallsData, setHallsData] = useState<CinemaHallListResponse[]>([]);

    const loadHalls = useCallback(async () => {
        const response = await getAllHalls();
        setHallsData(response || []);
    }, []);

    useEffect(() => {
        loadHalls();
    }, []);

    const handleCreateHall = useCallback(async (request: CinemaHallRequest) => {
        const response = await createHall(request);
        if (response) {
            await loadHalls();
            setShowCreateModal(false);
        }
    }, []);

    const handleEditHall = useCallback(async (id: number, request: CinemaHallRequest) => {
        const response = await updateHall(id, request);
        if (response) {
            await loadHalls();
            setShowEditModal(false);
            setSelectedHall(null);
        }
    }, []);

    const handleDeleteHall = useCallback(async () => {
        if (!deleteModal.hall) return;

        setDeleteModal(prev => ({ ...prev, isDeleting: true }));

        try {
            await deleteHall(deleteModal.hall.id);
            await loadHalls();
            setDeleteModal({ isOpen: false, hall: null, isDeleting: false });
        } catch {
            setDeleteModal(prev => ({ ...prev, isDeleting: false }));
        }
    }, [deleteModal.hall]);

    const confirmDelete = useCallback((hall: CinemaHallListResponse) => {
        setDeleteModal({ isOpen: true, hall, isDeleting: false });
    }, []);

    const handleEdit = useCallback(async (hall: CinemaHallListResponse) => {
        const response = await getHallById(hall.id);
        if (response) {
            setSelectedHall(response);
            setShowEditModal(true);
        }
    }, []);

    const handleShowLayout = useCallback(async (hall: CinemaHallListResponse) => {
        const response = await getHallById(hall.id);
        if (response) {
            openLayout(response);
        }
    }, []);

    if (showDelayedLoading && !hallsData.length) {
        return (
            <div className={styles.loading}>
                <LoadingSpinner text="Loading cinema halls..." />
            </div>
        );
    }

    return (
        <div className={styles.section}>
            <div className={styles.header}>
                <div className={styles.headerContent}>
                    <h1 className={styles.title}>Cinema Halls Management</h1>
                    <p className={styles.subtitle}>
                        Manage your cinema halls, seating layouts and configurations
                    </p>
                </div>
                <Button variant="primary" onClick={() => setShowCreateModal(true)} disabled={loading}>
                    Add Hall
                </Button>
            </div>

            <div className={styles.content}>
                <HallsTable
                    halls={hallsData}
                    onDelete={confirmDelete}
                    onShowLayout={handleShowLayout}
                    onEdit={handleEdit}
                />
            </div>

            {showCreateModal && (
                <CreateHallModal
                    onClose={() => setShowCreateModal(false)}
                    onCreate={handleCreateHall}
                    loading={loading}
                />
            )}

            {showEditModal && selectedHall && (
                <EditHallModal
                    hall={selectedHall}
                    onClose={() => {
                        setShowEditModal(false);
                        setSelectedHall(null);
                    }}
                    onUpdate={handleEditHall}
                    loading={loading}
                />
            )}

            <HallLayoutModal />

            <DeleteConfirmModal
                isOpen={deleteModal.isOpen}
                onConfirm={handleDeleteHall}
                onCancel={() => setDeleteModal({ isOpen: false, hall: null, isDeleting: false })}
                itemName={deleteModal.hall?.name}
                itemType="cinema hall"
                isDeleting={deleteModal.isDeleting}
            />
        </div>
    );
};

export const SectionHalls: React.FC = () => {
    return (
        <HallLayoutProvider>
            <SectionHallsContent />
        </HallLayoutProvider>
    );
};