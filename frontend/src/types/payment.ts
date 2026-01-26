export type PaymentStatus = 'PENDING' | 'PROCESSING' | 'SUCCESS' | 'FAILED' | 'CANCELLED' | 'EXPIRED' | 'REFUNDED' | 'PARTIALLY_REFUNDED';

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
    bookingId: number;
    bookingNumber: string;
    userEmail: string;
    movieTitle: string;
    sessionTime: string;
    hallName: string;
    amount: string;
    finalAmount: string;
    status: PaymentStatus;
    liqpayOrderId: string | null;
    liqpayPaymentId: string | null;
    liqpayTransactionId: string | null;
    paymentTime: string | null;
    errorCode: string | null;
    errorDescription: string | null;
    senderCardMask: string | null;
    createdAt: string;
    updatedAt: string;
}

export const PaymentStatusDisplay: Record<PaymentStatus, string> = {
    PENDING: 'Pending',
    PROCESSING: 'Processing',
    SUCCESS: 'Success',
    FAILED: 'Failed',
    CANCELLED: 'Cancelled',
    EXPIRED: 'Expired',
    REFUNDED: 'Refunded',
    PARTIALLY_REFUNDED: 'Partially Refunded'
};