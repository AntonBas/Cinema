import { api } from "@/services/api";
import type { SeatReservationResponse } from "@/types/seatReservation";

const BASE_URL = "/api/sessions";

export const seatReservationApi = {
  getAvailability: (sessionId: string) =>
    api.get<SeatReservationResponse>(`${BASE_URL}/${sessionId}/seats`),
  hold: (sessionId: string, seatId: number) =>
    api.post<void>(`${BASE_URL}/${sessionId}/seats/${seatId}/hold`),
  release: (sessionId: string, seatId: number) =>
    api.delete<void>(`${BASE_URL}/${sessionId}/seats/${seatId}/hold`),
};
