import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    CinemaSessionStatus
} from '@/types/session';
import type { PageResponse } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';

const ADMIN_URL = '/api/admin/sessions';
const PUBLIC_URL = '/api/sessions';

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
    admin: {
        create: (request: SessionCreateRequest): Promise<SessionAdminResponse> =>
            fetchApi<SessionAdminResponse>(ADMIN_URL, {
                method: 'POST',
                body: JSON.stringify(request),
            }),

        getById: (id: number): Promise<SessionAdminResponse> =>
            fetchApi<SessionAdminResponse>(`${ADMIN_URL}/${id}`),

        update: (id: number, request: SessionUpdateRequest): Promise<SessionAdminResponse> =>
            fetchApi<SessionAdminResponse>(`${ADMIN_URL}/${id}`, {
                method: 'PUT',
                body: JSON.stringify(request),
            }),

        cancel: (id: number): Promise<void> =>
            fetchApi<void>(`${ADMIN_URL}/${id}/cancel`, {
                method: 'PATCH',
            }),

        reactivate: (id: number): Promise<void> =>
            fetchApi<void>(`${ADMIN_URL}/${id}/reactivate`, {
                method: 'PATCH',
            }),

        delete: (id: number): Promise<void> =>
            fetchApi<void>(`${ADMIN_URL}/${id}`, {
                method: 'DELETE',
            }),

        getAll: (
            page?: number,
            size: number = 20,
            sort: string = 'startTime',
            search?: string,
            date?: string,
            hallId?: number,
            movieId?: number,
            status?: CinemaSessionStatus
        ): Promise<PageResponse<SessionAdminResponse>> => {
            const params = new URLSearchParams();
            if (page !== undefined) params.append('page', page.toString());
            params.append('size', size.toString());
            params.append('sort', sort);
            if (search) params.append('search', search);
            if (date) params.append('date', date);
            if (hallId !== undefined) params.append('hallId', hallId.toString());
            if (movieId !== undefined) params.append('movieId', movieId.toString());
            if (status) params.append('status', status);

            return fetchApi<PageResponse<SessionAdminResponse>>(`${ADMIN_URL}?${params}`);
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

            return fetchApi<boolean>(`${ADMIN_URL}/check-conflict?${params}`);
        },

        getTodaySessions: (page?: number, size: number = 50): Promise<PageResponse<SessionAdminResponse>> => {
            const params = new URLSearchParams();
            if (page !== undefined) params.append('page', page.toString());
            params.append('size', size.toString());
            params.append('sort', 'startTime');

            return fetchApi<PageResponse<SessionAdminResponse>>(`${ADMIN_URL}/upcoming/today?${params}`);
        },
    },

    public: {
        getSchedule: (
            page?: number,
            size: number = 20,
            date?: string,
            movieId?: number,
            daysAhead?: number
        ): Promise<PageResponse<SessionScheduleResponse>> => {
            const params = new URLSearchParams();
            if (page !== undefined) params.append('page', page.toString());
            params.append('size', size.toString());
            params.append('sort', 'startTime');
            if (date) params.append('date', date);
            if (movieId !== undefined) params.append('movieId', movieId.toString());
            if (daysAhead !== undefined) params.append('daysAhead', daysAhead.toString());

            return fetchApi<PageResponse<SessionScheduleResponse>>(
                `${PUBLIC_URL}?${params}`,
                {},
                true
            );
        },

        getById: (id: number): Promise<SessionScheduleResponse> =>
            fetchApi<SessionScheduleResponse>(`${PUBLIC_URL}/${id}`, {}, true),
    }
};