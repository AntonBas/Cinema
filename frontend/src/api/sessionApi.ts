import { api } from '@/services/api';
import type {
    SessionResponse,
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    CinemaSessionStatus
} from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';

const ADMIN_BASE_URL = '/api/admin/sessions';
const BASE_URL = '/api/sessions';

export const sessionApi = {
    admin: {
        create: (request: SessionCreateRequest) =>
            api.post<SessionResponse>(ADMIN_BASE_URL, request),

        getById: (id: number) =>
            api.get<SessionResponse>(`${ADMIN_BASE_URL}/${id}`),

        update: (id: number, request: SessionUpdateRequest) =>
            api.put<SessionResponse>(`${ADMIN_BASE_URL}/${id}`, request),

        cancel: (id: number) =>
            api.patch<void>(`${ADMIN_BASE_URL}/${id}/cancel`),

        reactivate: (id: number) =>
            api.patch<void>(`${ADMIN_BASE_URL}/${id}/reactivate`),

        delete: (id: number) =>
            api.delete<void>(`${ADMIN_BASE_URL}/${id}`),

        getSessions: (params?: SearchParams & {
            hallId?: number;
            movieTitle?: string;
            status?: CinemaSessionStatus;
            dateFrom?: string;
            dateTo?: string;
        }) => api.get<PageResponse<SessionAdminResponse>>(ADMIN_BASE_URL, { params }),
    },

    public: {
        getSchedule: (params?: {
            searchTerm?: string;
            date?: string;
        }) => api.get<SessionScheduleResponse[]>(BASE_URL, { params }),
    }
};