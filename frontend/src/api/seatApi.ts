import type { SeatResponse, SeatType } from '@/types';

const getAuthHeaders = () => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
    };
};

export const seatApi = {
    getSeatsByHall: async (hallId: number): Promise<SeatResponse[]> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch seats');
        return response.json();
    },

    getSeatById: async (hallId: number, seatId: number): Promise<SeatResponse> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/${seatId}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch seat');
        return response.json();
    },

    getSeatByPosition: async (hallId: number, row: number, number: number): Promise<SeatResponse> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/position?row=${row}&number=${number}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch seat by position');
        return response.json();
    },

    updateSeatType: async (hallId: number, seatId: number, seatType: SeatType): Promise<SeatResponse> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/${seatId}/type?seatType=${seatType}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to update seat type');
        return response.json();
    },

    checkSeatAvailability: async (hallId: number, row: number, number: number): Promise<boolean> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/check-availability?row=${row}&number=${number}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to check seat availability');
        return response.json();
    },

    countSeatsByHall: async (hallId: number): Promise<number> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/count`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to count seats');
        return response.json();
    },

    getSeatsByType: async (hallId: number, seatType: SeatType): Promise<SeatResponse[]> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/by-type?seatType=${seatType}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw new Error('Failed to fetch seats by type');
        return response.json();
    },
};