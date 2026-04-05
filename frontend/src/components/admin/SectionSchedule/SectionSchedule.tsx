import React, { useState, useMemo, useEffect, useCallback, useRef } from 'react';
import { useSession } from '@/hooks/features/sessions/useSession';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
import { usePagination } from '@/hooks/common/usePagination';
import { SessionFilters } from './SessionFilters/SessionFilters';
import { SessionTable } from './SessionTable/SessionTable';
import { CreateSessionModal } from './SessionModal/CreateSessionModal';
import { EditSessionModal } from './SessionModal/EditSessionModal';
import { DeleteConfirmModal } from '@/components/ui/DeleteConfirmModal/DeleteConfirmModal';
import { Pagination } from '@/components/ui/Pagination/Pagination';
import { Button } from '@/components/ui/Button/Button';
import { Notification } from '@/components/ui/Notification/Notification';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import type { SessionAdminResponse, SessionCreateRequest, SessionUpdateRequest, CinemaSessionStatus } from '@/types/session';
import styles from './SectionSchedule.module.css';

interface FiltersState {
    dateFrom?: string;
    dateTo?: string;
    hallId?: number;
    movieTitle?: string;
    status?: CinemaSessionStatus;
}

const DEBOUNCE_DELAY = 300;

export const SectionSchedule: React.FC = () => {
    const prevParamsRef = useRef<string>('');

    const { notifications, showNotification, hideNotification } = useNotification();
    const { allHalls, loading: hallsLoading, getAllHalls } = useCinemaHalls();
    const { publicCurrentPagination, publicUpcomingPagination, loading: moviesLoading, getPublicCurrent, getPublicUpcoming } = useMovies();

    const [selectedSession, setSelectedSession] = useState<SessionAdminResponse | null>(null);
    const [sessionToDelete, setSessionToDelete] = useState<SessionAdminResponse | null>(null);
    const [sessionToCancel, setSessionToCancel] = useState<SessionAdminResponse | null>(null);
    const [sessionToReactivate, setSessionToReactivate] = useState<SessionAdminResponse | null>(null);
    const [sessionData, setSessionData] = useState<{
        sessions: SessionAdminResponse[];
        pagination: any;
    }>({
        sessions: [],
        pagination: null
    });

    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
    const [isReactivateModalOpen, setIsReactivateModalOpen] = useState(false);

    const [filters, setFilters] = useState<FiltersState>({});

    const { params, setPage } = usePagination({ size: 10 });

    const isMounted = useRef(true);
    const initialHallsLoaded = useRef(false);
    const initialMoviesLoaded = useRef(false);
    const loadingDataRef = useRef(false);
    const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const initialLoadDone = useRef(false);

    const {
        getSessions,
        createSession,
        updateSession,
        deleteSession,
        cancelSession,
        reactivateSession,
        loading
    } = useSession();

    const showDelayedLoading = useDelayedLoading(loading, { delay: 150, minDisplayTime: 300 });

    useEffect(() => {
        isMounted.current = true;
        return () => {
            isMounted.current = false;
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, []);

    useEffect(() => {
        if (!initialHallsLoaded.current) {
            initialHallsLoaded.current = true;
            getAllHalls();
        }
    }, [getAllHalls]);

    useEffect(() => {
        if (!initialMoviesLoaded.current) {
            initialMoviesLoaded.current = true;
            getPublicCurrent();
            getPublicUpcoming();
        }
    }, [getPublicCurrent, getPublicUpcoming]);

    const allMovies = useMemo(() => {
        return [
            ...(publicCurrentPagination?.content || []),
            ...(publicUpcomingPagination?.content || [])
        ];
    }, [publicCurrentPagination, publicUpcomingPagination]);

    const activeFilterCount = useMemo(() => {
        return Object.values(filters).filter(value => value !== undefined && value !== '').length;
    }, [filters]);

    const hasActiveFilters = activeFilterCount > 0;

    const loadSessions = useCallback(async () => {
        if (loadingDataRef.current) return;

        loadingDataRef.current = true;

        try {
            const requestParams: Record<string, any> = {
                page: params.page || 0,
                size: params.size,
                ...filters
            };

            const response = await getSessions(requestParams);

            setSessionData({
                sessions: response?.content || [],
                pagination: response
            });
        } catch (error) {
            if (isMounted.current) {
                if (error instanceof Error) {
                    showNotification(error.message, 'error');
                } else {
                    showNotification('Failed to load sessions', 'error');
                }
            }
        } finally {
            loadingDataRef.current = false;
        }
    }, [params.page, params.size, filters, getSessions, showNotification]);

    useEffect(() => {
        if (!initialLoadDone.current) {
            initialLoadDone.current = true;
            loadSessions();
            return;
        }

        const paramsString = JSON.stringify({ page: params.page, size: params.size, filters });

        if (prevParamsRef.current === paramsString) return;

        prevParamsRef.current = paramsString;

        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }

        timeoutRef.current = setTimeout(() => {
            loadSessions();
        }, DEBOUNCE_DELAY);

        return () => {
            if (timeoutRef.current) {
                clearTimeout(timeoutRef.current);
            }
        };
    }, [params.page, params.size, filters, loadSessions]);

    const handleFilterChange = useCallback(<K extends keyof FiltersState>(key: K, value: FiltersState[K]) => {
        setFilters(prev => ({ ...prev, [key]: value }));
        setPage(0);
    }, [setPage]);

    const handleDateFromChange = useCallback((dateFrom: string | undefined) =>
        handleFilterChange('dateFrom', dateFrom), [handleFilterChange]);

    const handleDateToChange = useCallback((dateTo: string | undefined) =>
        handleFilterChange('dateTo', dateTo), [handleFilterChange]);

    const handleHallChange = useCallback((hallId: number | undefined) =>
        handleFilterChange('hallId', hallId), [handleFilterChange]);

    const handleMovieTitleChange = useCallback((movieTitle: string | undefined) =>
        handleFilterChange('movieTitle', movieTitle), [handleFilterChange]);

    const handleStatusChange = useCallback((status: CinemaSessionStatus | undefined) =>
        handleFilterChange('status', status), [handleFilterChange]);

    const handleClearFilters = useCallback(() => {
        setFilters({});
        setPage(0);
        showNotification('Filters cleared', 'info');
    }, [setPage, showNotification]);

    const handleCreateSession = useCallback(() => {
        setSelectedSession(null);
        setIsCreateModalOpen(true);
    }, []);

    const handleEditSession = useCallback((session: SessionAdminResponse) => {
        setSelectedSession(session);
        setIsUpdateModalOpen(true);
    }, []);

    const handleDeleteSession = useCallback((session: SessionAdminResponse) => {
        setSessionToDelete(session);
        setIsDeleteModalOpen(true);
    }, []);

    const handleCancelSession = useCallback((session: SessionAdminResponse) => {
        setSessionToCancel(session);
        setIsCancelModalOpen(true);
    }, []);

    const handleReactivateSession = useCallback((session: SessionAdminResponse) => {
        setSessionToReactivate(session);
        setIsReactivateModalOpen(true);
    }, []);

    const handleConfirmDelete = useCallback(async () => {
        if (!sessionToDelete) return;
        try {
            await deleteSession(sessionToDelete.id);
            showNotification('Session deleted successfully', 'success');
            setIsDeleteModalOpen(false);
            setSessionToDelete(null);
            await loadSessions();
        } catch (error) {
            if (error instanceof Error) {
                showNotification(error.message, 'error');
            } else {
                showNotification('Failed to delete session', 'error');
            }
        }
    }, [sessionToDelete, deleteSession, showNotification, loadSessions]);

    const handleConfirmCancel = useCallback(async () => {
        if (!sessionToCancel) return;
        try {
            await cancelSession(sessionToCancel.id);
            showNotification('Session cancelled successfully', 'success');
            setIsCancelModalOpen(false);
            setSessionToCancel(null);
            await loadSessions();
        } catch (error) {
            if (error instanceof Error) {
                showNotification(error.message, 'error');
            } else {
                showNotification('Failed to cancel session', 'error');
            }
        }
    }, [sessionToCancel, cancelSession, showNotification, loadSessions]);

    const handleConfirmReactivate = useCallback(async () => {
        if (!sessionToReactivate) return;
        try {
            await reactivateSession(sessionToReactivate.id);
            showNotification('Session reactivated successfully', 'success');
            setIsReactivateModalOpen(false);
            setSessionToReactivate(null);
            await loadSessions();
        } catch (error) {
            if (error instanceof Error) {
                showNotification(error.message, 'error');
            } else {
                showNotification('Failed to reactivate session', 'error');
            }
        }
    }, [sessionToReactivate, reactivateSession, showNotification, loadSessions]);

    const handleSaveNewSession = useCallback(async (data: SessionCreateRequest) => {
        try {
            await createSession(data);
            showNotification('Session created successfully', 'success');
            setIsCreateModalOpen(false);
            setSelectedSession(null);
            setPage(0);
            await loadSessions();
        } catch (error) {
            if (error instanceof Error) {
                showNotification(error.message, 'error');
            } else {
                showNotification('Failed to create session', 'error');
            }
            throw error;
        }
    }, [createSession, showNotification, loadSessions, setPage]);

    const handleSaveUpdatedSession = useCallback(async (id: number, data: SessionUpdateRequest) => {
        try {
            await updateSession(id, data);
            showNotification('Session updated successfully', 'success');
            setIsUpdateModalOpen(false);
            setSelectedSession(null);
            await loadSessions();
        } catch (error) {
            if (error instanceof Error) {
                showNotification(error.message, 'error');
            } else {
                showNotification('Failed to update session', 'error');
            }
            throw error;
        }
    }, [updateSession, showNotification, loadSessions]);

    const handlePageChange = useCallback((page: number) => {
        setPage(page);
    }, [setPage]);

    const handleCloseCreateModal = useCallback(() => {
        setIsCreateModalOpen(false);
        setSelectedSession(null);
    }, []);

    const handleCloseUpdateModal = useCallback(() => {
        setIsUpdateModalOpen(false);
        setSelectedSession(null);
    }, []);

    const handleCloseDeleteModal = useCallback(() => {
        setIsDeleteModalOpen(false);
        setSessionToDelete(null);
    }, []);

    const handleCloseCancelModal = useCallback(() => {
        setIsCancelModalOpen(false);
        setSessionToCancel(null);
    }, []);

    const handleCloseReactivateModal = useCallback(() => {
        setIsReactivateModalOpen(false);
        setSessionToReactivate(null);
    }, []);

    const formatSessionDateTime = useCallback((dateString: string) => {
        return new Date(dateString).toLocaleString();
    }, []);

    const paginationInfo = useMemo(() => {
        const total = sessionData.pagination?.totalElements || 0;
        const page = params.page || 0;
        const pageSize = params.size || 10;
        const start = total > 0 ? page * pageSize + 1 : 0;
        const end = Math.min(start + pageSize - 1, total);
        const totalPages = sessionData.pagination?.totalPages || 0;

        return { total, start, end, totalPages, showPagination: totalPages > 1 };
    }, [sessionData.pagination, params.page, params.size]);

    if (showDelayedLoading && !sessionData.sessions.length && !initialLoadDone.current) {
        return (
            <div className={styles.loadingContainer}>
                <LoadingSpinner text="Loading sessions..." />
            </div>
        );
    }

    return (
        <div className={styles.container}>
            {notifications.map((notification) => (
                <Notification
                    key={notification.id}
                    id={notification.id}
                    message={notification.message}
                    type={notification.type}
                    isVisible={notification.isVisible}
                    onClose={hideNotification}
                    duration={4000}
                />
            ))}

            <div className={styles.header}>
                <div className={styles.headerContent}>
                    <div className={styles.titleContainer}>
                        <h1 className={styles.title}>Session Schedule</h1>
                        {hasActiveFilters && (
                            <div className={styles.filterIndicator}>
                                <span className={styles.filterDot} />
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
                    disabled={loading}
                    className={styles.createButton}
                >
                    Add Session
                </Button>
            </div>

            <SessionFilters
                filters={filters}
                onDateFromChange={handleDateFromChange}
                onDateToChange={handleDateToChange}
                onHallChange={handleHallChange}
                onMovieTitleChange={handleMovieTitleChange}
                onStatusChange={handleStatusChange}
                onClearFilters={handleClearFilters}
                hasActiveFilters={hasActiveFilters}
                activeFilterCount={activeFilterCount}
                halls={allHalls}
                hallsLoading={hallsLoading}
                movies={allMovies}
                moviesLoading={moviesLoading}
            />

            {paginationInfo.total > 0 && (
                <div className={styles.resultsInfo}>
                    Showing {paginationInfo.start}-{paginationInfo.end} of {paginationInfo.total} sessions
                    {hasActiveFilters && ' (filtered)'}
                </div>
            )}

            <div className={styles.tableSection}>
                <SessionTable
                    sessions={sessionData.sessions}
                    onEdit={handleEditSession}
                    onDelete={handleDeleteSession}
                    onCancel={handleCancelSession}
                    onReactivate={handleReactivateSession}
                />
            </div>

            {paginationInfo.showPagination && (
                <div className={styles.paginationSection}>
                    <Pagination
                        currentPage={params.page || 0}
                        totalPages={paginationInfo.totalPages}
                        totalElements={paginationInfo.total}
                        pageSize={params.size || 10}
                        onPageChange={handlePageChange}
                        variant="pages"
                        showInfo={false}
                    />
                </div>
            )}

            <CreateSessionModal
                isOpen={isCreateModalOpen}
                onSave={handleSaveNewSession}
                onClose={handleCloseCreateModal}
                loading={loading}
                halls={allHalls}
                hallsLoading={hallsLoading}
            />

            {selectedSession && (
                <EditSessionModal
                    isOpen={isUpdateModalOpen}
                    session={selectedSession}
                    onSave={handleSaveUpdatedSession}
                    onClose={handleCloseUpdateModal}
                    loading={loading}
                    halls={allHalls}
                    hallsLoading={hallsLoading}
                />
            )}

            <DeleteConfirmModal
                isOpen={isDeleteModalOpen}
                onConfirm={handleConfirmDelete}
                onCancel={handleCloseDeleteModal}
                itemName={sessionToDelete?.movieTitle}
                itemType="session"
                isDeleting={loading}
            />

            {sessionToCancel && (
                <DeleteConfirmModal
                    isOpen={isCancelModalOpen}
                    onConfirm={handleConfirmCancel}
                    onCancel={handleCloseCancelModal}
                    itemName={sessionToCancel.movieTitle}
                    itemType="session"
                    isDeleting={loading}
                    confirmText="Cancel Session"
                    cancelText="Keep Session"
                    message={`Are you sure you want to cancel the session "${sessionToCancel.movieTitle}" on ${formatSessionDateTime(sessionToCancel.startTime)}?`}
                />
            )}

            {sessionToReactivate && (
                <DeleteConfirmModal
                    isOpen={isReactivateModalOpen}
                    onConfirm={handleConfirmReactivate}
                    onCancel={handleCloseReactivateModal}
                    itemName={sessionToReactivate.movieTitle}
                    itemType="session"
                    isDeleting={loading}
                    confirmText="Reactivate Session"
                    cancelText="Cancel"
                    message={`Are you sure you want to reactivate the session "${sessionToReactivate.movieTitle}" on ${formatSessionDateTime(sessionToReactivate.startTime)}?`}
                />
            )}
        </div>
    );
};