import { api } from '@/services/api';
import type { SeatReservationResponse } from '@/types/seatReservation';

export const seatAvailabilityApi = {
    getSeatAvailability: (sessionId: number) =>
        api.get<SeatReservationResponse>(`/api/sessions/${sessionId}/seats/availability`),
};