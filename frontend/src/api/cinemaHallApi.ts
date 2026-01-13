import type {
    CinemaHallResponse,
    CinemaHallRequest,
    CinemaHallWithSeatsResponse,
    HallLayoutResponse
} from '@/types/cinemaHall';
import { handleApiError } from '@/utils/apiErrorHandler';

const PUBLIC_URL = '/api/cinema-halls';
const ADMIN_URL = '/api/admin/cinema-halls';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
    };
};

const getPublicHeaders = (): HeadersInit => {
    return {
        'Content-Type': 'application/json',
    };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}, isPublic: boolean = false): Promise<T> => {
    const headers = isPublic ? getPublicHeaders() : getAuthHeaders();

    const response = await fetch(url, {
        headers,
        ...options,
    });

    if (!response.ok) throw await handleApiError(response);
    if (response.status === 204) return undefined as T;

    return response.json();
};

export const cinemaHallApi = {
    getById: (id: number): Promise<CinemaHallResponse> =>
        fetchApi<CinemaHallResponse>(`${PUBLIC_URL}/${id}`, {}, true),

    getAll: (): Promise<CinemaHallResponse[]> =>
        fetchApi<CinemaHallResponse[]>(PUBLIC_URL, {}, true),

    getWithSeats: (id: number): Promise<CinemaHallWithSeatsResponse> =>
        fetchApi<CinemaHallWithSeatsResponse>(`${PUBLIC_URL}/${id}/with-seats`, {}, true),

    getLayout: (id: number): Promise<HallLayoutResponse> =>
        fetchApi<HallLayoutResponse>(`${PUBLIC_URL}/${id}/layout`, {}, true),

    search: (search?: string): Promise<CinemaHallResponse[]> => {
        const url = search ? `${PUBLIC_URL}/search?search=${encodeURIComponent(search)}` : `${PUBLIC_URL}/search`;
        return fetchApi<CinemaHallResponse[]>(url, {}, true);
    },

    admin: {
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
    }
};