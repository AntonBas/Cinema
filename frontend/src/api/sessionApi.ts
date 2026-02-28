import { api } from '@/services/api';
import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    SessionFilterRequest
} from '@/types/session';
import type { SeatReservationResponse } from '@/types/seatReservation';
import type { PageResponse, SearchParams } from '@/types/pagination';

const ADMIN_URL = '/api/admin/sessions';
const PUBLIC_URL = '/api/sessions';

export const sessionApi = {
    admin: {
        create: (request: SessionCreateRequest) =>
            api.post<SessionAdminResponse>(ADMIN_URL, request),

        getById: (id: number) =>
            api.get<SessionAdminResponse>(`${ADMIN_URL}/${id}`),

        update: (id: number, request: SessionUpdateRequest) =>
            api.put<SessionAdminResponse>(`${ADMIN_URL}/${id}`, request),

        cancel: (id: number) =>
            api.patch<void>(`${ADMIN_URL}/${id}/cancel`),

        reactivate: (id: number) =>
            api.patch<void>(`${ADMIN_URL}/${id}/reactivate`),

        delete: (id: number) =>
            api.delete<void>(`${ADMIN_URL}/${id}`),

        getSessions: (params?: SearchParams & SessionFilterRequest) =>
            api.get<PageResponse<SessionAdminResponse>>(ADMIN_URL, { params }),
    },

    public: {
        getSessions: (searchTerm?: string, date?: string, params?: SearchParams) =>
            api.get<PageResponse<SessionScheduleResponse>>(PUBLIC_URL, {
                params: {
                    searchTerm,
                    date,
                    ...params,
                }
            }),

        getById: (id: number) =>
            api.get<SessionScheduleResponse>(`${PUBLIC_URL}/${id}`),

        getSeatAvailability: (sessionId: number) =>
            api.get<SeatReservationResponse>(`${PUBLIC_URL}/${sessionId}/seats`),
    }
};

export const sessionKeys = {
    all: ['sessions'] as const,
    admin: {
        all: ['sessions', 'admin'] as const,
        lists: () => [...sessionKeys.admin.all, 'list'] as const,
        list: (params?: SessionFilterRequest & SearchParams) =>
            [...sessionKeys.admin.lists(), params] as const,
        details: () => [...sessionKeys.admin.all, 'detail'] as const,
        detail: (id: number) => [...sessionKeys.admin.details(), id] as const,
    },
    public: {
        all: ['sessions', 'public'] as const,
        lists: () => [...sessionKeys.public.all, 'list'] as const,
        list: (searchTerm?: string, date?: string, params?: SearchParams) =>
            [...sessionKeys.public.lists(), { searchTerm, date, ...params }] as const,
        details: () => [...sessionKeys.public.all, 'detail'] as const,
        detail: (id: number) => [...sessionKeys.public.details(), id] as const,
        seats: (id: number) => [...sessionKeys.public.detail(id), 'seats'] as const,
    }
};