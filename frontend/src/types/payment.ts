export type PaymentStatus = 'PENDING' | 'PROCESSING' | 'SUCCESS' | 'FAILED' | 'CANCELLED' | 'EXPIRED' | 'REFUNDED' | 'PARTIALLY_REFUNDED';

export interface PaymentCreateRequest {
    bookingId: number;
}

export interface LiqPayCallbackRequest {
    payment_id: string;
    order_id: string;
    transaction_id: string;
    status: string;
    sender_card_mask: string;
    err_code: string | null;
    err_description: string | null;
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

export const PaymentStatusColors: Record<PaymentStatus, string> = {
    PENDING: 'warning',
    PROCESSING: 'info',
    SUCCESS: 'success',
    FAILED: 'error',
    CANCELLED: 'error',
    EXPIRED: 'default',
    REFUNDED: 'info',
    PARTIALLY_REFUNDED: 'warning'
};

export interface PaymentsListResponse {
    content: PaymentResponse[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}