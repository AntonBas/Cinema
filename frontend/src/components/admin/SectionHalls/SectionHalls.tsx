import React, { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import type { CinemaHallListResponse, CinemaHallResponse, CinemaHallRequest } from '@/types/cinemaHall';
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
import { HallLayoutProvider, useHallLayout } from './HallLayoutContext';
import { isApiErrorException } from '@/utils/apiErrorHandler';
import styles from './SectionHalls.module.css';

const SectionHallsContent: React.FC = () => {
    const {
        loading,
        getAllHalls,
        getHallById,
        createHall,
        updateHall,
        deleteHall } = useCinemaHalls();

    const { openLayout } = useHallLayout();

    const { notifications, showNotification, hideNotification } = useNotification();
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

    const hasLoadedRef = useRef(false);
    const isMountedRef = useRef(true);

    useEffect(() => {
        isMountedRef.current = true;
        return () => {
            isMountedRef.current = false;
        };
    }, []);

    const loadHalls = useCallback(async () => {
        if (!isMountedRef.current) return;

        try {
            const response = await getAllHalls();
            if (isMountedRef.current) {
                setHallsData(response || []);
            }
        } catch (error) {
            console.error('Failed to load halls:', error);
        }
    }, [getAllHalls]);

    useEffect(() => {
        if (!hasLoadedRef.current) {
            hasLoadedRef.current = true;
            loadHalls();
        }
    }, [loadHalls]);

    const handleCreateHall = useCallback(async (request: CinemaHallRequest) => {
        try {
            const response = await createHall(request);
            if (response && isMountedRef.current) {
                showNotification(`Cinema hall "${response.name}" created successfully!`, 'success');
                await loadHalls();
            }
            setShowCreateModal(false);
        } catch (err) {
            if (isApiErrorException(err)) {
                showNotification(err.message, 'error');
            } else {
                showNotification('Failed to create hall', 'error');
            }
        }
    }, [createHall, showNotification, loadHalls]);

    const handleEditHall = useCallback(async (id: number, request: CinemaHallRequest) => {
        try {
            const response = await updateHall(id, request);
            if (response && isMountedRef.current) {
                showNotification(`Cinema hall "${response.name}" updated successfully!`, 'success');
                await loadHalls();
            }
            setShowEditModal(false);
            setSelectedHall(null);
        } catch (err) {
            if (isApiErrorException(err)) {
                showNotification(err.message, 'error');
            } else {
                showNotification('Failed to update hall', 'error');
            }
        }
    }, [updateHall, showNotification, loadHalls]);

    const handleDeleteHall = useCallback(async () => {
        if (!deleteModal.hall) return;

        setDeleteModal(prev => ({ ...prev, isDeleting: true }));

        try {
            await deleteHall(deleteModal.hall.id, deleteModal.hall.name);
            if (isMountedRef.current) {
                showNotification(`Cinema hall "${deleteModal.hall.name}" deleted successfully!`, 'success');
                await loadHalls();
            }
            setDeleteModal({ isOpen: false, hall: null, isDeleting: false });
        } catch (err) {
            if (isApiErrorException(err)) {
                showNotification(err.message, 'error');
            } else {
                showNotification('Failed to delete hall', 'error');
            }
            setDeleteModal(prev => ({ ...prev, isDeleting: false }));
        }
    }, [deleteHall, deleteModal.hall, showNotification, loadHalls]);

    const confirmDelete = useCallback((hall: CinemaHallListResponse) => {
        setDeleteModal({ isOpen: true, hall, isDeleting: false });
    }, []);

    const handleEdit = useCallback(async (hall: CinemaHallListResponse) => {
        const response = await getHallById(hall.id);
        if (response) {
            setSelectedHall(response);
            setShowEditModal(true);
        }
    }, [getHallById]);

    const handleShowLayout = useCallback((hall: CinemaHallListResponse) => {
        getHallById(hall.id).then(response => {
            if (response) {
                openLayout(response);
            }
        });
    }, [getHallById, openLayout]);

    const handleCloseCreate = useCallback(() => {
        setShowCreateModal(false);
    }, []);

    const handleCloseEdit = useCallback(() => {
        setShowEditModal(false);
        setSelectedHall(null);
    }, []);

    const handleCloseDelete = useCallback(() => {
        setDeleteModal({ isOpen: false, hall: null, isDeleting: false });
    }, []);

    const safeHalls = useMemo(() => Array.isArray(hallsData) ? hallsData : [], [hallsData]);

    if (showDelayedLoading && !safeHalls.length) {
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
                    halls={safeHalls}
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
                    onClose={handleCloseEdit}
                    onUpdate={handleEditHall}
                    loading={loading}
                />
            )}

            <HallLayoutModal />

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

export const SectionHalls: React.FC = () => {
    return (
        <HallLayoutProvider>
            <SectionHallsContent />
        </HallLayoutProvider>
    );
};