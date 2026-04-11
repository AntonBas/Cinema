export type RefundStatus = 'PENDING' | 'COMPLETED' | 'REJECTED' | 'CANCELLED';
export type RefundItemStatus = 'PENDING' | 'COMPLETED' | 'REJECTED' | 'CANCELLED';

export interface RefundPreviewRequest {
    ticketId: number;
}

export interface RefundRequest {
    ticketId: number;
    reason?: string;
}

export interface RefundPreviewResponse {
    ticketId: number;
    ticketCode: string;
    movieTitle: string;
    sessionTime: string;
    hallName: string;
    seatInfo: string;
    originalPrice: string;
    finalPrice: string;
    refundAmount: string;
    refundPercentage: string;
    feeAmount: string;
    feePercentage: string;
    bonusPointsUsed: number;
    bonusPointsToRefund: number;
    policyName: string;
    policyDescription: string;
    isRefundable: boolean;
    nonRefundableReason?: string;
    refundDeadline?: string;
    remainingTime?: string;
    purchaseTime: string;
    ticketType: string;
}

export interface RefundResponse {
    id: number;
    refundNumber: string;
    status: string;
    totalAmount: string;
    totalBonusPointsToRefund: number;
    reason?: string;
    processedBy?: string;
    processedAt?: string;
    paymentId: number;
    paymentMethod: string;
    items: RefundItemResponse[];
    message: string;
    estimatedRefundTime: string;
}

export interface RefundItemResponse {
    id: number;
    ticketId: number;
    ticketCode: string;
    ticketPrice: string;
    refundPercentage: string;
    refundAmount: string;
    bonusPointsToDeduct: number;
    status: string;
}

export const RefundStatusDisplay: Record<string, string> = {
    PENDING: 'Pending',
    COMPLETED: 'Completed',
    REJECTED: 'Rejected',
    CANCELLED: 'Cancelled'
};

export const RefundItemStatusDisplay: Record<string, string> = {
    PENDING: 'Pending',
    COMPLETED: 'Completed',
    REJECTED: 'Rejected',
    CANCELLED: 'Cancelled'
};