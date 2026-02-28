import { api } from '@/services/api';
import type { SeatResponse, SeatType } from '@/types/seat';

export const seatApi = {
    admin: {
        updateSeatType: (hallId: number, seatId: number, seatType: SeatType) =>
            api.put<SeatResponse>(`/api/admin/cinema-halls/${hallId}/seats/${seatId}/type`, null, {
                params: { seatType }
            }),

        setSeatStatus: (hallId: number, seatId: number, active: boolean) =>
            api.put<SeatResponse>(`/api/admin/cinema-halls/${hallId}/seats/${seatId}/status`, null, {
                params: { active }
            }),
    }
};