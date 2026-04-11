import { useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type {
    SessionResponse,
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionRequest,
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
    const adminSessionsApi = useApi<PageResponse<SessionAdminResponse>>();
    const publicSessionsApi = useApi<SessionScheduleResponse[]>();
    const sessionApiHook = useApi<SessionResponse>();
    const mutationApi = useApi<SessionResponse | void>();

    const loading = useDelayedLoading(
        adminSessionsApi.loading || publicSessionsApi.loading || sessionApiHook.loading || mutationApi.loading,
        { delay: 150, minDisplayTime: 300 }
    );

    const getAdminSessions = useCallback(async (params?: AdminSessionParams) => {
        return adminSessionsApi.execute(() => sessionApi.admin.getSessions(params));
    }, [adminSessionsApi]);

    const getSchedule = useCallback(async (params?: PublicSessionParams) => {
        return publicSessionsApi.execute(() => sessionApi.public.getSchedule(params));
    }, [publicSessionsApi]);

    const getById = useCallback(async (id: number) => {
        return sessionApiHook.execute(() => sessionApi.admin.getById(id));
    }, [sessionApiHook]);

    const create = useCallback(async (request: SessionRequest) => {
        return mutationApi.execute(
            () => sessionApi.admin.create(request),
            { successMessage: 'Session created successfully' }
        );
    }, [mutationApi]);

    const update = useCallback(async (id: number, request: SessionRequest) => {
        return mutationApi.execute(
            () => sessionApi.admin.update(id, request),
            { successMessage: 'Session updated successfully' }
        );
    }, [mutationApi]);

    const cancel = useCallback(async (id: number) => {
        return mutationApi.execute(
            () => sessionApi.admin.cancel(id),
            { successMessage: 'Session cancelled successfully' }
        );
    }, [mutationApi]);

    const reactivate = useCallback(async (id: number) => {
        return mutationApi.execute(
            () => sessionApi.admin.reactivate(id),
            { successMessage: 'Session reactivated successfully' }
        );
    }, [mutationApi]);

    const remove = useCallback(async (id: number) => {
        return mutationApi.execute(
            () => sessionApi.admin.delete(id),
            { successMessage: 'Session deleted successfully' }
        );
    }, [mutationApi]);

    return {
        adminSessions: adminSessionsApi.data?.content || [],
        schedule: publicSessionsApi.data || [],
        session: sessionApiHook.data,
        pagination: adminSessionsApi.data,
        loading,
        adminError: adminSessionsApi.error,
        scheduleError: publicSessionsApi.error,
        sessionError: sessionApiHook.error,
        mutationError: mutationApi.error,
        getAdminSessions,
        getSchedule,
        getById,
        create,
        update,
        cancel,
        reactivate,
        remove,
    };
};