import { api } from "@/services/api";
import type {
  TicketResponse,
  TicketFilterRequest,
  TicketCashierResponse,
} from "@/types/ticket";
import type { PageResponse, SearchParams } from "@/types/pagination";

const BASE_URL = "/api/tickets";
const ADMIN_BASE_URL = "/api/admin/tickets";

export const ticketApi = {
  public: {
    getMine: (params?: SearchParams & TicketFilterRequest) =>
      api.get<PageResponse<TicketResponse>>(BASE_URL, { params }),
    getByCode: (ticketCode: string) =>
      api.get<TicketResponse>(`${BASE_URL}/code/${ticketCode}`),
    getQRCode: (ticketCode: string) =>
      api.get<Blob>(`${BASE_URL}/code/${ticketCode}/qr`, {
        responseType: "blob",
      }),
  },
  admin: {
    getByCode: (ticketCode: string) =>
      api.get<TicketCashierResponse>(`${ADMIN_BASE_URL}/${ticketCode}`),
    validate: (ticketCode: string) =>
      api.post<TicketCashierResponse>(
        `${ADMIN_BASE_URL}/${ticketCode}/validate`,
      ),
  },
};
