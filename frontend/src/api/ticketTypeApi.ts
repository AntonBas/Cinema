import { api } from '@/services/api';
import type {
    TicketTypeResponse,
    TicketTypeSimpleResponse,
    TicketTypeCreateRequest,
    TicketTypeUpdateRequest
} from '@/types/ticketType';

const PUBLIC_URL = '/api/ticket-types';
const ADMIN_URL = '/api/admin/ticket-types';

export const ticketTypeApi = {
    public: {
        getDropdownTypes: () =>
            api.get<TicketTypeSimpleResponse[]>(`${PUBLIC_URL}/dropdown`),
    },

    admin: {
        create: (request: TicketTypeCreateRequest) =>
            api.post<TicketTypeResponse>(ADMIN_URL, request),

        getAll: (params?: { active?: boolean; category?: string; search?: string }) =>
            api.get<TicketTypeResponse[]>(ADMIN_URL, { params }),

        getById: (id: number) =>
            api.get<TicketTypeResponse>(`${ADMIN_URL}/${id}`),

        getByCode: (code: string) =>
            api.get<TicketTypeResponse>(`${ADMIN_URL}/code/${code}`),

        update: (id: number, request: TicketTypeUpdateRequest) =>
            api.put<TicketTypeResponse>(`${ADMIN_URL}/${id}`, request),

        delete: (id: number) =>
            api.delete<void>(`${ADMIN_URL}/${id}`),

        toggleActive: (id: number) =>
            api.patch<TicketTypeResponse>(`${ADMIN_URL}/${id}/toggle-active`),

        getDropdownTypes: () =>
            api.get<TicketTypeSimpleResponse[]>(`${ADMIN_URL}/dropdown`),
    }
};