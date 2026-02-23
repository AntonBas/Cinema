import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    SessionFilterRequest
} from '@/types/session';
import type { SeatReservationResponse } from '@/types/seatReservation';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { buildFilteredUrl } from '@/utils/paginationUtils';
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
    const seenKeys = new Set<string>();

    if (params) {
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '' && !seenKeys.has(key)) {
                searchParams.append(key, value.toString());
                seenKeys.add(key);
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
                movieTitle: params?.movieTitle,
                status: params?.status,
                dateFrom: params?.dateFrom,
                dateTo: params?.dateTo
            });

            const url = `${ADMIN_URL}?${searchParams.toString()}`;
            return fetchApi<PageResponse<SessionAdminResponse>>(url);
        },
    },

    public: {
        getSessions: (
            searchTerm?: string,
            date?: string,
            params?: SearchParams
        ): Promise<PageResponse<SessionScheduleResponse>> => {
            const filter: Record<string, any> = {};
            if (searchTerm) filter.searchTerm = searchTerm;
            if (date) filter.date = date;

            const url = buildFilteredUrl(PUBLIC_URL, params, filter, 'grid');
            return fetchApi<PageResponse<SessionScheduleResponse>>(url, {}, true);
        },

        getById: (id: number): Promise<SessionScheduleResponse> =>
            fetchApi<SessionScheduleResponse>(`${PUBLIC_URL}/${id}`, {}, true),

        getSeatAvailability: (sessionId: number): Promise<SeatReservationResponse> =>
            fetchApi<SeatReservationResponse>(`${PUBLIC_URL}/${sessionId}/seats`, {}, true),
    }
};

export const sessionKeys = {
    all: ['sessions'] as const,
    admin: {
        all: ['sessions', 'admin'] as const,
        lists: () => [...sessionKeys.admin.all, 'list'] as const,
        list: (params?: SessionFilterRequest & SearchParams) =>
            [...sessionKeys.admin.lists(), params] as const,
        details: () => [...sessionKeys.admin.all, 'detail'] as const,
        detail: (id: number) => [...sessionKeys.admin.details(), id] as const,
    },
    public: {
        all: ['sessions', 'public'] as const,
        lists: () => [...sessionKeys.public.all, 'list'] as const,
        list: (searchTerm?: string, date?: string, params?: SearchParams) =>
            [...sessionKeys.public.lists(), { searchTerm, date, ...params }] as const,
        details: () => [...sessionKeys.public.all, 'detail'] as const,
        detail: (id: number) => [...sessionKeys.public.details(), id] as const,
        seats: (id: number) => [...sessionKeys.public.detail(id), 'seats'] as const,
    }
};