import { api } from "@/services/api";
import type {
  PaymentResponse,
  PaymentCreateRequest,
  PaymentLiqPayDataResponse,
} from "@/types/payment";

const BASE_URL = "/api/payments";

export const paymentApi = {
  create: (request: PaymentCreateRequest) =>
    api.post<PaymentResponse>(BASE_URL, request),

  getById: (paymentId: number) =>
    api.get<PaymentResponse>(`${BASE_URL}/${paymentId}`),

  getLiqPayData: (paymentId: number) =>
    api.get<PaymentLiqPayDataResponse>(`${BASE_URL}/${paymentId}/liqpay-data`),

  retry: (paymentId: number) =>
    api.post<PaymentResponse>(`${BASE_URL}/${paymentId}/retry`),
};
