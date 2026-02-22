import type { SeatResponse, SeatType } from '@/types/seat';
import { handleApiError } from '@/utils/apiErrorHandler';

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

export const seatApi = {
    admin: {
        updateSeatType: (hallId: number, seatId: number, seatType: SeatType): Promise<SeatResponse> =>
            fetchApi<SeatResponse>(`/api/admin/cinema-halls/${hallId}/seats/${seatId}/type?seatType=${seatType}`, {
                method: 'PUT',
            }),

        setSeatStatus: (hallId: number, seatId: number, active: boolean): Promise<SeatResponse> =>
            fetchApi<SeatResponse>(`/api/admin/cinema-halls/${hallId}/seats/${seatId}/status?active=${active}`, {
                method: 'PUT',
            }),
    }
};