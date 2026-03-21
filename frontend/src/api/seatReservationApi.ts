import { api } from '@/services/api';
import type { SeatReservationResponse } from '@/types/seatReservation';

export const seatReservationApi = {
    getSeatAvailability: (sessionId: number) =>
        api.get<SeatReservationResponse>(`/api/sessions/${sessionId}/seats/availability`),

    temporaryHoldSeat: (sessionId: number, seatId: number) =>
        api.post<void>(`/api/sessions/${sessionId}/seats/${seatId}/hold`),
};