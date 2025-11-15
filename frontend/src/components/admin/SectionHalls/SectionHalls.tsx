import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types';
import { useHalls, useCinemaHallMutation } from '@/hooks/features/cinemaHalls';
import { useNotification } from '@/hooks/common/useNotification';
import { DeleteConfirmModal, Notification, Button } from '@/components/ui';
import { HallsTable } from './HallsTable/HallsTable';
import { CreateHallModal } from './CreateHallModal/CreateHallModal';
import { EditHallModal } from './EditHallModal/EditHallModal';
import { HallLayoutModal } from './HallLayoutModal/HallLayoutModal';
import styles from './SectionHalls.module.css';

export const SectionHalls: React.FC = () => {
    const { halls, loading, error, refetch } = useHalls();
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
    const [deleteModal, setDeleteModal] = useState<{
        isOpen: boolean;
        hall: CinemaHallResponse | null;
    }>({
        isOpen: false,
        hall: null
    });

    const { notifications, showNotification, hideNotification } = useNotification();

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

    const handleCreateHall = async (name: string) => {
        try {
            await createHall({ name });
            setShowCreateModal(false);
            showNotification('Cinema hall created successfully', 'success');
            await refetch();
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
            await refetch();
        } catch (err) {
            console.error('Failed to update hall:', err);
        }
    };

    const handleDeleteHall = async (id: number) => {
        try {
            await deleteHall(id);
            showNotification('Cinema hall deleted successfully', 'success');
            await refetch();
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

    const handleEdit = (hall: CinemaHallResponse) => {
        setSelectedHall(hall);
        setShowEditModal(true);
    };

    const handleShowLayout = (hall: CinemaHallResponse) => {
        setSelectedHall(hall);
        setShowLayoutModal(true);
    };

    if (loading) {
        return (
            <div className={styles.loading}>
                Loading cinema halls...
            </div>
        );
    }

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
                <h1>Cinema Halls Management</h1>
                <Button
                    variant="primary"
                    onClick={() => setShowCreateModal(true)}
                    disabled={mutationLoading}
                >
                    {mutationLoading ? 'Creating...' : 'Add New Hall'}
                </Button>
            </div>

            <HallsTable
                halls={halls}
                onDelete={confirmDelete}
                onShowLayout={handleShowLayout}
                onEdit={handleEdit}
            />

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
                    onClose={() => {
                        setShowLayoutModal(false);
                        setSelectedHall(null);
                    }}
                    onSeatsGenerated={() => {
                        showNotification('Seats generated successfully', 'success');
                        refetch();
                    }}
                />
            )}

            <DeleteConfirmModal
                isOpen={deleteModal.isOpen}
                onConfirm={() => deleteModal.hall && handleDeleteHall(deleteModal.hall.id!)}
                onCancel={() => setDeleteModal({ isOpen: false, hall: null })}
                itemName={deleteModal.hall?.name}
                itemType="cinema hall"
                isDeleting={mutationLoading}
            />
        </div>
    );
};