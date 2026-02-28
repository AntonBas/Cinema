import React, { useState, useMemo, useEffect, useCallback, useRef } from 'react';
import { useSession } from '@/hooks/features/sessions/useSession';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { useNotification } from '@/hooks/common/useNotification';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';
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
    const { notifications, showNotification, hideNotification } = useNotification();
    const { allHalls, loading: hallsLoading, getAllHalls } = useCinemaHalls();
    const { publicCurrent, publicUpcoming, loading: moviesLoading, getPublicCurrent, getPublicUpcoming } = useMovies();

    const [selectedSession, setSelectedSession] = useState<SessionAdminResponse | null>(null);
    const [sessionToDelete, setSessionToDelete] = useState<SessionAdminResponse | null>(null);
    const [sessionToCancel, setSessionToCancel] = useState<SessionAdminResponse | null>(null);
    const [sessionToReactivate, setSessionToReactivate] = useState<SessionAdminResponse | null>(null);

    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [isCancelModalOpen, setIsCancelModalOpen] = useState(false);
    const [isReactivateModalOpen, setIsReactivateModalOpen] = useState(false);

    const [filters, setFilters] = useState<FiltersState>({});
    const [currentPage, setCurrentPage] = useState(0);
    const pageSize = 20;

    const isMounted = useRef(true);
    const initialHallsLoaded = useRef(false);
    const initialMoviesLoaded = useRef(false);
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);

    const {
        sessions,
        pagination,
        loading,
        createSession,
        updateSession,
        deleteSession,
        cancelSession,
        reactivateSession,
        getSessions
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
        return [...(publicCurrent || []), ...(publicUpcoming || [])];
    }, [publicCurrent, publicUpcoming]);

    const activeFilterCount = useMemo(() => {
        return Object.values(filters).filter(value => value !== undefined && value !== '').length;
    }, [filters]);

    const hasActiveFilters = activeFilterCount > 0;

    useEffect(() => {
        const loadSessions = async () => {
            if (!isMounted.current) return;

            const params: Record<string, any> = {
                page: currentPage,
                size: pageSize,
                ...filters
            };

            try {
                await getSessions(params);
            } catch (error) {
                if (isMounted.current) {
                    showNotification('Failed to load sessions', 'error');
                }
            }
        };

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
    }, [currentPage, pageSize, filters, getSessions, showNotification]);

    const handleFilterChange = useCallback(<K extends keyof FiltersState>(key: K, value: FiltersState[K]) => {
        setFilters(prev => ({ ...prev, [key]: value }));
        setCurrentPage(0);
    }, []);

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
        setCurrentPage(0);
        showNotification('Filters cleared', 'info');
    }, [showNotification]);

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
        } catch {
            showNotification('Failed to delete session', 'error');
        }
    }, [sessionToDelete, deleteSession, showNotification]);

    const handleConfirmCancel = useCallback(async () => {
        if (!sessionToCancel) return;
        try {
            await cancelSession(sessionToCancel.id);
            showNotification('Session cancelled successfully', 'success');
            setIsCancelModalOpen(false);
            setSessionToCancel(null);
        } catch {
            showNotification('Failed to cancel session', 'error');
        }
    }, [sessionToCancel, cancelSession, showNotification]);

    const handleConfirmReactivate = useCallback(async () => {
        if (!sessionToReactivate) return;
        try {
            await reactivateSession(sessionToReactivate.id);
            showNotification('Session reactivated successfully', 'success');
            setIsReactivateModalOpen(false);
            setSessionToReactivate(null);
        } catch {
            showNotification('Failed to reactivate session', 'error');
        }
    }, [sessionToReactivate, reactivateSession, showNotification]);

    const handleSaveNewSession = useCallback(async (data: SessionCreateRequest) => {
        try {
            await createSession(data);
            showNotification('Session created successfully', 'success');
            setIsCreateModalOpen(false);
            setSelectedSession(null);
        } catch {
            showNotification('Failed to create session', 'error');
            throw new Error('Failed to create session');
        }
    }, [createSession, showNotification]);

    const handleSaveUpdatedSession = useCallback(async (id: number, data: SessionUpdateRequest) => {
        try {
            await updateSession(id, data);
            showNotification('Session updated successfully', 'success');
            setIsUpdateModalOpen(false);
            setSelectedSession(null);
        } catch {
            showNotification('Failed to update session', 'error');
            throw new Error('Failed to update session');
        }
    }, [updateSession, showNotification]);

    const handlePageChange = useCallback((page: number) => {
        setCurrentPage(page);
    }, []);

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

    const totalPages = pagination?.totalPages || 0;

    if (showDelayedLoading && !sessions.length) {
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

            <div className={styles.tableSection}>
                <SessionTable
                    sessions={sessions}
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
                        totalElements={pagination?.totalElements || 0}
                        pageSize={pageSize}
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