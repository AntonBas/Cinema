import type {
    CinemaHallResponse,
    CinemaHallRequest,
    CinemaHallWithSeatsResponse,
    HallLayoutResponse
} from '@/types';
import { handleApiError } from '@/utils/apiErrorHandler';

const BASE_URL = '/api/cinema-halls';

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
    createHall: (request: CinemaHallRequest) =>
        fetchApi<CinemaHallResponse>(BASE_URL, {
            method: 'POST',
            body: JSON.stringify(request),
        }),

    getHallById: (id: number) =>
        fetchApi<CinemaHallResponse>(`${BASE_URL}/${id}`),

    updateHall: (id: number, request: CinemaHallRequest) =>
        fetchApi<CinemaHallResponse>(`${BASE_URL}/${id}`, {
            method: 'PUT',
            body: JSON.stringify(request),
        }),

    deleteHall: (id: number) =>
        fetchApi<void>(`${BASE_URL}/${id}`, { method: 'DELETE' }),

    getAllHalls: () =>
        fetchApi<CinemaHallResponse[]>(BASE_URL),

    getHallWithSeats: (id: number) =>
        fetchApi<CinemaHallWithSeatsResponse>(`${BASE_URL}/${id}/with-seats`),

    getHallLayout: (id: number) =>
        fetchApi<HallLayoutResponse>(`${BASE_URL}/${id}/layout`),

    searchHalls: (name?: string) => {
        const url = name ? `${BASE_URL}/search?name=${encodeURIComponent(name)}` : `${BASE_URL}/search`;
        return fetchApi<CinemaHallResponse[]>(url);
    },
};