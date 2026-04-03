import { api } from '@/services/api';
import type {
    TicketTypeAdminResponse,
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest
} from '@/types/ticketType';
import type { PageResponse } from '@/types/pagination';

const BASE_URL = '/api/admin/ticket-types';

export const ticketTypeApi = {

    admin: {
        create: (request: TicketTypeCreateRequest) =>
            api.post<TicketTypeAdminResponse>(BASE_URL, request),

        getAll: (params?: { active?: boolean; category?: string; search?: string; page?: number; size?: number }) =>
            api.get<PageResponse<TicketTypeAdminResponse>>(BASE_URL, { params }),

        getById: (id: number) =>
            api.get<TicketTypeAdminResponse>(`${BASE_URL}/${id}`),

        update: (id: number, request: TicketTypeUpdateRequest) =>
            api.put<TicketTypeAdminResponse>(`${BASE_URL}/${id}`, request),

        delete: (id: number) =>
            api.delete<void>(`${BASE_URL}/${id}`),

        toggleActive: (id: number) =>
            api.patch<TicketTypeAdminResponse>(`${BASE_URL}/${id}/toggle-active`),
    }
};