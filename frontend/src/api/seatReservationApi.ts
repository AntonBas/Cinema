import type { SeatReservationResponse } from '@/types/seatReservation';
import { handleApiError } from '@/utils/apiErrorHandler';

const getPublicHeaders = (): HeadersInit => {
    return {
        'Content-Type': 'application/json',
    };
};

const fetchApi = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
    const response = await fetch(url, {
        headers: getPublicHeaders(),
        ...options,
    });
    if (!response.ok) throw await handleApiError(response);
    if (response.status === 204) return undefined as T;
    return response.json();
};

export const seatAvailabilityApi = {
    getSeatAvailability: (sessionId: number): Promise<SeatReservationResponse> =>
        fetchApi<SeatReservationResponse>(`/api/sessions/${sessionId}/seats/availability`),
};