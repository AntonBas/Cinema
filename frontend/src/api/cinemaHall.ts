import type {
    CinemaHallDto,
    CinemaHallRequest,
    CinemaHallWithSeatsDto,
    HallLayoutDto,
    SeatLayoutRequest
} from '@/types';

const API_URL = '/api/cinema-halls';

const getAuthHeaders = () => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
    };
};

export const cinemaHallApi = {
    createHall: async (request: CinemaHallRequest): Promise<CinemaHallDto> => {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) throw new Error('Failed to create cinema hall');
        return response.json();
    },

    getHallById: async (id: number): Promise<CinemaHallDto> => {
        const response = await fetch(`${API_URL}/${id}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch cinema hall');
        return response.json();
    },

    updateHall: async (id: number, request: CinemaHallRequest): Promise<CinemaHallDto> => {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) throw new Error('Failed to update cinema hall');
        return response.json();
    },

    deleteHall: async (id: number): Promise<void> => {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to delete cinema hall');
    },

    getAllHalls: async (): Promise<CinemaHallDto[]> => {
        const response = await fetch(API_URL, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch cinema halls');
        return response.json();
    },

    generateSeats: async (id: number, request: SeatLayoutRequest): Promise<CinemaHallWithSeatsDto> => {
        const response = await fetch(`${API_URL}/${id}/seats`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) throw new Error('Failed to generate seats');
        return response.json();
    },

    getHallWithSeats: async (id: number): Promise<CinemaHallWithSeatsDto> => {
        const response = await fetch(`${API_URL}/${id}/with-seats`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch hall with seats');
        return response.json();
    },

    getHallLayout: async (id: number): Promise<HallLayoutDto> => {
        const response = await fetch(`${API_URL}/${id}/layout`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch hall layout');
        return response.json();
    },

    searchHalls: async (name?: string): Promise<CinemaHallDto[]> => {
        const url = name
            ? `${API_URL}/search?name=${encodeURIComponent(name)}`
            : `${API_URL}/search`;

        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to search cinema halls');
        return response.json();
    },
};