import type { SeatResponse, SeatType } from '@/types/seat';
import { handleApiError } from '@/utils/apiErrorHandler';

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

export const seatApi = {
    getSeatsByHall: (hallId: number): Promise<SeatResponse[]> =>
        fetchApi<SeatResponse[]>(`/api/cinema-halls/${hallId}/seats`, {}, true),

    getSeatById: (hallId: number, seatId: number): Promise<SeatResponse> =>
        fetchApi<SeatResponse>(`/api/cinema-halls/${hallId}/seats/${seatId}`, {}, true),

    getSeatByPosition: (hallId: number, row: number, number: number): Promise<SeatResponse> =>
        fetchApi<SeatResponse>(`/api/cinema-halls/${hallId}/seats/position?row=${row}&number=${number}`, {}, true),

    checkSeatAvailability: (hallId: number, row: number, number: number): Promise<boolean> =>
        fetchApi<boolean>(`/api/cinema-halls/${hallId}/seats/check-availability?row=${row}&number=${number}`, {}, true),

    countSeatsByHall: (hallId: number): Promise<number> =>
        fetchApi<number>(`/api/cinema-halls/${hallId}/seats/count`, {}, true),

    getSeatsByType: (hallId: number, seatType: SeatType): Promise<SeatResponse[]> =>
        fetchApi<SeatResponse[]>(`/api/cinema-halls/${hallId}/seats/by-type?seatType=${seatType}`, {}, true),

    admin: {
        updateSeatType: (hallId: number, seatId: number, seatType: SeatType): Promise<SeatResponse> =>
            fetchApi<SeatResponse>(`/api/admin/cinema-halls/${hallId}/seats/${seatId}/type?seatType=${seatType}`, {
                method: 'PUT',
            }),

        activateSeat: (hallId: number, seatId: number): Promise<SeatResponse> =>
            fetchApi<SeatResponse>(`/api/admin/cinema-halls/${hallId}/seats/${seatId}/activate`, {
                method: 'PUT',
            }),

        deactivateSeat: (hallId: number, seatId: number): Promise<SeatResponse> =>
            fetchApi<SeatResponse>(`/api/admin/cinema-halls/${hallId}/seats/${seatId}/deactivate`, {
                method: 'PUT',
            }),
    }
};