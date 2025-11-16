import type {
    CinemaHallResponse,
    CinemaHallRequest,
    CinemaHallWithSeatsResponse,
    HallLayoutResponse,
    SeatLayoutRequest
} from '@/types';
import { handleApiError } from '@/utils/apiErrorHandler';

const API_URL = '/api/cinema-halls';

const getAuthHeaders = () => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
    };
};

export const cinemaHallApi = {
    createHall: async (request: CinemaHallRequest): Promise<CinemaHallResponse> => {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    getHallById: async (id: number): Promise<CinemaHallResponse> => {
        const response = await fetch(`${API_URL}/${id}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    updateHall: async (id: number, request: CinemaHallRequest): Promise<CinemaHallResponse> => {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    deleteHall: async (id: number): Promise<void> => {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleApiError(response);
    },

    getAllHalls: async (): Promise<CinemaHallResponse[]> => {
        const response = await fetch(API_URL, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    generateSeats: async (id: number, request: SeatLayoutRequest): Promise<CinemaHallWithSeatsResponse> => {
        const response = await fetch(`${API_URL}/${id}/seats`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    getHallWithSeats: async (id: number): Promise<CinemaHallWithSeatsResponse> => {
        const response = await fetch(`${API_URL}/${id}/with-seats`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    getHallLayout: async (id: number): Promise<HallLayoutResponse> => {
        const response = await fetch(`${API_URL}/${id}/layout`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },

    searchHalls: async (name?: string): Promise<CinemaHallResponse[]> => {
        const url = name
            ? `${API_URL}/search?name=${encodeURIComponent(name)}`
            : `${API_URL}/search`;

        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleApiError(response);
        return response.json();
    },
};