import type { TicketResponse, TicketStatus } from '@/types/ticket';
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
    getById: (ticketId: number) =>
        fetchApi<TicketResponse>(`${BASE_URL}/${ticketId}`),

    getByCode: (ticketCode: string) =>
        fetchApi<TicketResponse>(`${BASE_URL}/code/${ticketCode}`),

    getUserTickets: (status?: TicketStatus, searchParams?: SearchParams) => {
        const params: Record<string, any> = { ...searchParams };
        if (status) params.status = status;
        const url = buildPagedUrl(BASE_URL, params, 'list');
        return fetchApi<PageResponse<TicketResponse>>(url);
    },

    getUpcomingTickets: (searchParams?: SearchParams) => {
        const url = buildPagedUrl(`${BASE_URL}/upcoming`, searchParams, 'list');
        return fetchApi<PageResponse<TicketResponse>>(url);
    },

    validate: (ticketCode: string) =>
        fetchApi<void>(`${BASE_URL}/${ticketCode}/validate`, {
            method: 'POST',
        }),

    getQRCode: (ticketCode: string): Promise<Blob> =>
        fetchBlob(`${BASE_URL}/${ticketCode}/qr`),
};