import { api } from '@/services/api';
import type {
    BookingResponse,
    BookingCreateRequest,
} from '@/types/booking';

const BASE_URL = '/api/bookings';

export const bookingApi = {
    create: (request: BookingCreateRequest) =>
        api.post<BookingResponse>(BASE_URL, request),

    getById: (bookingId: number) =>
        api.get<BookingResponse>(`${BASE_URL}/${bookingId}`),

    cancel: (bookingId: number) =>
        api.delete<void>(`${BASE_URL}/${bookingId}`)
};