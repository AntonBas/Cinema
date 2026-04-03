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

const ADMIN_BASE_URL = '/api/admin/sessions';
const BASE_URL = '/api/sessions';

export const sessionApi = {
    admin: {
        create: (request: SessionCreateRequest) =>
            api.post<SessionAdminResponse>(ADMIN_BASE_URL, request),

        getById: (id: number) =>
            api.get<SessionAdminResponse>(`${ADMIN_BASE_URL}/${id}`),

        update: (id: number, request: SessionUpdateRequest) =>
            api.put<SessionAdminResponse>(`${ADMIN_BASE_URL}/${id}`, request),

        cancel: (id: number) =>
            api.patch<void>(`${ADMIN_BASE_URL}/${id}/cancel`),

        reactivate: (id: number) =>
            api.patch<void>(`${ADMIN_BASE_URL}/${id}/reactivate`),

        delete: (id: number) =>
            api.delete<void>(`${ADMIN_BASE_URL}/${id}`),

        getSessions: (params?: SearchParams & SessionFilterRequest) =>
            api.get<PageResponse<SessionAdminResponse>>(ADMIN_BASE_URL, { params }),
    },

    public: {
        getSessions: (searchTerm?: string, date?: string) =>
            api.get<SessionScheduleResponse[]>(BASE_URL, {
                params: { searchTerm, date }
            }),

        getSeatAvailability: (sessionId: number) =>
            api.get<SeatReservationResponse>(`${BASE_URL}/${sessionId}/seats/availability`),
    }
};