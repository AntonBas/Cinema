import { api } from '@/services/api';
import type {
    BookingResponse,
    BookingCreateRequest,
    BookingStatus
} from '@/types/booking';
import type { PageResponse } from '@/types/pagination';

const BASE_URL = '/api/bookings';

export const bookingApi = {
    create: (request: BookingCreateRequest) =>
        api.post<BookingResponse>(BASE_URL, request),

    getById: (bookingId: number) =>
        api.get<BookingResponse>(`${BASE_URL}/${bookingId}`),

    getUserBookings: (params?: { status?: BookingStatus; page?: number; size?: number }) =>
        api.get<PageResponse<BookingResponse>>(BASE_URL, {
            params: { ...params, size: params?.size || 20 }
        }),

    cancel: (bookingId: number) =>
        api.delete<void>(`${BASE_URL}/${bookingId}`)
};