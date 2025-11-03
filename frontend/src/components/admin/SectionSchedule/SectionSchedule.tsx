import React, { useState } from 'react';
import { useSessions, useSessionMutation, useSessionFilters, useNotification } from '@/hooks';
import { SessionFilters } from './SessionFilters';
import { SessionTable } from './SessionTable';
import { SessionModal } from './SessionModal';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal';
import { Pagination } from '@/components/ui/Pagination';
import type { SessionDto, SessionRequest } from '@/types/session';
import styles from './SectionSchedule.module.css';

export const SectionSchedule: React.FC = () => {
    const { showNotification } = useNotification();
    const [selectedSession, setSelectedSession] = useState<SessionDto | null>(null);
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [deleteModalOpen, setDeleteModalOpen] = useState(false);
    const [sessionToDelete, setSessionToDelete] = useState<SessionDto | null>(null);

    const [pagination, setPagination] = useState({
        page: 0,
        size: 20
    });

    const { filters, setDateFilter, setHallFilter, setMovieFilter, setUpcomingDaysFilter, clearFilters, hasActiveFilters } = useSessionFilters();
    const { sessions, loading, error, pagination: apiPagination, refetch } = useSessions(filters, pagination);
    const { createSession, updateSession, deleteSession, loading: mutationLoading } = useSessionMutation();

    const handleCreateSession = () => {
        setSelectedSession(null);
        setIsModalOpen(true);
    };

    const handleEditSession = (session: SessionDto) => {
        setSelectedSession(session);
        setIsModalOpen(true);
    };

    const handleDeleteSession = (session: SessionDto) => {
        setSessionToDelete(session);
        setDeleteModalOpen(true);
    };

    const handleConfirmDelete = async () => {
        if (!sessionToDelete) return;

        try {
            await deleteSession(sessionToDelete.id);
            showNotification('Session deleted successfully', 'success');
            refetch();
            setDeleteModalOpen(false);
            setSessionToDelete(null);
        } catch (error) {
            showNotification('Failed to delete session', 'error');
        }
    };

    const handleSaveSession = async (data: SessionRequest) => {
        try {
            if (selectedSession) {
                await updateSession(selectedSession.id, data);
                showNotification('Session updated successfully', 'success');
            } else {
                await createSession(data);
                showNotification('Session created successfully', 'success');
            }

            setIsModalOpen(false);
            setSelectedSession(null);
            refetch();
        } catch (error) { }
    };

    const handlePageChange = (page: number) => {
        setPagination(prev => ({ ...prev, page }));
    };

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <h1>Session Schedule</h1>
                <button
                    className={styles.createButton}
                    onClick={handleCreateSession}
                    disabled={mutationLoading}
                >
                    + Create Session
                </button>
            </div>

            <SessionFilters
                filters={filters}
                onDateChange={setDateFilter}
                onHallChange={setHallFilter}
                onMovieChange={setMovieFilter}
                onUpcomingDaysChange={setUpcomingDaysFilter}
                onClearFilters={clearFilters}
                hasActiveFilters={hasActiveFilters}
            />

            {error && (
                <div className={styles.error}>
                    {error}
                    <button onClick={refetch}>Retry</button>
                </div>
            )}

            <SessionTable
                sessions={sessions}
                loading={loading}
                onEdit={handleEditSession}
                onDelete={handleDeleteSession}
            />

            {apiPagination && apiPagination.totalPages > 1 && (
                <Pagination
                    currentPage={apiPagination.number}
                    totalPages={apiPagination.totalPages}
                    totalElements={apiPagination.totalElements}
                    pageSize={apiPagination.size}
                    onPageChange={handlePageChange}
                />
            )}

            <SessionModal
                isOpen={isModalOpen}
                session={selectedSession}
                onSave={handleSaveSession}
                onClose={() => {
                    setIsModalOpen(false);
                    setSelectedSession(null);
                }}
                loading={mutationLoading}
            />

            <DeleteConfirmModal
                isOpen={deleteModalOpen}
                onConfirm={handleConfirmDelete}
                onCancel={() => {
                    setDeleteModalOpen(false);
                    setSessionToDelete(null);
                }}
                itemName={sessionToDelete?.movie.title}
                itemType="session"
                isDeleting={mutationLoading}
            />
        </div>
    );
};