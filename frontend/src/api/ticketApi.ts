import type { TicketResponse, TicketStatus } from '@/types/ticket';
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

export const ticketApi = {
    getById: (ticketId: number) =>
        fetchApi<TicketResponse>(`${BASE_URL}/${ticketId}`),

    getByCode: (ticketCode: string) =>
        fetchApi<TicketResponse>(`${BASE_URL}/code/${ticketCode}`),

    getUserTickets: (status?: TicketStatus) => {
        const params = new URLSearchParams();
        if (status) params.append('status', status);
        const queryString = params.toString();
        const url = queryString ? `${BASE_URL}?${queryString}` : BASE_URL;
        return fetchApi<TicketResponse[]>(url);
    },

    getUpcomingTickets: () =>
        fetchApi<TicketResponse[]>(`${BASE_URL}/upcoming`),

    getQrCode: (ticketCode: string) =>
        fetch(`${BASE_URL}/${ticketCode}/qr`, {
            headers: getAuthHeaders(),
        }),

    validate: (ticketCode: string) =>
        fetchApi<void>(`${BASE_URL}/${ticketCode}/validate`, {
            method: 'POST',
        }),
};