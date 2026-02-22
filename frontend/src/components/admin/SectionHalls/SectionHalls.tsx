import React, { useState, useEffect, useCallback } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types/cinemaHall';
import { SeatType } from '@/types/seat';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { DeleteConfirmModal, Button, Notification, LoadingSpinner } from '@/components/ui';
import { CreateHallModal } from './HallModal/CreateHallModal';
import { EditHallModal } from './HallModal/EditHallModal';
import { HallsTable } from './HallsTable/HallsTable';
import { HallLayoutModal } from './HallLayoutModal/HallLayoutModal';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './SectionHalls.module.css';

export const SectionHalls: React.FC = () => {
    const {
        allHalls: halls,
        loading,
        getAllHalls,
        createHall,
        updateHall,
        deleteHall,
        getHallLayout
    } = useCinemaHalls();

    const { notifications, showNotification, hideNotification } = useNotification();
    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedHall, setSelectedHall] = useState<CinemaHallResponse | null>(null);
    const [currentLayout, setCurrentLayout] = useState<{ rows: number; seatsPerRow: number; coupleRows?: number[] } | undefined>();
    const [showLayoutModal, setShowLayoutModal] = useState(false);
    const [deleteModal, setDeleteModal] = useState<{
        isOpen: boolean;
        hall: CinemaHallResponse | null;
        isDeleting: boolean;
    }>({
        isOpen: false,
        hall: null,
        isDeleting: false
    });

    useEffect(() => {
        getAllHalls();
    }, []);

    const loadHalls = useCallback(async () => {
        await getAllHalls();
    }, [getAllHalls]);

    const handleCreateHall = useCallback(async (request: CinemaHallRequest) => {
        try {
            const result = await createHall(request);
            if (result) {
                showNotification(`Cinema hall "${result.name}" created successfully!`, 'success');
                await loadHalls();
            }
            setShowCreateModal(false);
        } catch (err) {
            if (isApiErrorException(err)) {
                if (err.isConflict()) {
                    showNotification(`Hall with name "${request.name}" already exists`, 'error');
                } else {
                    showNotification(err.message, 'error');
                }
            } else {
                showNotification('Failed to create hall', 'error');
            }
        }
    }, [createHall, loadHalls, showNotification]);

    const handleEditHall = useCallback(async (id: number, request: CinemaHallRequest & { coupleRows?: number[] }) => {
        try {
            const result = await updateHall(id, request);
            if (result) {
                showNotification(`Cinema hall "${result.name}" updated successfully!`, 'success');
                await loadHalls();
            }
            setShowEditModal(false);
            setSelectedHall(null);
            setCurrentLayout(undefined);
        } catch (err) {
            if (isApiErrorException(err)) {
                if (err.isConflict()) {
                    showNotification(`Hall with name "${request.name}" already exists`, 'error');
                } else {
                    showNotification(err.message, 'error');
                }
            } else {
                showNotification('Failed to update hall', 'error');
            }
        }
    }, [updateHall, loadHalls, showNotification]);

    const handleDeleteHall = useCallback(async () => {
        if (!deleteModal.hall) return;

        setDeleteModal(prev => ({ ...prev, isDeleting: true }));

        try {
            await deleteHall(deleteModal.hall.id);
            showNotification(`Cinema hall "${deleteModal.hall.name}" deleted successfully!`, 'success');
            await loadHalls();
            setDeleteModal({ isOpen: false, hall: null, isDeleting: false });
        } catch (err) {
            if (isApiErrorException(err)) {
                if (err.isConflict()) {
                    showNotification('Cannot delete hall because it has associated sessions', 'error');
                } else {
                    showNotification(err.message, 'error');
                }
            } else {
                showNotification('Failed to delete hall', 'error');
            }
            setDeleteModal(prev => ({ ...prev, isDeleting: false }));
        }
    }, [deleteHall, deleteModal.hall, loadHalls, showNotification]);

    const confirmDelete = useCallback((hall: CinemaHallResponse) => {
        setDeleteModal({
            isOpen: true,
            hall,
            isDeleting: false
        });
    }, []);

    const handleEdit = useCallback(async (hall: CinemaHallResponse) => {
        setSelectedHall(hall);
        const layout = await getHallLayout(hall.id);
        if (layout) {
            const coupleRows = layout.rows
                .filter(row => row.seats.every(seat => seat.seatType === SeatType.COUPLE))
                .map(row => row.rowNumber);
            setCurrentLayout({
                rows: layout.totalRows,
                seatsPerRow: layout.maxSeatsPerRow,
                coupleRows
            });
        }
        setShowEditModal(true);
    }, [getHallLayout]);

    const handleShowLayout = useCallback((hall: CinemaHallResponse) => {
        setSelectedHall(hall);
        setShowLayoutModal(true);
    }, []);

    const handleCloseDelete = useCallback(() => {
        setDeleteModal({ isOpen: false, hall: null, isDeleting: false });
    }, []);

    if (showDelayedLoading && !halls.length) {
        return (
            <div className={styles.loading}>
                <LoadingSpinner text="Loading cinema halls..." />
            </div>
        );
    }

    return (
        <div className={styles.section}>
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
                <div className={styles.headerContent}>
                    <h1 className={styles.title}>Cinema Halls Management</h1>
                    <p className={styles.subtitle}>
                        Manage your cinema halls, seating layouts and configurations
                    </p>
                </div>
                <Button
                    variant="primary"
                    onClick={() => setShowCreateModal(true)}
                    disabled={loading}
                    loading={loading}
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
                    currentLayout={currentLayout}
                    onClose={() => {
                        setShowEditModal(false);
                        setSelectedHall(null);
                        setCurrentLayout(undefined);
                    }}
                    onUpdate={handleEditHall}
                    loading={loading}
                />
            )}

            {showLayoutModal && selectedHall && (
                <HallLayoutModal
                    key={selectedHall.id}
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
                onConfirm={handleDeleteHall}
                onCancel={handleCloseDelete}
                itemName={deleteModal.hall?.name}
                itemType="cinema hall"
                isDeleting={deleteModal.isDeleting}
            />
        </div>
    );
};