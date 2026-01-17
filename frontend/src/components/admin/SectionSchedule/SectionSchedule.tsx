import React, { useState } from 'react';
import { useSessions, useSessionMutation, useSessionFilters, useNotification } from '@/hooks';
import { SessionFilters } from './SessionFilters';
import { SessionTable } from './SessionTable';
import { CreateSessionModal, EditSessionModal } from './SessionModal';
import { DeleteConfirmModal, Pagination, Button } from '@/components/ui';
import type { SessionAdminResponse, SessionCreateRequest, SessionUpdateRequest } from '@/types/session';
import styles from './SectionSchedule.module.css';

export const SectionSchedule: React.FC = () => {
    const { showNotification } = useNotification();

    const [selectedSession, setSelectedSession] = useState<SessionAdminResponse | null>(null);
    const [sessionToDelete, setSessionToDelete] = useState<SessionAdminResponse | null>(null);
    const [sessionToCancel, setSessionToCancel] = useState<SessionAdminResponse | null>(null);
    const [sessionToReactivate, setSessionToReactivate] = useState<SessionAdminResponse | null>(null);

    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
    const [isReactivateModalOpen, setIsReactivateModalOpen] = useState(false);

    const {
        filters,
        setDateFilter,
        setHallFilter,
        setMovieFilter,
        setStatusFilter,
        setDaysAheadFilter,
        clearFilters,
        hasActiveFilters,
        activeFilterCount
    } = useSessionFilters();

    const [pagination, setPagination] = useState({
        page: 0,
        size: 20,
        sort: 'startTime'
    });

    const {
        sessions,
        loading,
        error,
        pagination: apiPagination,
        refetch
    } = useSessions({
        ...filters,
        page: pagination.page,
        size: pagination.size,
        sort: pagination.sort
    });

    const {
        createSession,
        updateSession,
        deleteSession,
        cancelSession,
        reactivateSession,
        loading: mutationLoading
    } = useSessionMutation();

    const handleCreateSession = () => {
        setSelectedSession(null);
        setIsCreateModalOpen(true);
    };

    const handleEditSession = (session: SessionAdminResponse) => {
        setSelectedSession(session);
        setIsUpdateModalOpen(true);
    };

    const handleDeleteSession = (session: SessionAdminResponse) => {
        setSessionToDelete(session);
        setIsDeleteModalOpen(true);
    };

    const handleCancelSession = (session: SessionAdminResponse) => {
        setSessionToCancel(session);
        setIsCancelModalOpen(true);
    };

    const handleReactivateSession = (session: SessionAdminResponse) => {
        setSessionToReactivate(session);
        setIsReactivateModalOpen(true);
    };

    const handleViewDetails = (session: SessionAdminResponse) => {
        console.log('View session details:', session);
    };

    const handleConfirmDelete = async () => {
        if (!sessionToDelete) return;

        try {
            await deleteSession(sessionToDelete.id);
            showNotification('Session deleted successfully', 'success');
            refetch();
            setIsDeleteModalOpen(false);
            setSessionToDelete(null);
        } catch (error) {
            showNotification('Failed to delete session', 'error');
        }
    };

    const handleConfirmCancel = async () => {
        if (!sessionToCancel) return;

        try {
            await cancelSession(sessionToCancel.id);
            showNotification('Session cancelled successfully', 'success');
            refetch();
            setIsCancelModalOpen(false);
            setSessionToCancel(null);
        } catch (error) {
            showNotification('Failed to cancel session', 'error');
        }
    };

    const handleConfirmReactivate = async () => {
        if (!sessionToReactivate) return;

        try {
            await reactivateSession(sessionToReactivate.id);
            showNotification('Session reactivated successfully', 'success');
            refetch();
            setIsReactivateModalOpen(false);
            setSessionToReactivate(null);
        } catch (error) {
            showNotification('Failed to reactivate session', 'error');
        }
    };

    const handleSaveNewSession = async (data: SessionCreateRequest) => {
        try {
            await createSession(data);
            showNotification('Session created successfully', 'success');
            setIsCreateModalOpen(false);
            setSelectedSession(null);
            refetch();
        } catch (error) {
            throw error;
        }
    };

    const handleSaveUpdatedSession = async (id: number, data: SessionUpdateRequest) => {
        try {
            await updateSession(id, data);
            showNotification('Session updated successfully', 'success');
            setIsUpdateModalOpen(false);
            setSelectedSession(null);
            refetch();
        } catch (error) {
            throw error;
        }
    };

    const handlePageChange = (page: number) => {
        setPagination(prev => ({ ...prev, page }));
    };

    const handleRetry = () => {
        refetch();
    };

    const handleClearFilters = () => {
        clearFilters();
        setPagination(prev => ({ ...prev, page: 0 }));
        showNotification('Filters cleared', 'info');
    };

    const totalSessions = apiPagination?.totalElements || 0;
    const currentPage = pagination.page;
    const totalPages = apiPagination?.totalPages || 1;

    return (
        <div className={styles.container}>
            <div className={styles.header}>
                <div className={styles.headerContent}>
                    <div className={styles.titleContainer}>
                        <h1 className={styles.title}>Session Schedule</h1>
                        {hasActiveFilters && (
                            <div className={styles.filterIndicator}>
                                <span className={styles.filterDot}></span>
                                Filters Active ({activeFilterCount})
                            </div>
                        )}
                    </div>
                    <p className={styles.subtitle}>
                        Manage movie sessions, showtimes, and schedules
                    </p>
                </div>
                <Button
                    variant="primary"
                    size="medium"
                    onClick={handleCreateSession}
                    disabled={mutationLoading}
                    className={styles.createButton}
                >
                    Add Session
                </Button>
            </div>

            <SessionFilters
                filters={filters}
                onDateChange={setDateFilter}
                onHallChange={setHallFilter}
                onMovieChange={setMovieFilter}
                onStatusChange={setStatusFilter}
                onUpcomingDaysChange={setDaysAheadFilter}
                onClearFilters={handleClearFilters}
                hasActiveFilters={hasActiveFilters}
                activeFilterCount={activeFilterCount}
            />

            {error && (
                <div className={styles.error}>
                    <div className={styles.errorContent}>
                        <span className={styles.errorIcon}>⚠️</span>
                        <span className={styles.errorMessage}>{error}</span>
                    </div>
                    <Button
                        variant="secondary"
                        size="small"
                        onClick={handleRetry}
                        className={styles.retryButton}
                    >
                        Retry
                    </Button>
                </div>
            )}

            <div className={styles.tableSection}>
                <SessionTable
                    sessions={sessions}
                    loading={loading}
                    error={error || undefined}
                    onEdit={handleEditSession}
                    onDelete={handleDeleteSession}
                    onCancel={handleCancelSession}
                    onReactivate={handleReactivateSession}
                    onViewDetails={handleViewDetails}
                />
            </div>

            {totalPages > 1 && (
                <div className={styles.paginationSection}>
                    <Pagination
                        currentPage={currentPage}
                        totalPages={totalPages}
                        totalElements={totalSessions}
                        pageSize={pagination.size}
                        onPageChange={handlePageChange}
                    />
                </div>
            )}

            <CreateSessionModal
                isOpen={isCreateModalOpen}
                session={null}
                onSave={handleSaveNewSession}
                onClose={() => {
                    setIsCreateModalOpen(false);
                    setSelectedSession(null);
                }}
                loading={mutationLoading}
            />

            {selectedSession && (
                <EditSessionModal
                    isOpen={isUpdateModalOpen}
                    session={selectedSession}
                    onSave={handleSaveUpdatedSession}
                    onClose={() => {
                        setIsUpdateModalOpen(false);
                        setSelectedSession(null);
                    }}
                    loading={mutationLoading}
                />
            )}

            <DeleteConfirmModal
                isOpen={isDeleteModalOpen}
                onConfirm={handleConfirmDelete}
                onCancel={() => {
                    setIsDeleteModalOpen(false);
                    setSessionToDelete(null);
                }}
                itemName={sessionToDelete?.movieTitle}
                itemType="session"
                isDeleting={mutationLoading}
            />

            {sessionToCancel && (
                <DeleteConfirmModal
                    isOpen={isCancelModalOpen}
                    onConfirm={handleConfirmCancel}
                    onCancel={() => {
                        setIsCancelModalOpen(false);
                        setSessionToCancel(null);
                    }}
                    itemName={sessionToCancel.movieTitle}
                    itemType="session"
                    isDeleting={mutationLoading}
                    confirmText="Cancel Session"
                    cancelText="Keep Session"
                    message={`Are you sure you want to cancel the session "${sessionToCancel.movieTitle}" on ${new Date(sessionToCancel.startTime).toLocaleString()}?`}
                />
            )}

            {sessionToReactivate && (
                <DeleteConfirmModal
                    isOpen={isReactivateModalOpen}
                    onConfirm={handleConfirmReactivate}
                    onCancel={() => {
                        setIsReactivateModalOpen(false);
                        setSessionToReactivate(null);
                    }}
                    itemName={sessionToReactivate.movieTitle}
                    itemType="session"
                    isDeleting={mutationLoading}
                    confirmText="Reactivate Session"
                    cancelText="Cancel"
                    message={`Are you sure you want to reactivate the session "${sessionToReactivate.movieTitle}" on ${new Date(sessionToReactivate.startTime).toLocaleString()}?`}
                />
            )}
        </div>
    );
};