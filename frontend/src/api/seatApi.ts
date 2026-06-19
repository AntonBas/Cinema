import { api } from "@/services/api";
import type { SeatResponse, SeatType } from "@/types/seat";

const BASE_URL = "/api/admin/cinema-halls";

export const seatApi = {
  updateSeatType: (hallId: number, seatId: number, seatType: SeatType) =>
    api.put<SeatResponse>(`${BASE_URL}/${hallId}/seats/${seatId}/type`, null, {
      params: { seatType },
    }),

  setSeatActiveStatus: (hallId: number, seatId: number, active: boolean) =>
    api.put<SeatResponse>(
      `${BASE_URL}/${hallId}/seats/${seatId}/status`,
      null,
      {
        params: { active },
      },
    ),
};
