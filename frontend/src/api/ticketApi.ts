import { api } from "@/services/api";
import type {
  TicketResponse,
  TicketFilterRequest,
  TicketCashierResponse,
} from "@/types/ticket";
import type { PageResponse, SearchParams } from "@/types/pagination";

const BASE_URL = "/api/tickets";
const CASHIER_BASE_URL = "/api/admin/ticket";

export const ticketApi = {
  getUserTickets: (params?: SearchParams & TicketFilterRequest) =>
    api.get<PageResponse<TicketResponse>>(BASE_URL, { params }),

  getByCode: (ticketCode: string) =>
    api.get<TicketResponse>(`${BASE_URL}/code/${ticketCode}`),

  getQRCode: (ticketCode: string) =>
    api.get<Blob>(`${BASE_URL}/code/${ticketCode}/qr`, {
      responseType: "blob",
    }),

  validate: (ticketCode: string) =>
    api.post<void>(`${BASE_URL}/code/${ticketCode}/validate`),

  getTicketForCashier: (uniqueCode: string) =>
    api.get<TicketCashierResponse>(`${CASHIER_BASE_URL}/${uniqueCode}`),

  validateTicket: (uniqueCode: string) =>
    api.post<TicketCashierResponse>(
      `${CASHIER_BASE_URL}/${uniqueCode}/validate`,
    ),
};
