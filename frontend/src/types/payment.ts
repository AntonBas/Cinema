export type PaymentStatus =
  | "PENDING"
  | "PROCESSING"
  | "SUCCESS"
  | "FAILED"
  | "CANCELLED"
  | "EXPIRED"
  | "REFUNDED"
  | "PARTIALLY_REFUNDED";

export interface PaymentCreateRequest {
  bookingId: number;
}

export interface LiqPayCallbackRequest {
  data: string;
  signature: string;
}

export interface PaymentLiqPayDataResponse {
  data: string;
  signature: string;
  paymentUrl: string;
  liqpayOrderId: string;
}

export interface PaymentResponse {
  id: number;
  bookingNumber: string;
  movieTitle: string;
  sessionTime: string;
  hallName: string;
  finalAmount: string;
  status: PaymentStatus;
  paymentTime: string;
  senderCardMask: string;
  errorDescription?: string;
}

export const PaymentStatusDisplay: Record<PaymentStatus, string> = {
  PENDING: "Pending",
  PROCESSING: "Processing",
  SUCCESS: "Success",
  FAILED: "Failed",
  CANCELLED: "Cancelled",
  EXPIRED: "Expired",
  REFUNDED: "Refunded",
  PARTIALLY_REFUNDED: "Partially Refunded",
};
