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

    getUserTickets: (status?: TicketStatus) => {
        const url = status ? `${BASE_URL}?status=${status}` : BASE_URL;
        return fetchApi<TicketResponse[]>(url);
    },

    getBookingTickets: (bookingId: number) =>
        fetchApi<TicketResponse[]>(`${BASE_URL}/booking/${bookingId}`),

    validate: (ticketCode: string) =>
        fetchApi<string>(`${BASE_URL}/validate/${ticketCode}`, {
            method: 'POST',
        }),

    getQrCode: (ticketCode: string) =>
        fetch(`${BASE_URL}/${ticketCode}/qr`, {
            headers: getAuthHeaders(),
        }),

    voidTicket: (ticketId: number) =>
        fetchApi<void>(`${BASE_URL}/${ticketId}/void`, {
            method: 'POST',
        }),

    checkStatus: (ticketCode: string) =>
        fetchApi<string>(`${BASE_URL}/${ticketCode}/status`)
};