import type {
    TicketTypeResponse,
    TicketTypeSimpleResponse,
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest
} from '@/types/ticket-type';
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
    getAllActive: () => fetchApi<TicketTypeResponse[]>(PUBLIC_URL),

    getById: (id: number) => fetchApi<TicketTypeResponse>(`${PUBLIC_URL}/${id}`),

    getByCode: (code: string) => fetchApi<TicketTypeResponse>(`${PUBLIC_URL}/code/${code}`),

    getSimpleActive: () => fetchApi<TicketTypeSimpleResponse[]>(`${PUBLIC_URL}/simple`),

    validateAge: (ticketTypeId: number, age: number) =>
        fetchApi<boolean>(`${PUBLIC_URL}/age-validation?ticketTypeId=${ticketTypeId}&age=${age}`),

    getAgeRange: (id: number) => fetchApi<string>(`${PUBLIC_URL}/${id}/age-range`),

    getAvailableForAge: (age: number) =>
        fetchApi<TicketTypeSimpleResponse[]>(`${PUBLIC_URL}/available-for-age?age=${age}`),

    admin: {
        create: (request: TicketTypeCreateRequest) =>
            fetchApi<TicketTypeResponse>(ADMIN_URL, {
                method: 'POST',
                body: JSON.stringify(request),
            }),

        getAll: (active?: boolean) => {
            const url = active !== undefined ? `${ADMIN_URL}?active=${active}` : ADMIN_URL;
            return fetchApi<TicketTypeResponse[]>(url);
        },

        getById: (id: number) => fetchApi<TicketTypeResponse>(`${ADMIN_URL}/${id}`),

        getByCode: (code: string) => fetchApi<TicketTypeResponse>(`${ADMIN_URL}/code/${code}`),

        update: (id: number, request: TicketTypeUpdateRequest) =>
            fetchApi<TicketTypeResponse>(`${ADMIN_URL}/${id}`, {
                method: 'PUT',
                body: JSON.stringify(request),
            }),

        delete: (id: number) => fetchApi<void>(`${ADMIN_URL}/${id}`, { method: 'DELETE' }),

        toggleActive: (id: number) =>
            fetchApi<TicketTypeResponse>(`${ADMIN_URL}/${id}/toggle-active`, {
                method: 'PATCH',
            }),

        getSimple: (active: boolean = true) =>
            fetchApi<TicketTypeSimpleResponse[]>(`${ADMIN_URL}/simple?active=${active}`),
    }
};