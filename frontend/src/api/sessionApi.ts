import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    SessionFilterRequest
} from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';
import { buildPagedUrl } from '@/utils/paginationUtils';

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

        getSessions: (params?: SearchParams & SessionFilterRequest): Promise<PageResponse<SessionAdminResponse>> => {
            const url = buildPagedUrl(ADMIN_URL, params, 'table');
            return fetchApi<PageResponse<SessionAdminResponse>>(url);
        },
    },

    public: {
        getSessions: (params?: SearchParams & SessionFilterRequest): Promise<PageResponse<SessionScheduleResponse>> => {
            const url = buildPagedUrl(PUBLIC_URL, params, 'grid');
            return fetchApi<PageResponse<SessionScheduleResponse>>(url, {}, true);
        },

        getById: (id: number): Promise<SessionScheduleResponse> =>
            fetchApi<SessionScheduleResponse>(`${PUBLIC_URL}/${id}`, {}, true),
    }
};