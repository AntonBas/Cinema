import React, { useState, useMemo, useEffect } from 'react';
import { useSession } from '@/hooks/features/sessions/useSession';
import { useNotification } from '@/hooks/common/useNotification';
import { SessionFilters } from './SessionFilters/SessionFilters';
import { SessionTable } from './SessionTable/SessionTable';
import { CreateSessionModal } from './SessionModal/CreateSessionModal';
import { EditSessionModal } from './SessionModal/EditSessionModal';
import { DeleteConfirmModal, Pagination, Button, Notification } from '@/components/ui';
import type { SessionAdminResponse, SessionCreateRequest, SessionUpdateRequest, CinemaSessionStatus } from '@/types/session';
import styles from './SectionSchedule.module.css';

export const SectionSchedule: React.FC = () => {
    const { notifications, showNotification, hideNotification } = useNotification();

    const [selectedSession, setSelectedSession] = useState<SessionAdminResponse | null>(null);
    const [sessionToDelete, setSessionToDelete] = useState<SessionAdminResponse | null>(null);
    const [sessionToCancel, setSessionToCancel] = useState<SessionAdminResponse | null>(null);
    const [sessionToReactivate, setSessionToReactivate] = useState<SessionAdminResponse | null>(null);

    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
    const [isReactivateModalOpen, setIsReactivateModalOpen] = useState(false);

    const [filters, setFilters] = useState<{
        dateFrom?: string;
        dateTo?: string;
        hallId?: number;
        movieId?: number;
        status?: CinemaSessionStatus;
        sort?: string;
    }>({});

    const [hasActiveFilters, setHasActiveFilters] = useState(false);
    const [currentPage, setCurrentPage] = useState(0);
    const [pageSize] = useState(10);

    const {
        sessions,
        loading,
        createSession,
        updateSession,
        deleteSession,
        cancelSession,
        reactivateSession,
        getSessions
    } = useSession();

    useEffect(() => {
        const initialLoad = async () => {
            try {
                await getSessions({ page: 0, size: pageSize });
            } catch (error) {
                showNotification('Failed to load sessions', 'error');
            }
        };
        initialLoad();
    }, []);

    const activeFilterCount = useMemo(() => {
        return Object.entries(filters).filter(([key, value]) =>
            key !== 'sort' &&
            value !== undefined
        ).length;
    }, [filters]);

    useEffect(() => {
        setHasActiveFilters(activeFilterCount > 0);
    }, [activeFilterCount]);

    const handleDateFromFilter = (dateFrom: string | undefined) => {
        setFilters(prev => ({ ...prev, dateFrom }));
        handleApplyFilters(0);
    };

    const handleDateToFilter = (dateTo: string | undefined) => {
        setFilters(prev => ({ ...prev, dateTo }));
        handleApplyFilters(0);
    };

    const handleHallFilter = (hallId: number | undefined) => {
        setFilters(prev => ({ ...prev, hallId }));
        handleApplyFilters(0);
    };

    const handleMovieFilter = (movieId: number | undefined) => {
        setFilters(prev => ({ ...prev, movieId }));
        handleApplyFilters(0);
    };

    const handleStatusFilter = (status: CinemaSessionStatus | undefined) => {
        setFilters(prev => ({ ...prev, status }));
        handleApplyFilters(0);
    };

    const handleSortChange = (sort: string) => {
        setFilters(prev => ({ ...prev, sort }));
        handleApplyFilters(0);
    };

    const handleClearFilters = () => {
        setFilters({});
        handleApplyFilters(0);
        showNotification('Filters cleared', 'info');
    };

    const handleApplyFilters = async (page?: number) => {
        try {
            setCurrentPage(page ?? 0);
            await getSessions({
                ...filters,
                page: page ?? 0,
                size: pageSize
            });
        } catch (error) {
            showNotification('Failed to apply filters', 'error');
        }
    };

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

    const handleConfirmDelete = async () => {
        if (!sessionToDelete) return;
        try {
            await deleteSession(sessionToDelete.id);
            showNotification('Session deleted successfully', 'success');
            await handleApplyFilters(currentPage);
        } catch (error) {
            showNotification('Failed to delete session', 'error');
        } finally {
            setIsDeleteModalOpen(false);
            setSessionToDelete(null);
        }
    };

    const handleConfirmCancel = async () => {
        if (!sessionToCancel) return;
        try {
            await cancelSession(sessionToCancel.id);
            showNotification('Session cancelled successfully', 'success');
            await handleApplyFilters(currentPage);
        } catch (error) {
            showNotification('Failed to cancel session', 'error');
        } finally {
            setIsCancelModalOpen(false);
            setSessionToCancel(null);
        }
    };

    const handleConfirmReactivate = async () => {
        if (!sessionToReactivate) return;
        try {
            await reactivateSession(sessionToReactivate.id);
            showNotification('Session reactivated successfully', 'success');
            await handleApplyFilters(currentPage);
        } catch (error) {
            showNotification('Failed to reactivate session', 'error');
        } finally {
            setIsReactivateModalOpen(false);
            setSessionToReactivate(null);
        }
    };

    const handleSaveNewSession = async (data: SessionCreateRequest) => {
        try {
            await createSession(data);
            showNotification('Session created successfully', 'success');
            setIsCreateModalOpen(false);
            setSelectedSession(null);
            await handleApplyFilters(0);
        } catch (error) {
            showNotification('Failed to create session', 'error');
        }
    };

    const handleSaveUpdatedSession = async (id: number, data: SessionUpdateRequest) => {
        try {
            await updateSession(id, data);
            showNotification('Session updated successfully', 'success');
            setIsUpdateModalOpen(false);
            setSelectedSession(null);
            await handleApplyFilters(currentPage);
        } catch (error) {
            showNotification('Failed to update session', 'error');
        }
    };

    const handlePageChange = (page: number) => {
        handleApplyFilters(page);
    };

    const mutationLoading = loading;

    return (
        <div className={styles.container}>
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
                onDateFromChange={handleDateFromFilter}
                onDateToChange={handleDateToFilter}
                onHallChange={handleHallFilter}
                onMovieChange={handleMovieFilter}
                onStatusChange={handleStatusFilter}
                onSortChange={handleSortChange}
                onClearFilters={handleClearFilters}
                hasActiveFilters={hasActiveFilters}
                activeFilterCount={activeFilterCount}
            />

            <div className={styles.tableSection}>
                <SessionTable
                    sessions={sessions}
                    loading={loading}
                    error={undefined}
                    onEdit={handleEditSession}
                    onDelete={handleDeleteSession}
                    onCancel={handleCancelSession}
                    onReactivate={handleReactivateSession}
                />
            </div>

            {sessions.length > 0 && (
                <div className={styles.paginationSection}>
                    <Pagination
                        currentPage={currentPage}
                        totalPages={10}
                        totalElements={100}
                        pageSize={pageSize}
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