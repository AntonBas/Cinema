import { api } from '@/services/api';
import type { SeatReservationResponse } from '@/types/seatReservation';

const BASE_URL = '/api/sessions';

export const seatReservationApi = {
    getSeatAvailability: (sessionId: number) =>
        api.get<SeatReservationResponse>(`${BASE_URL}/${sessionId}/seats/availability`),

    temporaryHoldSeat: (sessionId: number, seatId: number) =>
        api.post<void>(`${BASE_URL}/${sessionId}/seats/${seatId}/hold`),

    cancelTemporaryHold: (sessionId: number, seatId: number) =>
        api.delete<void>(`${BASE_URL}/${sessionId}/seats/${seatId}/hold`),
};