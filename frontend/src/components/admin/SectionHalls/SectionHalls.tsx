import React, { useState, useEffect } from 'react';
import type { CinemaHallResponse, CinemaHallRequest } from '@/types/cinemaHall';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { DeleteConfirmModal, Notification, Button } from '@/components/ui';
import { CreateHallModal } from './HallModal/CreateHallModal';
import { EditHallModal } from './HallModal/EditHallModal';
import { HallsTable } from './HallsTable/HallsTable';
import { HallLayoutModal } from './HallLayoutModal/HallLayoutModal';
import styles from './SectionHalls.module.css';

export const SectionHalls: React.FC = () => {
    const {
        allHalls: halls,
        loading,
        getAllHalls,
        createHall,
        updateHall,
        deleteHall
    } = useCinemaHalls();

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedHall, setSelectedHall] = useState<CinemaHallResponse | null>(null);
    const [showLayoutModal, setShowLayoutModal] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');
    const [errorMessage, setErrorMessage] = useState('');
    const [deleteModal, setDeleteModal] = useState<{
        isOpen: boolean;
        hall: CinemaHallResponse | null;
    }>({
        isOpen: false,
        hall: null
    });

    useEffect(() => {
        getAllHalls();
    }, []);

    const handleCreateHall = async (request: CinemaHallRequest) => {
        try {
            await createHall(request);
            setShowCreateModal(false);
            setSuccessMessage('Cinema hall created successfully');
            getAllHalls();
        } catch (err) {
            setErrorMessage('Failed to create hall');
        }
    };

    const handleEditHall = async (id: number, request: CinemaHallRequest) => {
        try {
            await updateHall(id, request);
            setShowEditModal(false);
            setSelectedHall(null);
            setSuccessMessage('Cinema hall updated successfully');
            getAllHalls();
        } catch (err) {
            setErrorMessage('Failed to update hall');
        }
    };

    const handleDeleteHall = async (id: number) => {
        try {
            await deleteHall(id);
            setSuccessMessage('Cinema hall deleted successfully');
            getAllHalls();
        } catch (err) {
            setErrorMessage('Failed to delete hall');
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

    const handleCloseNotification = () => {
        setErrorMessage('');
        setSuccessMessage('');
    };

    return (
        <div className={styles.section}>
            {successMessage && (
                <Notification
                    id="success"
                    message={successMessage}
                    type="success"
                    isVisible={true}
                    onClose={handleCloseNotification}
                    duration={4000}
                />
            )}

            {errorMessage && (
                <Notification
                    id="error"
                    message={errorMessage}
                    type="error"
                    isVisible={true}
                    onClose={handleCloseNotification}
                    duration={4000}
                />
            )}

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
                    loading={loading}
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
                isDeleting={loading}
            />
        </div>
    );
};