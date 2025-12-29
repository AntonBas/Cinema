import type {
    CinemaHallResponse,
    CinemaHallRequest,
    CinemaHallWithSeatsResponse,
    HallLayoutResponse
} from '@/types';
import { handleApiError } from '@/utils/apiErrorHandler';

const PUBLIC_API_URL = '/api/cinema-halls';
const ADMIN_API_URL = '/api/admin/cinema-halls';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
    };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const response = await fetch(url, {
        headers: getAuthHeaders(),
        ...options,
    });

    if (!response.ok) throw await handleApiError(response);
    if (response.status === 204) return undefined as T;

    return response.json();
};

export const cinemaHallApi = {
    getHallById: (id: number) =>
        fetchApi<CinemaHallResponse>(`${PUBLIC_API_URL}/${id}`),

    getAllHalls: () =>
        fetchApi<CinemaHallResponse[]>(PUBLIC_API_URL),

    getHallWithSeats: (id: number) =>
        fetchApi<CinemaHallWithSeatsResponse>(`${PUBLIC_API_URL}/${id}/with-seats`),

    getHallLayout: (id: number) =>
        fetchApi<HallLayoutResponse>(`${PUBLIC_API_URL}/${id}/layout`),

    searchHalls: (name?: string) => {
        const url = name ? `${PUBLIC_API_URL}/search?name=${encodeURIComponent(name)}` : `${PUBLIC_API_URL}/search`;
        return fetchApi<CinemaHallResponse[]>(url);
    },

    createHall: (request: CinemaHallRequest) =>
        fetchApi<CinemaHallResponse>(ADMIN_API_URL, {
            method: 'POST',
            body: JSON.stringify(request),
        }),

    updateHall: (id: number, request: CinemaHallRequest) =>
        fetchApi<CinemaHallResponse>(`${ADMIN_API_URL}/${id}`, {
            method: 'PUT',
            body: JSON.stringify(request),
        }),

    deleteHall: (id: number) =>
        fetchApi<void>(`${ADMIN_API_URL}/${id}`, { method: 'DELETE' }),
};