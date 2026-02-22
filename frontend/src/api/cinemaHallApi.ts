import type {
    CinemaHallResponse,
    CinemaHallRequest,
    HallLayoutResponse
} from '@/types/cinemaHall';
import { handleApiError } from '@/utils/apiErrorHandler';

const ADMIN_URL = '/api/admin/cinema-halls';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
    };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const headers = getAuthHeaders();

    const response = await fetch(url, {
        headers,
        ...options,
    });

    if (!response.ok) throw await handleApiError(response);
    if (response.status === 204) return undefined as T;

    return response.json();
};

export const cinemaHallApi = {
    admin: {
        getAll: (): Promise<CinemaHallResponse[]> =>
            fetchApi<CinemaHallResponse[]>(ADMIN_URL),

        create: (request: CinemaHallRequest): Promise<CinemaHallResponse> =>
            fetchApi<CinemaHallResponse>(ADMIN_URL, {
                method: 'POST',
                body: JSON.stringify(request),
            }),

        update: (id: number, request: CinemaHallRequest): Promise<CinemaHallResponse> =>
            fetchApi<CinemaHallResponse>(`${ADMIN_URL}/${id}`, {
                method: 'PUT',
                body: JSON.stringify(request),
            }),

        delete: (id: number): Promise<void> =>
            fetchApi<void>(`${ADMIN_URL}/${id}`, {
                method: 'DELETE'
            }),

        getLayout: (id: number): Promise<HallLayoutResponse> =>
            fetchApi<HallLayoutResponse>(`${ADMIN_URL}/${id}/layout`)
    }
};