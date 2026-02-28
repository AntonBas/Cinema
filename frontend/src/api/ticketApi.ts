import { api } from '@/services/api';
import type { TicketResponse, TicketFilterRequest } from '@/types/ticket';
import type { PageResponse, SearchParams } from '@/types/pagination';

const BASE_URL = '/api/tickets';

export const ticketApi = {
    getById: (ticketId: number) =>
        api.get<TicketResponse>(`${BASE_URL}/${ticketId}`),

    getByCode: (ticketCode: string) =>
        api.get<TicketResponse>(`${BASE_URL}/code/${ticketCode}`),

    getUserTickets: (params?: SearchParams & TicketFilterRequest) =>
        api.get<PageResponse<TicketResponse>>(BASE_URL, { params }),

    getUpcomingTickets: (params?: SearchParams) =>
        api.get<PageResponse<TicketResponse>>(`${BASE_URL}/upcoming`, { params }),

    validate: (ticketCode: string) =>
        api.post<void>(`${BASE_URL}/${ticketCode}/validate`),

    getQRCode: (ticketCode: string) =>
        api.get<Blob>(`${BASE_URL}/${ticketCode}/qr`, {
            responseType: 'blob'
        }),
};