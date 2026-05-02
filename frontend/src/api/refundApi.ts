import { api } from "@/services/api";
import type {
  RefundResponse,
  RefundRequest,
  RefundPolicy,
} from "@/types/refund";

const BASE_URL = "/api/refunds";

export const refundApi = {
  processRefund: (request: RefundRequest) =>
    api.post<RefundResponse>(BASE_URL, request),

  getPolicy: () => api.get<RefundPolicy>(`${BASE_URL}/policy`),
};
