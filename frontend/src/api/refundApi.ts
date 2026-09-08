import { api } from "@/services/api";
import type {
  RefundResponse,
  RefundRequest,
  RefundPolicy,
  RefundPreviewRequest,
  RefundPreviewResponse,
} from "@/types/refund";

const BASE_URL = "/api/refunds";

export const refundApi = {
  processRefund: (request: RefundRequest) =>
    api.post<RefundResponse>(BASE_URL, request),

  preview: (request: RefundPreviewRequest) =>
    api.post<RefundPreviewResponse>(`${BASE_URL}/preview`, request),

  getPolicy: () => api.get<RefundPolicy>(`${BASE_URL}/policy`),
};
