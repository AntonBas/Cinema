import type {
    BookingResponse,
    BookingCreateRequest,
    BookingStatus
} from '@/types/booking';
import type { PageResponse } from '@/types/pagination';
import { buildPagedUrl } from '@/utils/paginationUtils';
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

    getUserBookings: (params?: { status?: BookingStatus; page?: number; size?: number }) => {
        const url = buildPagedUrl(BASE_URL, { ...params, size: params?.size || 20 });
        return fetchApi<PageResponse<BookingResponse>>(url);
    },

    cancel: (bookingId: number) =>
        fetchApi<void>(`${BASE_URL}/${bookingId}`, {
            method: 'DELETE',
        }),
};