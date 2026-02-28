import { api } from '@/services/api';
import type {
    CinemaHallResponse,
    CinemaHallRequest,
    HallLayoutResponse
} from '@/types/cinemaHall';

const ADMIN_URL = '/api/admin/cinema-halls';

export const cinemaHallApi = {
    admin: {
        getAll: () => api.get<CinemaHallResponse[]>(ADMIN_URL),

        getById: (id: number) => api.get<CinemaHallResponse>(`${ADMIN_URL}/${id}`),

        create: (request: CinemaHallRequest) =>
            api.post<CinemaHallResponse>(ADMIN_URL, request),

        update: (id: number, request: CinemaHallRequest) =>
            api.put<CinemaHallResponse>(`${ADMIN_URL}/${id}`, request),

        delete: (id: number) => api.delete<void>(`${ADMIN_URL}/${id}`),

        getLayout: (id: number) => api.get<HallLayoutResponse>(`${ADMIN_URL}/${id}/layout`)
    }
};

export const cinemaHallKeys = {
    all: ['cinemaHalls'] as const,
    lists: () => [...cinemaHallKeys.all, 'list'] as const,
    list: () => [...cinemaHallKeys.lists()] as const,
    details: () => [...cinemaHallKeys.all, 'detail'] as const,
    detail: (id: number) => [...cinemaHallKeys.details(), id] as const,
    layouts: () => [...cinemaHallKeys.all, 'layout'] as const,
    layout: (id: number) => [...cinemaHallKeys.layouts(), id] as const,
};