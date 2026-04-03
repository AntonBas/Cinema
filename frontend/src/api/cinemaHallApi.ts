import { api } from '@/services/api';
import type {
    CinemaHallResponse,
    CinemaHallRequest,
    HallLayoutResponse
} from '@/types/cinemaHall';

const BASE_URL = '/api/admin/cinema-halls';

export const cinemaHallApi = {
    getAll: () => api.get<CinemaHallResponse[]>(BASE_URL),

    getById: (id: number) => api.get<CinemaHallResponse>(`${BASE_URL}/${id}`),

    create: (request: CinemaHallRequest) =>
        api.post<CinemaHallResponse>(BASE_URL, request),

    update: (id: number, request: CinemaHallRequest) =>
        api.put<CinemaHallResponse>(`${BASE_URL}/${id}`, request),

    delete: (id: number) => api.delete<void>(`${BASE_URL}/${id}`),

    getLayout: (id: number) => api.get<HallLayoutResponse>(`${BASE_URL}/${id}/layout`)
};