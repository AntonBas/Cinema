import React, { useState, useEffect } from 'react';
import type { CinemaHallDto, CinemaHallRequest } from '@/types';
import { cinemaHallApi } from '@/api/cinemaHallApi';
import { useNotification } from '@/hooks/useNotification';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import { HallsTable } from './HallsTable/HallsTable';
import { CreateHallModal } from './CreateHallModal/CreateHallModal';
import { EditHallModal } from './EditHallModal/EditHallModal';
import { HallLayoutModal } from './HallLayoutModal/HallLayoutModal';
import styles from './SectionHalls.module.css';

export const SectionHalls: React.FC = () => {
    const [halls, setHalls] = useState<CinemaHallDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [showCreateModal, setShowCreateModal] = useState(false);
    const [showEditModal, setShowEditModal] = useState(false);
    const [selectedHall, setSelectedHall] = useState<CinemaHallDto | null>(null);
    const [showLayoutModal, setShowLayoutModal] = useState(false);
    const [deleteModal, setDeleteModal] = useState<{
        isOpen: boolean;
        hall: CinemaHallDto | null;
        isDeleting: boolean;
    }>({
        isOpen: false,
        hall: null,
        isDeleting: false
    });

    const { showNotification } = useNotification();

    const loadHalls = async () => {
        try {
            setLoading(true);
            const data = await cinemaHallApi.getAllHalls();
            setHalls(data);
        } catch (err: any) {
            showNotification(err.message || 'Failed to load cinema halls', 'error');
            console.error('Error loading halls:', err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadHalls();
    }, []);

    const handleCreateHall = async (name: string) => {
        try {
            await cinemaHallApi.createHall({ name });
            await loadHalls();
            setShowCreateModal(false);
            showNotification('Cinema hall created successfully', 'success');
        } catch (err: any) {
            showNotification(err.message || 'Failed to create cinema hall', 'error');
        }
    };

    const handleEditHall = async (id: number, request: CinemaHallRequest) => {
        try {
            await cinemaHallApi.updateHall(id, request);
            await loadHalls();
            setShowEditModal(false);
            setSelectedHall(null);
            showNotification('Cinema hall updated successfully', 'success');
        } catch (err: any) {
            showNotification(err.message || 'Failed to update cinema hall', 'error');
        }
    };

    const handleDeleteHall = async (id: number) => {
        try {
            setDeleteModal(prev => ({ ...prev, isDeleting: true }));
            await cinemaHallApi.deleteHall(id);
            await loadHalls();
            showNotification('Cinema hall deleted successfully', 'success');
        } catch (err: any) {
            showNotification(err.message || 'Failed to delete cinema hall', 'error');
        } finally {
            setDeleteModal({ isOpen: false, hall: null, isDeleting: false });
        }
    };

    const confirmDelete = (hall: CinemaHallDto) => {
        setDeleteModal({
            isOpen: true,
            hall,
            isDeleting: false
        });
    };

    const handleEdit = (hall: CinemaHallDto) => {
        setSelectedHall(hall);
        setShowEditModal(true);
    };

    const handleShowLayout = (hall: CinemaHallDto) => {
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
            <div className={styles.header}>
                <h1>Cinema Halls Management</h1>
                <button
                    className={styles.createButton}
                    onClick={() => setShowCreateModal(true)}
                >
                    + Create New Hall
                </button>
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
                        loadHalls();
                    }}
                />
            )}

            <DeleteConfirmModal
                isOpen={deleteModal.isOpen}
                onConfirm={() => deleteModal.hall && handleDeleteHall(deleteModal.hall.id!)}
                onCancel={() => setDeleteModal({ isOpen: false, hall: null, isDeleting: false })}
                itemName={deleteModal.hall?.name}
                itemType="cinema hall"
                isDeleting={deleteModal.isDeleting}
            />
        </div>
    );
};