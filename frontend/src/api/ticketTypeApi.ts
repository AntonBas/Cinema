import { api } from '@/services/api';
import type {
    TicketTypeResponse,
    TicketTypeUserResponse,
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest
} from '@/types/ticketType';
import type { PageResponse } from '@/types/pagination';

const BASE_URL = '/api/ticket-types';
const ADMIN_BASE_URL = '/api/admin/ticket-types';

export const ticketTypeApi = {
    public: {
        getDropdownTypes: () =>
            api.get<TicketTypeUserResponse[]>(`${BASE_URL}/dropdown`),
    },

    admin: {
        create: (request: TicketTypeCreateRequest) =>
            api.post<TicketTypeResponse>(ADMIN_BASE_URL, request),

        getAll: (params?: { active?: boolean; category?: string; search?: string; page?: number; size?: number }) =>
            api.get<PageResponse<TicketTypeResponse>>(ADMIN_BASE_URL, { params }),

        getById: (id: number) =>
            api.get<TicketTypeResponse>(`${ADMIN_BASE_URL}/${id}`),

        update: (id: number, request: TicketTypeUpdateRequest) =>
            api.put<TicketTypeResponse>(`${ADMIN_BASE_URL}/${id}`, request),

        delete: (id: number) =>
            api.delete<void>(`${ADMIN_BASE_URL}/${id}`),

        toggleActive: (id: number) =>
            api.patch<TicketTypeResponse>(`${ADMIN_BASE_URL}/${id}/toggle-active`),
    }
};