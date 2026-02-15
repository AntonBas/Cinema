import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    SessionFilterRequest
} from '@/types/session';
import type { SeatReservationResponse } from '@/types/seatReservation';
import type { PageResponse, SearchParams } from '@/types/pagination';
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

const getPublicHeaders = (): HeadersInit => ({
    'Content-Type': 'application/json',
});

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

const buildSearchParams = (params?: SearchParams & SessionFilterRequest): URLSearchParams => {
    const searchParams = new URLSearchParams();

    if (params) {
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                searchParams.append(key, value.toString());
            }
        });
    }

    return searchParams;
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

        getSessions: (params?: SearchParams & SessionFilterRequest): Promise<PageResponse<SessionAdminResponse>> => {
            const searchParams = buildSearchParams({
                page: params?.page || 0,
                size: params?.size || 20,
                sort: params?.sort,
                hallId: params?.hallId,
                movieId: params?.movieId,
                status: params?.status,
                dateFrom: params?.dateFrom,
                dateTo: params?.dateTo
            });

            const url = `${ADMIN_URL}?${searchParams.toString()}`;
            return fetchApi<PageResponse<SessionAdminResponse>>(url);
        },

        getSessionsAll: (params?: SearchParams & SessionFilterRequest): Promise<PageResponse<SessionAdminResponse>> =>
            sessionApi.admin.getSessions({ ...params, size: 1000 }),
    },

    public: {
        getSessions: (params?: SearchParams & SessionFilterRequest): Promise<PageResponse<SessionScheduleResponse>> => {
            const searchParams = buildSearchParams({
                page: params?.page || 0,
                size: params?.size || 12,
                sort: params?.sort || 'startTime,asc',
                hallId: params?.hallId,
                movieId: params?.movieId,
                dateFrom: params?.dateFrom,
                dateTo: params?.dateTo
            });

            const url = `${PUBLIC_URL}?${searchParams.toString()}`;
            return fetchApi<PageResponse<SessionScheduleResponse>>(url, {}, true);
        },

        getById: (id: number): Promise<SessionScheduleResponse> =>
            fetchApi<SessionScheduleResponse>(`${PUBLIC_URL}/${id}`, {}, true),

        getByDate: (date: string): Promise<PageResponse<SessionScheduleResponse>> =>
            sessionApi.public.getSessions({ dateFrom: date, dateTo: date, size: 50 }),

        getByMovie: (movieId: number): Promise<PageResponse<SessionScheduleResponse>> =>
            sessionApi.public.getSessions({ movieId, size: 20 }),

        getSeatAvailability: (sessionId: number): Promise<SeatReservationResponse> =>
            fetchApi<SeatReservationResponse>(`${PUBLIC_URL}/${sessionId}/seats`, {}, true),
    }
};

export const sessionKeys = {
    all: ['sessions'] as const,
    admin: {
        all: ['sessions', 'admin'] as const,
        lists: () => [...sessionKeys.admin.all, 'list'] as const,
        list: (params?: SearchParams & SessionFilterRequest) =>
            [...sessionKeys.admin.lists(), params] as const,
        details: () => [...sessionKeys.admin.all, 'detail'] as const,
        detail: (id: number) => [...sessionKeys.admin.details(), id] as const,
    },
    public: {
        all: ['sessions', 'public'] as const,
        lists: () => [...sessionKeys.public.all, 'list'] as const,
        list: (params?: SearchParams & SessionFilterRequest) =>
            [...sessionKeys.public.lists(), params] as const,
        details: () => [...sessionKeys.public.all, 'detail'] as const,
        detail: (id: number) => [...sessionKeys.public.details(), id] as const,
        seats: (id: number) => [...sessionKeys.public.detail(id), 'seats'] as const,
    }
};