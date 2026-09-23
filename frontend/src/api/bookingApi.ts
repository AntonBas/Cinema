import { api } from "@/services/api";
import type {
  AdminBookingDetailsResponse,
  AdminBookingFilters,
  AdminBookingListResponse,
  BookingResponse,
  BookingCreateRequest,
} from "@/types/booking";
import type { PageResponse, SearchParams } from "@/types/pagination";

const BASE_URL = "/api/bookings";
const ADMIN_BASE_URL = "/api/admin/bookings";

export const bookingApi = {
  create: (request: BookingCreateRequest) =>
    api.post<BookingResponse>(BASE_URL, request),

  getById: (bookingId: string) =>
    api.get<BookingResponse>(`${BASE_URL}/${bookingId}`),

  cancel: (bookingId: string) => api.delete<void>(`${BASE_URL}/${bookingId}`),

  admin: {
    getAll: (params: SearchParams & AdminBookingFilters) =>
      api.get<PageResponse<AdminBookingListResponse>>(ADMIN_BASE_URL, {
        params,
      }),
    getById: (bookingId: number) =>
      api.get<AdminBookingDetailsResponse>(`${ADMIN_BASE_URL}/${bookingId}`),
  },
};
