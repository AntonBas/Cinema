import type {
    CinemaHallResponse,
    CinemaHallRequest,
    CinemaHallWithSeatsResponse,
    HallLayoutResponse,
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

const handleResponseError = async (response: Response): Promise<never> => {
    let errorMessage = 'Failed to process request';

    try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorMessage;
    } catch {
        if (response.status === 409) {
            errorMessage = 'Cinema hall with this name already exists';
        } else if (response.status === 400) {
            errorMessage = 'Invalid request data';
        } else if (response.status === 401) {
            errorMessage = 'Unauthorized access';
        } else if (response.status === 403) {
            errorMessage = 'Access forbidden';
        } else if (response.status === 404) {
            errorMessage = 'Cinema hall not found';
        } else if (response.status >= 500) {
            errorMessage = 'Server error occurred';
        }
    }

    throw new Error(errorMessage);
};

export const cinemaHallApi = {
    createHall: async (request: CinemaHallRequest): Promise<CinemaHallResponse> => {
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) await handleResponseError(response);
        return response.json();
    },

    getHallById: async (id: number): Promise<CinemaHallResponse> => {
        const response = await fetch(`${API_URL}/${id}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleResponseError(response);
        return response.json();
    },

    updateHall: async (id: number, request: CinemaHallRequest): Promise<CinemaHallResponse> => {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) await handleResponseError(response);
        return response.json();
    },

    deleteHall: async (id: number): Promise<void> => {
        const response = await fetch(`${API_URL}/${id}`, {
            method: 'DELETE',
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleResponseError(response);
    },

    getAllHalls: async (): Promise<CinemaHallResponse[]> => {
        const response = await fetch(API_URL, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleResponseError(response);
        return response.json();
    },

    generateSeats: async (id: number, request: SeatLayoutRequest): Promise<CinemaHallWithSeatsResponse> => {
        const response = await fetch(`${API_URL}/${id}/seats`, {
            method: 'POST',
            headers: getAuthHeaders(),
            body: JSON.stringify(request),
        });
        if (!response.ok) await handleResponseError(response);
        return response.json();
    },

    getHallWithSeats: async (id: number): Promise<CinemaHallWithSeatsResponse> => {
        const response = await fetch(`${API_URL}/${id}/with-seats`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleResponseError(response);
        return response.json();
    },

    getHallLayout: async (id: number): Promise<HallLayoutResponse> => {
        const response = await fetch(`${API_URL}/${id}/layout`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleResponseError(response);
        return response.json();
    },

    searchHalls: async (name?: string): Promise<CinemaHallResponse[]> => {
        const url = name
            ? `${API_URL}/search?name=${encodeURIComponent(name)}`
            : `${API_URL}/search`;

        const response = await fetch(url, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) await handleResponseError(response);
        return response.json();
    },
};