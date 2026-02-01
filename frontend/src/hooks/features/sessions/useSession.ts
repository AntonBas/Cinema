import { useState, useCallback, useRef } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    CinemaSessionStatus
} from '@/types/session';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useSession = () => {
    const [sessions, setSessions] = useState<SessionAdminResponse[]>([]);
    const [scheduleSessions, setScheduleSessions] = useState<SessionScheduleResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<SessionAdminResponse> | null>(null);
    const [schedulePagination, setSchedulePagination] = useState<PageResponse<SessionScheduleResponse> | null>(null);
    const [filters, setFilters] = useState<{
        search?: string;
        date?: string;
        hallId?: number;
        movieId?: number;
        daysAhead?: number;
        status?: CinemaSessionStatus;
    }>({});

    const apiHookRef = useRef(useApi<PageResponse<SessionAdminResponse>>());
    const apiHook = apiHookRef.current;

    const getScheduleHook = useApi<PageResponse<SessionScheduleResponse>>();
    const getSessionByIdHook = useApi<SessionScheduleResponse>();
    const getAdminSessionByIdHook = useApi<SessionAdminResponse>();
    const createSessionHook = useApi<SessionAdminResponse>();
    const updateSessionHook = useApi<SessionAdminResponse>();
    const cancelSessionHook = useApi<void>();
    const reactivateSessionHook = useApi<void>();
    const deleteSessionHook = useApi<void>();
    const checkTimeConflictHook = useApi<boolean>();
    const getTodaySessionsHook = useApi<PageResponse<SessionAdminResponse>>();

    const abortControllerRef = useRef<AbortController | null>(null);

    const getSessions = useCallback(async (options?: {
        enabled?: boolean;
        page?: number;
        size?: number;
        sort?: string;
        search?: string;
        date?: string;
        hallId?: number;
        movieId?: number;
        status?: CinemaSessionStatus;
    }): Promise<PageResponse<SessionAdminResponse>> => {
        return apiHook.callApi(async () => {
            const response = await sessionApi.admin.getAll(
                options?.page,
                options?.size || 20,
                options?.sort || 'startTime,desc',
                options?.search,
                options?.date,
                options?.hallId,
                options?.movieId,
                options?.status
            );

            if (options?.enabled !== false) {
                setSessions(response.content);
                setPagination(response);
            }
            return response;
        }, { showErrorNotification: false });
    }, [apiHook]);

    const getSchedule = useCallback(async (options?: {
        page?: number;
        size?: number;
        date?: string;
        movieId?: number;
        daysAhead?: number;
        enabled?: boolean;
        keepPreviousData?: boolean;
    }): Promise<PageResponse<SessionScheduleResponse>> => {
        return getScheduleHook.callApi(async () => {
            const pageToLoad = options?.keepPreviousData && schedulePagination
                ? (schedulePagination.number + 1)
                : (options?.page ?? 0);

            const response = await sessionApi.public.getSchedule(
                pageToLoad,
                options?.size,
                'startTime,asc',
                options?.date,
                options?.movieId,
                options?.daysAhead
            );

            if (options?.enabled !== false) {
                if (options?.keepPreviousData && scheduleSessions.length > 0 && pageToLoad > 0) {
                    setScheduleSessions(prev => [...prev, ...response.content]);
                } else {
                    setScheduleSessions(response.content);
                }
                setSchedulePagination(response);
            }
            return response;
        }, { showErrorNotification: false });
    }, [getScheduleHook, schedulePagination, scheduleSessions.length]);

    const getSessionById = useCallback(async (id: number): Promise<SessionScheduleResponse> => {
        return getSessionByIdHook.callApi(async () => {
            return await sessionApi.public.getById(id);
        }, { showErrorNotification: false });
    }, [getSessionByIdHook]);

    const getAdminSessionById = useCallback(async (id: number): Promise<SessionAdminResponse> => {
        return getAdminSessionByIdHook.callApi(async () => {
            return await sessionApi.admin.getById(id);
        }, { showErrorNotification: false });
    }, [getAdminSessionByIdHook]);

    const createSession = useCallback(async (request: SessionCreateRequest): Promise<SessionAdminResponse> => {
        return createSessionHook.callApi(async () => {
            const response = await sessionApi.admin.create(request);
            setSessions(prev => [...prev, response]);
            return response;
        }, { showErrorNotification: false });
    }, [createSessionHook]);

    const updateSession = useCallback(async (id: number, request: SessionUpdateRequest): Promise<SessionAdminResponse> => {
        return updateSessionHook.callApi(async () => {
            const response = await sessionApi.admin.update(id, request);
            setSessions(prev => prev.map(session => session.id === id ? response : session));
            return response;
        }, { showErrorNotification: false });
    }, [updateSessionHook]);

    const cancelSession = useCallback(async (id: number): Promise<void> => {
        return cancelSessionHook.callApi(async () => {
            await sessionApi.admin.cancel(id);
            setSessions(prev => prev.map(session =>
                session.id === id ? { ...session, status: 'CANCELLED' } : session
            ));
        }, { showErrorNotification: false });
    }, [cancelSessionHook]);

    const reactivateSession = useCallback(async (id: number): Promise<void> => {
        return reactivateSessionHook.callApi(async () => {
            await sessionApi.admin.reactivate(id);
            setSessions(prev => prev.map(session =>
                session.id === id ? { ...session, status: 'SCHEDULED' } : session
            ));
        }, { showErrorNotification: false });
    }, [reactivateSessionHook]);

    const deleteSession = useCallback(async (id: number): Promise<void> => {
        return deleteSessionHook.callApi(async () => {
            await sessionApi.admin.delete(id);
            setSessions(prev => prev.filter(session => session.id !== id));
        }, { showErrorNotification: false });
    }, [deleteSessionHook]);

    const checkTimeConflict = useCallback(async (
        hallId: number,
        startTime: string,
        durationMinutes: number,
        excludeSessionId?: number
    ): Promise<boolean> => {
        if (abortControllerRef.current) {
            abortControllerRef.current.abort();
        }

        abortControllerRef.current = new AbortController();

        return checkTimeConflictHook.callApi(async () => {
            return await sessionApi.admin.checkTimeConflict(
                hallId,
                startTime,
                durationMinutes,
                excludeSessionId
            );
        }, { showErrorNotification: false });
    }, [checkTimeConflictHook]);

    const getTodaySessions = useCallback(async (page?: number, size: number = 50): Promise<PageResponse<SessionAdminResponse>> => {
        return getTodaySessionsHook.callApi(async () => {
            const response = await sessionApi.admin.getTodaySessions(page, size);
            setSessions(response.content);
            setPagination(response);
            return response;
        }, { showErrorNotification: false });
    }, [getTodaySessionsHook]);

    const setSearchFilter = useCallback((search: string | undefined) => {
        setFilters(prev => ({ ...prev, search }));
    }, []);

    const setDateFilter = useCallback((date: string | undefined) => {
        setFilters(prev => ({
            ...prev,
            date,
            daysAhead: undefined
        }));
    }, []);

    const setHallFilter = useCallback((hallId: number | undefined) => {
        setFilters(prev => ({ ...prev, hallId }));
    }, []);

    const setMovieFilter = useCallback((movieId: number | undefined) => {
        setFilters(prev => ({ ...prev, movieId }));
    }, []);

    const setStatusFilter = useCallback((status: CinemaSessionStatus | undefined) => {
        setFilters(prev => ({ ...prev, status }));
    }, []);

    const setDaysAheadFilter = useCallback((daysAhead: number | undefined) => {
        setFilters(prev => ({
            ...prev,
            daysAhead,
            date: undefined
        }));
    }, []);

    const clearFilters = useCallback(() => {
        setFilters({});
    }, []);

    const resetToDefault = useCallback(() => {
        setFilters({});
    }, []);

    const hasMore = schedulePagination ? !schedulePagination.last : false;

    const hasActiveFilters = Boolean(
        filters.search ||
        filters.date ||
        filters.hallId ||
        filters.movieId ||
        filters.daysAhead ||
        filters.status
    );

    const activeFilterCount = Object.values(filters).filter(Boolean).length;

    return {
        sessions,
        scheduleSessions,
        pagination,
        schedulePagination,
        filters,
        loading: apiHook.loading || getScheduleHook.loading || getSessionByIdHook.loading ||
            getAdminSessionByIdHook.loading || createSessionHook.loading || updateSessionHook.loading ||
            cancelSessionHook.loading || reactivateSessionHook.loading || deleteSessionHook.loading ||
            checkTimeConflictHook.loading || getTodaySessionsHook.loading,
        getSessions,
        getSchedule,
        getSessionById,
        getAdminSessionById,
        createSession,
        updateSession,
        cancelSession,
        reactivateSession,
        deleteSession,
        checkTimeConflict,
        getTodaySessions,
        setSearchFilter,
        setDateFilter,
        setHallFilter,
        setMovieFilter,
        setStatusFilter,
        setDaysAheadFilter,
        clearFilters,
        resetToDefault,
        hasMore,
        hasActiveFilters,
        activeFilterCount,
        currentPage: pagination?.number || 0,
        scheduleCurrentPage: schedulePagination?.number || 0,
        totalPages: pagination?.totalPages || 0,
        scheduleTotalPages: schedulePagination?.totalPages || 0,
        totalElements: pagination?.totalElements || 0,
        scheduleTotalElements: schedulePagination?.totalElements || 0,
        pageSize: pagination?.size || 0,
        schedulePageSize: schedulePagination?.size || 0,
        isEmpty: pagination?.empty || false,
        scheduleIsEmpty: schedulePagination?.empty || false,
        isFirstPage: pagination?.first || true,
        scheduleIsFirstPage: schedulePagination?.first || true,
        isLastPage: pagination?.last || true,
        scheduleIsLastPage: schedulePagination?.last || true,
    };
};