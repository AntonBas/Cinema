import type {
    BookingResponse,
    BookingCreateRequest,
    BookingStatus
} from '@/types/booking';
import type { PageResponse } from '@/types/pagination';
import { handleApiError } from '@/utils/apiErrorHandler';

const BASE_URL = '/api/bookings';

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

export const bookingApi = {
    create: (request: BookingCreateRequest) =>
        fetchApi<BookingResponse>(BASE_URL, {
            method: 'POST',
            body: JSON.stringify(request),
        }),

    getById: (bookingId: number) =>
        fetchApi<BookingResponse>(`${BASE_URL}/${bookingId}`),

    getUserBookings: (status?: BookingStatus, page: number = 0, size: number = 20): Promise<PageResponse<BookingResponse>> => {
        const params = new URLSearchParams();
        if (status) params.append('status', status);
        params.append('page', page.toString());
        params.append('size', size.toString());
        const url = `${BASE_URL}?${params}`;
        return fetchApi<PageResponse<BookingResponse>>(url);
    },

    cancel: (bookingId: number) =>
        fetchApi<void>(`${BASE_URL}/${bookingId}`, {
            method: 'DELETE',
        }),

    getUpcomingTickets: () =>
        fetchApi<BookingResponse[]>(`${BASE_URL}/upcoming`),
};