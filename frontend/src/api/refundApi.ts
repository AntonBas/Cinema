import { api } from "@/services/api";
import type { PageResponse, SearchParams } from "@/types/pagination";
import type {
  AdminRefundFilters,
  AdminRefundListResponse,
  RefundResponse,
  RefundRequest,
  RefundPolicy,
  RefundPreviewRequest,
  RefundPreviewResponse,
} from "@/types/refund";

const BASE_URL = "/api/refunds";
const ADMIN_BASE_URL = "/api/admin/refunds";

export const refundApi = {
  create: (request: RefundRequest) =>
    api.post<RefundResponse>(BASE_URL, request),
  preview: (request: RefundPreviewRequest) =>
    api.post<RefundPreviewResponse>(`${BASE_URL}/preview`, request),
  getPolicy: () => api.get<RefundPolicy>(`${BASE_URL}/policy`),

  admin: {
    getAll: (params: SearchParams & AdminRefundFilters) =>
      api.get<PageResponse<AdminRefundListResponse>>(ADMIN_BASE_URL, {
        params,
      }),
  },
};
