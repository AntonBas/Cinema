import React, { useState, useMemo, useEffect, useRef } from 'react';
import { useSession } from '@/hooks/features';
import { useNotification } from '@/hooks/common/useNotification';
import { SessionFilters } from './SessionFilters';
import { SessionTable } from './SessionTable';
import { CreateSessionModal, EditSessionModal } from './SessionModal';
import { DeleteConfirmModal, Pagination, Button, Notification } from '@/components/ui';
import type { SessionAdminResponse, SessionCreateRequest, SessionUpdateRequest, CinemaSessionStatus } from '@/types/session';
import styles from './SectionSchedule.module.css';

export const SectionSchedule: React.FC = () => {
    const { notifications, showNotification, hideNotification } = useNotification();
    const showNotificationRef = useRef(showNotification);

    useEffect(() => {
        showNotificationRef.current = showNotification;
    }, [showNotification]);

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
        date?: string;
        hallId?: number;
        movieId?: number;
        status?: CinemaSessionStatus;
    }>({});

    const [pagination, setPagination] = useState({
        page: 0,
        size: 20,
        sort: 'startTime,desc' as string
    });

    const {
        sessions,
        pagination: apiPagination,
        loading,
        getSessions,
        createSession,
        updateSession,
        deleteSession,
        cancelSession,
        reactivateSession,
    } = useSession();

    const getSessionsRef = useRef(getSessions);
    const createSessionRef = useRef(createSession);
    const updateSessionRef = useRef(updateSession);
    const deleteSessionRef = useRef(deleteSession);
    const cancelSessionRef = useRef(cancelSession);
    const reactivateSessionRef = useRef(reactivateSession);

    useEffect(() => {
        getSessionsRef.current = getSessions;
        createSessionRef.current = createSession;
        updateSessionRef.current = updateSession;
        deleteSessionRef.current = deleteSession;
        cancelSessionRef.current = cancelSession;
        reactivateSessionRef.current = reactivateSession;
    }, [getSessions, createSession, updateSession, deleteSession, cancelSession, reactivateSession]);

    const requestParams = useMemo(() => ({
        page: pagination.page,
        size: pagination.size,
        sort: pagination.sort,
        search: undefined,
        date: filters.date,
        hallId: filters.hallId,
        movieId: filters.movieId,
        status: filters.status
    }), [pagination.page, pagination.size, pagination.sort, filters.date, filters.hallId, filters.movieId, filters.status]);

    useEffect(() => {
        const fetchData = async () => {
            await getSessionsRef.current(requestParams);
        };
        fetchData();
    }, [requestParams]);

    const handleDateFilter = (date: string | undefined) => {
        setFilters(prev => ({ ...prev, date }));
        setPagination(prev => ({ ...prev, page: 0 }));
    };

    const handleHallFilter = (hallId: number | undefined) => {
        setFilters(prev => ({ ...prev, hallId }));
        setPagination(prev => ({ ...prev, page: 0 }));
    };

    const handleMovieFilter = (movieId: number | undefined) => {
        setFilters(prev => ({ ...prev, movieId }));
        setPagination(prev => ({ ...prev, page: 0 }));
    };

    const handleStatusFilter = (status: CinemaSessionStatus | undefined) => {
        setFilters(prev => ({ ...prev, status }));
        setPagination(prev => ({ ...prev, page: 0 }));
    };

    const handleClearFilters = () => {
        setFilters({});
        setPagination(prev => ({ ...prev, page: 0 }));
        showNotificationRef.current('Filters cleared', 'info');
    };

    const hasActiveFilters = useMemo(() => {
        return Object.values(filters).some(value => value !== undefined);
    }, [filters]);

    const activeFilterCount = useMemo(() => {
        return Object.values(filters).filter(value => value !== undefined).length;
    }, [filters]);

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
            await deleteSessionRef.current(sessionToDelete.id);
            showNotificationRef.current('Session deleted successfully', 'success');
            await getSessionsRef.current(requestParams);
        } catch (error) {
            showNotificationRef.current('Failed to delete session', 'error');
        } finally {
            setIsDeleteModalOpen(false);
            setSessionToDelete(null);
        }
    };

    const handleConfirmCancel = async () => {
        if (!sessionToCancel) return;
        try {
            await cancelSessionRef.current(sessionToCancel.id);
            showNotificationRef.current('Session cancelled successfully', 'success');
            await getSessionsRef.current(requestParams);
        } catch (error) {
            showNotificationRef.current('Failed to cancel session', 'error');
        } finally {
            setIsCancelModalOpen(false);
            setSessionToCancel(null);
        }
    };

    const handleConfirmReactivate = async () => {
        if (!sessionToReactivate) return;
        try {
            await reactivateSessionRef.current(sessionToReactivate.id);
            showNotificationRef.current('Session reactivated successfully', 'success');
            await getSessionsRef.current(requestParams);
        } catch (error) {
            showNotificationRef.current('Failed to reactivate session', 'error');
        } finally {
            setIsReactivateModalOpen(false);
            setSessionToReactivate(null);
        }
    };

    const handleSaveNewSession = async (data: SessionCreateRequest) => {
        try {
            await createSessionRef.current(data);
            showNotificationRef.current('Session created successfully', 'success');
            setIsCreateModalOpen(false);
            setSelectedSession(null);
            await getSessionsRef.current(requestParams);
        } catch (error) {
            showNotificationRef.current('Failed to create session', 'error');
        }
    };

    const handleSaveUpdatedSession = async (id: number, data: SessionUpdateRequest) => {
        try {
            await updateSessionRef.current(id, data);
            showNotificationRef.current('Session updated successfully', 'success');
            setIsUpdateModalOpen(false);
            setSelectedSession(null);
            await getSessionsRef.current(requestParams);
        } catch (error) {
            showNotificationRef.current('Failed to update session', 'error');
        }
    };

    const handlePageChange = (page: number) => {
        setPagination(prev => ({ ...prev, page }));
    };

    const totalSessions = apiPagination?.totalElements || 0;
    const currentPage = pagination.page;
    const totalPages = apiPagination?.totalPages || 1;
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
                onDateChange={handleDateFilter}
                onHallChange={handleHallFilter}
                onMovieChange={handleMovieFilter}
                onStatusChange={handleStatusFilter}
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