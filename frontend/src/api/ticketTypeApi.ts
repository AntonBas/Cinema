import type {
    TicketTypeResponse,
    TicketTypeSimpleResponse,
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest
} from '@/types/ticketType';
import { handleApiError } from '@/utils/apiErrorHandler';

const PUBLIC_URL = '/api/ticket-types';
const ADMIN_URL = '/api/admin/ticket-types';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
    };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const response = await fetch(url, {
        headers: getAuthHeaders(),
        ...options,
    });
    if (!response.ok) throw await handleApiError(response);
    if (response.status === 204) return undefined as T;
    return response.json();
};

export const ticketTypeApi = {
    public: {
        getDropdownTypes: () =>
            fetchApi<TicketTypeSimpleResponse[]>(`${PUBLIC_URL}/dropdown`),
    },

    admin: {
        create: (request: TicketTypeCreateRequest) =>
            fetchApi<TicketTypeResponse>(ADMIN_URL, {
                method: 'POST',
                body: JSON.stringify(request),
            }),

        getAll: (params?: { active?: boolean; category?: string; search?: string }) => {
            const queryParams = new URLSearchParams();

            if (params?.active !== undefined) {
                queryParams.append('active', params.active.toString());
            }

            if (params?.category) {
                queryParams.append('category', params.category);
            }

            if (params?.search) {
                queryParams.append('search', params.search);
            }

            const queryString = queryParams.toString();
            const url = queryString ? `${ADMIN_URL}?${queryString}` : ADMIN_URL;

            return fetchApi<TicketTypeResponse[]>(url);
        },

        getById: (id: number) =>
            fetchApi<TicketTypeResponse>(`${ADMIN_URL}/${id}`),

        getByCode: (code: string) =>
            fetchApi<TicketTypeResponse>(`${ADMIN_URL}/code/${code}`),

        update: (id: number, request: TicketTypeUpdateRequest) =>
            fetchApi<TicketTypeResponse>(`${ADMIN_URL}/${id}`, {
                method: 'PUT',
                body: JSON.stringify(request),
            }),

        delete: (id: number) =>
            fetchApi<void>(`${ADMIN_URL}/${id}`, {
                method: 'DELETE'
            }),

        toggleActive: (id: number) =>
            fetchApi<TicketTypeResponse>(`${ADMIN_URL}/${id}/toggle-active`, {
                method: 'PATCH',
            }),

        getActiveForDropdown: () =>
            fetchApi<TicketTypeSimpleResponse[]>(`${ADMIN_URL}/dropdown`),
    }
};