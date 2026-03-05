import { api } from '@/services/api';
import type { TicketResponse } from '@/types/ticket';
import type { PageResponse, SearchParams } from '@/types/pagination';

const BASE_URL = '/api/tickets';

export const ticketApi = {
    getUpcoming: (params?: SearchParams) =>
        api.get<PageResponse<TicketResponse>>(`${BASE_URL}/upcoming`, { params }),

    getHistory: (params?: SearchParams) =>
        api.get<PageResponse<TicketResponse>>(`${BASE_URL}/history`, { params }),

    getByCode: (ticketCode: string) =>
        api.get<TicketResponse>(`${BASE_URL}/code/${ticketCode}`),

    getQRCode: (ticketCode: string) =>
        api.get<Blob>(`${BASE_URL}/code/${ticketCode}/qr`, {
            responseType: 'blob'
        }),

    validate: (ticketCode: string) =>
        api.post<void>(`${BASE_URL}/code/${ticketCode}/validate`),
};