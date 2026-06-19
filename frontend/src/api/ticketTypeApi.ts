import { api } from "@/services/api";
import type { TicketTypeResponse, TicketTypeRequest } from "@/types/ticketType";
import type { PageResponse } from "@/types/pagination";

const BASE_URL = "/api/admin/ticket-types";

export const ticketTypeApi = {
  admin: {
    create: (request: TicketTypeRequest) =>
      api.post<TicketTypeResponse>(BASE_URL, request),

    getAll: (params?: {
      active?: boolean;
      category?: string;
      query?: string;
      page?: number;
      size?: number;
    }) => api.get<PageResponse<TicketTypeResponse>>(BASE_URL, { params }),

    update: (id: number, request: TicketTypeRequest) =>
      api.put<TicketTypeResponse>(`${BASE_URL}/${id}`, request),

    delete: (id: number) => api.delete<void>(`${BASE_URL}/${id}`),

    toggleActive: (id: number) =>
      api.patch<TicketTypeResponse>(`${BASE_URL}/${id}/toggle`),
  },
};
