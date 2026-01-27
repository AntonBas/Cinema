import type { SeatAvailabilityResponse } from '@/types/seatAvailability';
import { handleApiError } from '@/utils/apiErrorHandler';

const BASE_URL = '/api/sessions';

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

export const seatAvailabilityApi = {
    getSeatAvailability: (sessionId: number) =>
        fetchApi<SeatAvailabilityResponse>(`${BASE_URL}/${sessionId}/seats/availability`),
};