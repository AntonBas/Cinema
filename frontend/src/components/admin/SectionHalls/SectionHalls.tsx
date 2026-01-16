import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types';
import { useCinemaHalls, useCinemaHallMutation } from '@/hooks/features/cinemaHalls';
import { useNotification } from '@/hooks/common/useNotification';
import { DeleteConfirmModal, Notification, Button } from '@/components/ui';
import { CreateHallModal, EditHallModal } from './HallModal';
import { HallsTable } from './HallsTable/HallsTable';
import { HallLayoutModal } from './HallLayoutModal/HallLayoutModal';
import styles from './SectionHalls.module.css';

export const SectionHalls: React.FC = () => {
    const { allHalls: halls, loading, error, getAllHalls, getHallLayout } = useCinemaHalls();
    const {
        createHall,
        updateHall,
        deleteHall,
        loading: mutationLoading,
        error: mutationError
    } = useCinemaHallMutation();

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedHall, setSelectedHall] = useState<CinemaHallResponse | null>(null);
    const [showLayoutModal, setShowLayoutModal] = useState(false);
    const [hallLayouts, setHallLayouts] = useState<{ [key: number]: { rows: number, seatsPerRow: number } }>({});
    const [deleteModal, setDeleteModal] = useState<{
        isOpen: boolean;
        hall: CinemaHallResponse | null;
    }>({
        isOpen: false,
        hall: null
    });

    const { notifications, showNotification, hideNotification } = useNotification();

    useEffect(() => {
        getAllHalls();
    }, []);

    useEffect(() => {
        if (error) {
            showNotification(error, 'error');
        }
    }, [error, showNotification]);

    useEffect(() => {
        if (mutationError) {
            showNotification(mutationError, 'error');
        }
    }, [mutationError, showNotification]);

    const loadHallLayout = async (hallId: number) => {
        try {
            const layout = await getHallLayout(hallId);
            setHallLayouts(prev => ({
                ...prev,
                [hallId]: {
                    rows: layout.totalRows,
                    seatsPerRow: layout.maxSeatsPerRow
                }
            }));
        } catch (err) {
            console.error('Failed to load hall layout:', err);
        }
    };

    const handleCreateHall = async (request: CinemaHallRequest) => {
        try {
            await createHall(request);
            setShowCreateModal(false);
            showNotification('Cinema hall created successfully', 'success');
            await getAllHalls();
        } catch (err) {
            console.error('Failed to create hall:', err);
        }
    };

    const handleEditHall = async (id: number, request: CinemaHallRequest) => {
        try {
            await updateHall(id, request);
            setShowEditModal(false);
            setSelectedHall(null);
            showNotification('Cinema hall updated successfully', 'success');
            await getAllHalls();
        } catch (err) {
            console.error('Failed to update hall:', err);
        }
    };

    const handleDeleteHall = async (id: number) => {
        try {
            await deleteHall(id);
            showNotification('Cinema hall deleted successfully', 'success');
            await getAllHalls();
        } catch (err) {
            console.error('Failed to delete hall:', err);
        } finally {
            setDeleteModal({ isOpen: false, hall: null });
        }
    };

    const confirmDelete = (hall: CinemaHallResponse) => {
        setDeleteModal({
            isOpen: true,
            hall
        });
    };

    const handleEdit = async (hall: CinemaHallResponse) => {
        setSelectedHall(hall);
        await loadHallLayout(hall.id);
        setShowEditModal(true);
    };

    const handleShowLayout = (hall: CinemaHallResponse) => {
        setSelectedHall(hall);
        setShowLayoutModal(true);
    };

    return (
        <div className={styles.section}>
            {notifications.map((notification, index) => (
                <Notification
                    key={notification.id}
                    id={notification.id}
                    message={notification.message}
                    type={notification.type}
                    isVisible={notification.isVisible}
                    onClose={hideNotification}
                    duration={4000}
                    position={index}
                />
            ))}

            <div className={styles.header}>
                <div className={styles.headerContent}>
                    <h1>Cinema Halls Management</h1>
                    <p className={styles.subtitle}>
                        Manage your cinema halls, seating layouts and configurations
                    </p>
                </div>
                <Button
                    variant="primary"
                    onClick={() => setShowCreateModal(true)}
                    disabled={mutationLoading}
                    loading={mutationLoading}
                >
                    Add Hall
                </Button>
            </div>

            <div className={styles.content}>
                <HallsTable
                    halls={halls}
                    onDelete={confirmDelete}
                    onShowLayout={handleShowLayout}
                    onEdit={handleEdit}
                    loading={loading}
                />
            </div>

            {showCreateModal && (
                <CreateHallModal
                    onClose={() => setShowCreateModal(false)}
                    onCreate={handleCreateHall}
                    loading={mutationLoading}
                />
            )}

            {showEditModal && selectedHall && (
                <EditHallModal
                    hall={selectedHall}
                    currentLayout={hallLayouts[selectedHall.id]}
                    onClose={() => {
                        setShowEditModal(false);
                        setSelectedHall(null);
                    }}
                    onUpdate={handleEditHall}
                    loading={mutationLoading}
                />
            )}

            {showLayoutModal && selectedHall && (
                <HallLayoutModal
                    hall={selectedHall}
                    isOpen={showLayoutModal}
                    onClose={() => {
                        setShowLayoutModal(false);
                        setSelectedHall(null);
                    }}
                />
            )}

            <DeleteConfirmModal
                isOpen={deleteModal.isOpen}
                onConfirm={() => deleteModal.hall && handleDeleteHall(deleteModal.hall.id)}
                onCancel={() => setDeleteModal({ isOpen: false, hall: null })}
                itemName={deleteModal.hall?.name}
                itemType="cinema hall"
                isDeleting={mutationLoading}
            />
        </div>
    );
};