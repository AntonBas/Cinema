import React, { useState, useEffect, useCallback, useRef } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types/cinemaHall';
import { SeatType } from '@/types/seat';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal/DeleteConfirmModal';
import { Button } from '@/components/ui/Button/Button';
import { Notification } from '@/components/ui/Notification/Notification';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
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
    const [deleteModal, setDeleteModal] = useState({
        isOpen: false,
        hall: null as CinemaHallResponse | null,
        isDeleting: false
    });

    const hasLoadedRef = useRef(false);

    useEffect(() => {
        if (!hasLoadedRef.current) {
            getAllHalls();
            hasLoadedRef.current = true;
        }
    }, [getAllHalls]);

    const handleApiError = useCallback((err: unknown, defaultMessage: string) => {
        if (isApiErrorException(err)) {
            showNotification(err.message, 'error');
        } else {
            showNotification(defaultMessage, 'error');
        }
    }, [showNotification]);

    const handleCreateHall = useCallback(async (request: CinemaHallRequest) => {
        try {
            const response = await createHall(request);
            if (response) {
                showNotification(`Cinema hall "${response.name}" created successfully!`, 'success');
            }
            setShowCreateModal(false);
        } catch (err) {
            if (isApiErrorException(err) && err.isConflict()) {
                showNotification(`Hall with name "${request.name}" already exists`, 'error');
            } else {
                handleApiError(err, 'Failed to create hall');
            }
        }
    }, [createHall, showNotification, handleApiError]);

    const handleEditHall = useCallback(async (id: number, request: CinemaHallRequest & { coupleRows?: number[] }) => {
        try {
            const response = await updateHall(id, request);
            if (response) {
                showNotification(`Cinema hall "${response.name}" updated successfully!`, 'success');
            }
            setShowEditModal(false);
            setSelectedHall(null);
            setCurrentLayout(undefined);
        } catch (err) {
            if (isApiErrorException(err) && err.isConflict()) {
                showNotification(`Hall with name "${request.name}" already exists`, 'error');
            } else {
                handleApiError(err, 'Failed to update hall');
            }
        }
    }, [updateHall, showNotification, handleApiError]);

    const handleDeleteHall = useCallback(async () => {
        if (!deleteModal.hall) return;

        setDeleteModal(prev => ({ ...prev, isDeleting: true }));

        try {
            await deleteHall(deleteModal.hall.id);
            showNotification(`Cinema hall "${deleteModal.hall.name}" deleted successfully!`, 'success');
            setDeleteModal({ isOpen: false, hall: null, isDeleting: false });
        } catch (err) {
            if (isApiErrorException(err) && err.isConflict()) {
                showNotification('Cannot delete hall because it has associated sessions', 'error');
            } else {
                handleApiError(err, 'Failed to delete hall');
            }
            setDeleteModal(prev => ({ ...prev, isDeleting: false }));
        }
    }, [deleteHall, deleteModal.hall, showNotification, handleApiError]);

    const confirmDelete = useCallback((hall: CinemaHallResponse) => {
        setDeleteModal({ isOpen: true, hall, isDeleting: false });
    }, []);

    const handleEdit = useCallback(async (hall: CinemaHallResponse) => {
        setSelectedHall(hall);
        const response = await getHallLayout(hall.id);
        if (response) {
            const coupleRows = response.rows
                .filter(row => row.seats.every(seat => seat.seatType === SeatType.COUPLE))
                .map(row => row.rowNumber);
            setCurrentLayout({
                rows: response.totalRows,
                seatsPerRow: response.maxSeatsPerRow,
                coupleRows
            });
        }
        setShowEditModal(true);
    }, [getHallLayout]);

    const handleShowLayout = useCallback((hall: CinemaHallResponse) => {
        setSelectedHall(hall);
        setShowLayoutModal(true);
    }, []);

    const handleCloseCreate = useCallback(() => {
        setShowCreateModal(false);
    }, []);

    const handleCloseEdit = useCallback(() => {
        setShowEditModal(false);
        setSelectedHall(null);
        setCurrentLayout(undefined);
    }, []);

    const handleCloseLayout = useCallback(() => {
        setShowLayoutModal(false);
        setSelectedHall(null);
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
                    onClose={handleCloseCreate}
                    onCreate={handleCreateHall}
                    loading={loading}
                />
            )}

            {showEditModal && selectedHall && (
                <EditHallModal
                    hall={selectedHall}
                    currentLayout={currentLayout}
                    onClose={handleCloseEdit}
                    onUpdate={handleEditHall}
                    loading={loading}
                />
            )}

            {showLayoutModal && selectedHall && (
                <HallLayoutModal
                    key={selectedHall.id}
                    hall={selectedHall}
                    isOpen={showLayoutModal}
                    onClose={handleCloseLayout}
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