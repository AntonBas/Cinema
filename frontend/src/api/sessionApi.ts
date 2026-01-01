import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    SessionFilter
} from '@/types/session';
import type { PageResponse } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';

const ADMIN_API_URL = '/api/admin/sessions';
const PUBLIC_API_URL = '/api/sessions';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
    };
};

const getPublicHeaders = (): HeadersInit => {
    return {
        'Content-Type': 'application/json',
    };
};

const fetchApi = async <T>(
    url: string,
    options: RequestInit = {},
    isPublic: boolean = false
): Promise<T> => {
    const headers = isPublic ? getPublicHeaders() : getAuthHeaders();

    const response = await fetch(url, {
        headers,
        ...options,
    });

    if (!response.ok) throw await handleApiError(response);
    if (response.status === 204) return undefined as T;

    return response.json();
};

export const sessionApi = {
    createSession: (request: SessionCreateRequest): Promise<SessionAdminResponse> =>
        fetchApi<SessionAdminResponse>(ADMIN_API_URL, {
            method: 'POST',
            body: JSON.stringify(request),
        }),

    getSessionByIdAdmin: (id: number): Promise<SessionAdminResponse> =>
        fetchApi<SessionAdminResponse>(`${ADMIN_API_URL}/${id}`),

    updateSession: (id: number, request: SessionUpdateRequest): Promise<SessionAdminResponse> =>
        fetchApi<SessionAdminResponse>(`${ADMIN_API_URL}/${id}`, {
            method: 'PUT',
            body: JSON.stringify(request),
        }),

    cancelSession: (id: number): Promise<void> =>
        fetchApi<void>(`${ADMIN_API_URL}/${id}/cancel`, {
            method: 'PATCH',
        }),

    reactivateSession: (id: number): Promise<void> =>
        fetchApi<void>(`${ADMIN_API_URL}/${id}/reactivate`, {
            method: 'PATCH',
        }),

    deleteSession: (id: number): Promise<void> =>
        fetchApi<void>(`${ADMIN_API_URL}/${id}`, {
            method: 'DELETE',
        }),

    getAllSessions: (
        page: number = 0,
        size: number = 20,
        sort: string = 'startTime',
        search?: string
    ): Promise<PageResponse<SessionAdminResponse>> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sort,
        });
        if (search) params.append('search', search);

        return fetchApi<PageResponse<SessionAdminResponse>>(`${ADMIN_API_URL}?${params}`);
    },

    getFilteredSessions: (filter: SessionFilter): Promise<PageResponse<SessionAdminResponse>> => {
        const params = new URLSearchParams();

        if (filter.search) params.append('search', filter.search);
        if (filter.movieId !== undefined && filter.movieId !== null) params.append('movieId', filter.movieId.toString());
        if (filter.hallId !== undefined && filter.hallId !== null) params.append('hallId', filter.hallId.toString());
        if (filter.status) params.append('status', filter.status);
        if (filter.startDate) params.append('startDate', filter.startDate);
        if (filter.endDate) params.append('endDate', filter.endDate);

        const page = filter.page !== undefined ? filter.page : 0;
        const size = filter.size !== undefined ? filter.size : 20;

        params.append('page', page.toString());
        params.append('size', size.toString());
        params.append('sort', 'startTime');

        return fetchApi<PageResponse<SessionAdminResponse>>(`${ADMIN_API_URL}/filter?${params}`);
    },

    getSessionsByDate: (
        date: string,
        page: number = 0,
        size: number = 20,
        sort: string = 'startTime'
    ): Promise<PageResponse<SessionAdminResponse>> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sort,
        });

        return fetchApi<PageResponse<SessionAdminResponse>>(`${ADMIN_API_URL}/date/${date}?${params}`);
    },

    getSessionsByHall: (
        hallId: number,
        page: number = 0,
        size: number = 20,
        sort: string = 'startTime'
    ): Promise<PageResponse<SessionAdminResponse>> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sort,
        });

        return fetchApi<PageResponse<SessionAdminResponse>>(`${ADMIN_API_URL}/hall/${hallId}?${params}`);
    },

    getSessionsByMovie: (
        movieId: number,
        page: number = 0,
        size: number = 20,
        sort: string = 'startTime'
    ): Promise<PageResponse<SessionAdminResponse>> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sort,
        });

        return fetchApi<PageResponse<SessionAdminResponse>>(`${ADMIN_API_URL}/movie/${movieId}?${params}`);
    },

    getSessionsByStatus: (
        status: string,
        page: number = 0,
        size: number = 20,
        sort: string = 'startTime'
    ): Promise<PageResponse<SessionAdminResponse>> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sort,
        });

        return fetchApi<PageResponse<SessionAdminResponse>>(`${ADMIN_API_URL}/status/${status}?${params}`);
    },

    getAvailableSessions: (
        page: number = 0,
        size: number = 20,
        sort: string = 'startTime'
    ): Promise<PageResponse<SessionAdminResponse>> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sort,
        });

        return fetchApi<PageResponse<SessionAdminResponse>>(`${ADMIN_API_URL}/available?${params}`);
    },

    checkTimeConflict: (
        hallId: number,
        startTime: string,
        durationMinutes: number,
        excludeSessionId?: number
    ): Promise<boolean> => {
        const params = new URLSearchParams({
            hallId: hallId.toString(),
            startTime: startTime,
            durationMinutes: durationMinutes.toString(),
        });

        if (excludeSessionId !== undefined) {
            params.append('excludeSessionId', excludeSessionId.toString());
        }

        return fetchApi<boolean>(`${ADMIN_API_URL}/check-conflict?${params}`);
    },

    getScheduleSessions: (
        page: number = 0,
        size: number = 20,
        sort: string = 'startTime'
    ): Promise<PageResponse<SessionScheduleResponse>> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sort,
        });

        return fetchApi<PageResponse<SessionScheduleResponse>>(
            `${PUBLIC_API_URL}/schedule?${params}`,
            {},
            true
        );
    },

    getScheduleSessionsByDate: (
        date: string,
        page: number = 0,
        size: number = 20,
        sort: string = 'startTime'
    ): Promise<PageResponse<SessionScheduleResponse>> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sort,
        });

        return fetchApi<PageResponse<SessionScheduleResponse>>(
            `${PUBLIC_API_URL}/schedule/date/${date}?${params}`,
            {},
            true
        );
    },

    getScheduleSessionsByMovie: (
        movieId: number,
        page: number = 0,
        size: number = 20,
        sort: string = 'startTime'
    ): Promise<PageResponse<SessionScheduleResponse>> => {
        const params = new URLSearchParams({
            page: page.toString(),
            size: size.toString(),
            sort,
        });

        return fetchApi<PageResponse<SessionScheduleResponse>>(
            `${PUBLIC_API_URL}/schedule/movie/${movieId}?${params}`,
            {},
            true
        );
    },

    getUpcomingScheduleSessions: (
        days: number = 7,
        page: number = 0,
        size: number = 20,
        sort: string = 'startTime'
    ): Promise<PageResponse<SessionScheduleResponse>> => {
        const params = new URLSearchParams({
            days: days.toString(),
            page: page.toString(),
            size: size.toString(),
            sort,
        });

        return fetchApi<PageResponse<SessionScheduleResponse>>(
            `${PUBLIC_API_URL}/upcoming?${params}`,
            {},
            true
        );
    },

    getSessionById: (id: number): Promise<SessionScheduleResponse> =>
        fetchApi<SessionScheduleResponse>(`${PUBLIC_API_URL}/${id}`, {}, true),
};