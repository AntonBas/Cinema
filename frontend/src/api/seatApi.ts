import type { SeatResponse, SeatType } from '@/types';
import { handleApiError } from '@/utils/apiErrorHandler';

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('authToken');
    return {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
    };
};

export const seatApi = {
    getSeatsByHall: async (hallId: number): Promise<SeatResponse[]> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    getSeatById: async (hallId: number, seatId: number): Promise<SeatResponse> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/${seatId}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    getSeatByPosition: async (hallId: number, row: number, number: number): Promise<SeatResponse> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/position?row=${row}&number=${number}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    checkSeatAvailability: async (hallId: number, row: number, number: number): Promise<boolean> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/check-availability?row=${row}&number=${number}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    countSeatsByHall: async (hallId: number): Promise<number> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/count`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    getSeatsByType: async (hallId: number, seatType: SeatType): Promise<SeatResponse[]> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/by-type?seatType=${seatType}`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    updateSeatType: async (hallId: number, seatId: number, seatType: SeatType): Promise<SeatResponse> => {
        const response = await fetch(`/api/admin/cinema-halls/${hallId}/seats/${seatId}/type?seatType=${seatType}`, {
            method: 'PUT',
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    getActiveSeatsByHall: async (hallId: number): Promise<SeatResponse[]> => {
        const response = await fetch(`/api/cinema-halls/${hallId}/seats/active`, {
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    activateSeat: async (hallId: number, seatId: number): Promise<SeatResponse> => {
        const response = await fetch(`/api/admin/cinema-halls/${hallId}/seats/${seatId}/activate`, {
            method: 'PUT',
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },

    deactivateSeat: async (hallId: number, seatId: number): Promise<SeatResponse> => {
        const response = await fetch(`/api/admin/cinema-halls/${hallId}/seats/${seatId}/deactivate`, {
            method: 'PUT',
            headers: getAuthHeaders(),
        });
        if (!response.ok) throw await handleApiError(response);
        return response.json();
    },
};