import { useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type {
    SessionResponse,
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    CinemaSessionStatus
} from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

interface AdminSessionParams extends SearchParams {
    hallId?: number;
    movieTitle?: string;
    status?: CinemaSessionStatus;
    dateFrom?: string;
    dateTo?: string;
}

interface PublicSessionParams {
    searchTerm?: string;
    date?: string;
}

export const useSession = () => {
    const getAdminSessionsApi = useApi<PageResponse<SessionAdminResponse>>();
    const getPublicSessionsApi = useApi<SessionScheduleResponse[]>();
    const getSessionByIdApi = useApi<SessionResponse>();
    const createSessionApi = useApi<SessionResponse>();
    const updateSessionApi = useApi<SessionResponse>();
    const cancelSessionApi = useApi<void>();
    const reactivateSessionApi = useApi<void>();
    const deleteSessionApi = useApi<void>();

    const rawLoading = getAdminSessionsApi.loading || getPublicSessionsApi.loading ||
        getSessionByIdApi.loading || createSessionApi.loading || updateSessionApi.loading ||
        cancelSessionApi.loading || reactivateSessionApi.loading || deleteSessionApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getAdminSessionsApi.error || getPublicSessionsApi.error ||
        getSessionByIdApi.error || createSessionApi.error || updateSessionApi.error ||
        cancelSessionApi.error || reactivateSessionApi.error || deleteSessionApi.error);

    const getAdminSessions = useCallback(async (params?: AdminSessionParams) => {
        const response = await getAdminSessionsApi.execute(
            () => sessionApi.admin.getSessions(params),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getAdminSessionsApi]);

    const getSchedule = useCallback(async (params?: PublicSessionParams) => {
        const response = await getPublicSessionsApi.execute(
            () => sessionApi.public.getSchedule(params),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getPublicSessionsApi]);

    const getById = useCallback(async (id: number) => {
        const response = await getSessionByIdApi.execute(
            () => sessionApi.admin.getById(id),
            { showErrorNotification: true }
        );
        return response || null;
    }, [getSessionByIdApi]);

    const create = useCallback(async (request: SessionCreateRequest) => {
        const response = await createSessionApi.execute(
            () => sessionApi.admin.create(request),
            { successMessage: 'Session created successfully' }
        );
        return response || null;
    }, [createSessionApi]);

    const update = useCallback(async (id: number, request: SessionUpdateRequest) => {
        const response = await updateSessionApi.execute(
            () => sessionApi.admin.update(id, request),
            { successMessage: 'Session updated successfully' }
        );
        return response || null;
    }, [updateSessionApi]);

    const cancel = useCallback(async (id: number) => {
        await cancelSessionApi.execute(
            () => sessionApi.admin.cancel(id),
            { successMessage: 'Session cancelled successfully' }
        );
    }, [cancelSessionApi]);

    const reactivate = useCallback(async (id: number) => {
        await reactivateSessionApi.execute(
            () => sessionApi.admin.reactivate(id),
            { successMessage: 'Session reactivated successfully' }
        );
    }, [reactivateSessionApi]);

    const remove = useCallback(async (id: number) => {
        await deleteSessionApi.execute(
            () => sessionApi.admin.delete(id),
            { successMessage: 'Session deleted successfully' }
        );
    }, [deleteSessionApi]);

    const resetAll = useCallback(() => {
        getAdminSessionsApi.reset();
        getPublicSessionsApi.reset();
        getSessionByIdApi.reset();
        createSessionApi.reset();
        updateSessionApi.reset();
        cancelSessionApi.reset();
        reactivateSessionApi.reset();
        deleteSessionApi.reset();
    }, [getAdminSessionsApi, getPublicSessionsApi, getSessionByIdApi, createSessionApi, updateSessionApi, cancelSessionApi, reactivateSessionApi, deleteSessionApi]);

    return {
        adminSessions: getAdminSessionsApi.data?.content || [],
        schedule: getPublicSessionsApi.data || [],
        session: getSessionByIdApi.data,
        pagination: getAdminSessionsApi.data,
        loading,
        error,
        getAdminSessions,
        getSchedule,
        getById,
        create,
        update,
        cancel,
        reactivate,
        remove,
        resetAll,
    };
};