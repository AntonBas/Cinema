import type { TicketResponse, TicketFilterRequest } from '@/types/ticket';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { buildPagedUrl } from '@/utils/paginationUtils';
import { handleApiError } from '@/utils/apiErrorHandler';

const BASE_URL = '/api/tickets';

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

const fetchBlob = async (url: string, options: RequestInit = {}): Promise<Blob> => {
    const response = await fetch(url, {
        headers: {
            ...(options.headers || {}),
            'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
        },
        ...options,
    });
    if (!response.ok) throw await handleApiError(response);
    return response.blob();
};

export const ticketApi = {
    getById: (ticketId: number): Promise<TicketResponse> =>
        fetchApi<TicketResponse>(`${BASE_URL}/${ticketId}`),

    getByCode: (ticketCode: string): Promise<TicketResponse> =>
        fetchApi<TicketResponse>(`${BASE_URL}/code/${ticketCode}`),

    getUserTickets: (params?: SearchParams & TicketFilterRequest): Promise<PageResponse<TicketResponse>> => {
        const url = buildPagedUrl(BASE_URL, params, 'list');
        return fetchApi<PageResponse<TicketResponse>>(url);
    },

    getUpcomingTickets: (params?: SearchParams): Promise<PageResponse<TicketResponse>> => {
        const url = buildPagedUrl(`${BASE_URL}/upcoming`, params, 'list');
        return fetchApi<PageResponse<TicketResponse>>(url);
    },

    validate: (ticketCode: string): Promise<void> =>
        fetchApi<void>(`${BASE_URL}/${ticketCode}/validate`, {
            method: 'POST',
        }),

    getQRCode: (ticketCode: string): Promise<Blob> =>
        fetchBlob(`${BASE_URL}/${ticketCode}/qr`),
};