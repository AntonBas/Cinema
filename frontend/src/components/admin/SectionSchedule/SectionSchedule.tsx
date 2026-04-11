import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { useSession } from '@/hooks/features/sessions/useSession';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import { SessionFilters } from './SessionFilters/SessionFilters';
import { SessionTable } from './SessionTable/SessionTable';
import { CreateSessionModal } from './SessionModal/CreateSessionModal';
import { EditSessionModal } from './SessionModal/EditSessionModal';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal/DeleteConfirmModal';
import { ConfirmModal } from '@/components/ui/ConfirmModal/ConfirmModal';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import { Button } from '@/components/ui/Button/Button';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import type { SessionAdminResponse, SessionRequest, CinemaSessionStatus } from '@/types/session';
import type { CinemaHallListResponse } from '@/types/cinemaHall';
import styles from './SectionSchedule.module.css';

interface FiltersState {
    dateFrom?: string;
    dateTo?: string;
    hallId?: number;
    movieTitle?: string;
    status?: CinemaSessionStatus;
}

export const SectionSchedule: React.FC = () => {
    const { halls, getAllHalls } = useCinemaHalls();
    const { params, setPage } = usePagination({ size: 10 });

    const [filters, setFilters] = useState<FiltersState>({});
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [editingSession, setEditingSession] = useState<SessionAdminResponse | null>(null);
    const [deletingSession, setDeletingSession] = useState<SessionAdminResponse | null>(null);
    const [cancellingSession, setCancellingSession] = useState<SessionAdminResponse | null>(null);
    const [reactivatingSession, setReactivatingSession] = useState<SessionAdminResponse | null>(null);

    const {
        adminSessions,
        pagination,
        loading,
        getAdminSessions,
        create,
        update,
        remove,
        cancel,
        reactivate,
    } = useSession();

    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    useEffect(() => {
        getAllHalls();
    }, [getAllHalls]);

    useEffect(() => {
        getAdminSessions({
            page: params.page ?? 0,
            size: params.size,
            ...filters,
        });
    }, [params.page, params.size, filters, getAdminSessions]);

    const handleFilterChange = useCallback(<K extends keyof FiltersState>(key: K, value: FiltersState[K]) => {
        setFilters(prev => ({ ...prev, [key]: value }));
        setPage(0);
    }, [setPage]);

    const handleClearFilters = useCallback(() => {
        setFilters({});
        setPage(0);
    }, [setPage]);

    const handleCreateSession = useCallback(async (data: SessionRequest) => {
        await create(data);
        setIsCreateModalOpen(false);
        getAdminSessions({ page: params.page ?? 0, size: params.size, ...filters });
    }, [create, getAdminSessions, params.page, params.size, filters]);

    const handleUpdateSession = useCallback(async (id: number, data: SessionRequest) => {
        await update(id, data);
        setEditingSession(null);
        getAdminSessions({ page: params.page ?? 0, size: params.size, ...filters });
    }, [update, getAdminSessions, params.page, params.size, filters]);

    const handleDeleteSession = useCallback(async () => {
        if (!deletingSession) return;
        await remove(deletingSession.id);
        setDeletingSession(null);
        getAdminSessions({ page: params.page ?? 0, size: params.size, ...filters });
    }, [deletingSession, remove, getAdminSessions, params.page, params.size, filters]);

    const handleCancelSession = useCallback(async () => {
        if (!cancellingSession) return;
        await cancel(cancellingSession.id);
        setCancellingSession(null);
        getAdminSessions({ page: params.page ?? 0, size: params.size, ...filters });
    }, [cancellingSession, cancel, getAdminSessions, params.page, params.size, filters]);

    const handleReactivateSession = useCallback(async () => {
        if (!reactivatingSession) return;
        await reactivate(reactivatingSession.id);
        setReactivatingSession(null);
        getAdminSessions({ page: params.page ?? 0, size: params.size, ...filters });
    }, [reactivatingSession, reactivate, getAdminSessions, params.page, params.size, filters]);

    const activeFilterCount = useMemo(() => {
        return Object.values(filters).filter(v => v !== undefined && v !== '').length;
    }, [filters]);

    const hasActiveFilters = activeFilterCount > 0;

    const hallsForSelect: CinemaHallListResponse[] = halls || [];

    if (showDelayedLoading && !adminSessions.length) {
        return (
            <div className={styles.loadingContainer}>
                <LoadingSpinner text="Loading sessions..." />
            </div>
        );
    }

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <div className={styles.headerContent}>
                    <h1 className={styles.title}>Session Schedule</h1>
                    <p className={styles.subtitle}>Manage movie sessions, showtimes, and schedules</p>
                </div>
                <Button variant="primary" onClick={() => setIsCreateModalOpen(true)} disabled={loading}>
                    Add Session
                </Button>
            </div>

            <SessionFilters
                filters={filters}
                onDateFromChange={(v) => handleFilterChange('dateFrom', v)}
                onDateToChange={(v) => handleFilterChange('dateTo', v)}
                onHallChange={(v) => handleFilterChange('hallId', v)}
                onMovieTitleChange={(v) => handleFilterChange('movieTitle', v)}
                onStatusChange={(v) => handleFilterChange('status', v)}
                onClearFilters={handleClearFilters}
                halls={hallsForSelect}
            />

            {pagination && pagination.totalElements > 0 && (
                <div className={styles.resultsInfo}>
                    Showing {pagination.number * pagination.size + 1}-
                    {Math.min((pagination.number + 1) * pagination.size, pagination.totalElements)} of{' '}
                    {pagination.totalElements} sessions
                    {hasActiveFilters && ' (filtered)'}
                </div>
            )}

            <div className={styles.tableSection}>
                <SessionTable
                    sessions={adminSessions}
                    onEdit={setEditingSession}
                    onDelete={setDeletingSession}
                    onCancel={setCancellingSession}
                    onReactivate={setReactivatingSession}
                />
            </div>

            {pagination && pagination.totalPages > 1 && (
                <div className={styles.paginationSection}>
                    <Pagination
                        currentPage={pagination.number}
                        totalPages={pagination.totalPages}
                        totalElements={pagination.totalElements}
                        pageSize={pagination.size}
                        onPageChange={setPage}
                        variant="pages"
                        showInfo={false}
                    />
                </div>
            )}

            <CreateSessionModal
                isOpen={isCreateModalOpen}
                onSave={handleCreateSession}
                onClose={() => setIsCreateModalOpen(false)}
                loading={loading}
                halls={hallsForSelect}
            />

            {editingSession && (
                <EditSessionModal
                    isOpen={!!editingSession}
                    session={editingSession}
                    onSave={handleUpdateSession}
                    onClose={() => setEditingSession(null)}
                    loading={loading}
                    halls={hallsForSelect}
                />
            )}

            <DeleteConfirmModal
                isOpen={!!deletingSession}
                onConfirm={handleDeleteSession}
                onCancel={() => setDeletingSession(null)}
                itemName={deletingSession?.movieTitle}
                itemType="session"
                isDeleting={loading}
            />

            <ConfirmModal
                isOpen={!!cancellingSession}
                onConfirm={handleCancelSession}
                onCancel={() => setCancellingSession(null)}
                title="Cancel Session"
                message={`Are you sure you want to cancel the session "${cancellingSession?.movieTitle}"?`}
                confirmText="Cancel Session"
                variant="error"
                isLoading={loading}
            />

            <ConfirmModal
                isOpen={!!reactivatingSession}
                onConfirm={handleReactivateSession}
                onCancel={() => setReactivatingSession(null)}
                title="Reactivate Session"
                message={`Are you sure you want to reactivate the session "${reactivatingSession?.movieTitle}"?`}
                confirmText="Reactivate"
                variant="success"
                isLoading={loading}
            />
        </div>
    );
};