export type PaymentStatus =
  | "PENDING"
  | "PROCESSING"
  | "SUCCESS"
  | "FAILED"
  | "CANCELLED"
  | "EXPIRED"
  | "REFUNDED"
  | "PARTIALLY_REFUNDED"
  | "REFUND_REQUIRED";

export interface PaymentCreateRequest {
  bookingId: string;
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
  paymentTime: string | null;
  expiresAt: string | null;
  senderCardMask: string;
  errorDescription?: string;
}

export const FINAL_PAYMENT_STATUSES: PaymentStatus[] = [
  "SUCCESS",
  "FAILED",
  "CANCELLED",
  "EXPIRED",
  "REFUND_REQUIRED",
  "REFUNDED",
  "PARTIALLY_REFUNDED",
];

export const LATE_PAYMENT_REFUND_STATUSES: PaymentStatus[] = [
  "REFUND_REQUIRED",
];

export const REFUNDED_PAYMENT_STATUSES: PaymentStatus[] = [
  "REFUNDED",
  "PARTIALLY_REFUNDED",
];

export const PaymentStatusDisplay: Record<PaymentStatus, string> = {
  PENDING: "Pending",
  PROCESSING: "Processing",
  SUCCESS: "Success",
  FAILED: "Failed",
  CANCELLED: "Cancelled",
  EXPIRED: "Expired",
  REFUNDED: "Refunded",
  PARTIALLY_REFUNDED: "Partially Refunded",
  REFUND_REQUIRED: "Refund in Progress",
};
