export type RefundStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'PROCESSED' | 'CANCELLED';
export type RefundItemStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'PROCESSED' | 'CANCELLED';

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
    status: RefundStatus;
    totalAmount: string;
    totalBonusPointsToDeduct: number;
    reason?: string;
    processedBy?: string;
    processedAt?: string;
    createdAt: string;
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
    status: RefundItemStatus;
    createdAt: string;
}

export interface RefundDetailsResponse {
    id: number;
    refundNumber: string;
    status: RefundStatus;
    userId: number;
    userEmail: string;
    userName: string;
    paymentId: number;
    paymentReference: string;
    paymentAmount: string;
    totalAmount: string;
    totalBonusPointsToDeduct: number;
    reason?: string;
    processedBy?: string;
    processedAt?: string;
    createdAt: string;
    updatedAt: string;
    items: RefundItemDetails[];
    bonusTransactions: RefundBonusTransactionResponse[];
    paymentStatus: string;
    bonusStatus: string;
}

export interface RefundItemDetails {
    id: number;
    ticketId: number;
    ticketCode: string;
    movieTitle: string;
    sessionTime: string;
    seatInfo: string;
    ticketPrice: string;
    refundPercentage: string;
    refundAmount: string;
    feeAmount: string;
    bonusPointsUsed: number;
    bonusPointsToDeduct: number;
    status: RefundItemStatus;
    createdAt: string;
}

export interface RefundBonusTransactionResponse {
    id: number;
    points: number;
    type: string;
    description: string;
    createdAt: string;
}

export const RefundStatusDisplay: Record<RefundStatus, string> = {
    PENDING: 'Pending',
    APPROVED: 'Approved',
    REJECTED: 'Rejected',
    PROCESSED: 'Processed',
    CANCELLED: 'Cancelled'
};

export const RefundStatusColors: Record<RefundStatus, string> = {
    PENDING: 'warning',
    APPROVED: 'info',
    REJECTED: 'error',
    PROCESSED: 'success',
    CANCELLED: 'default'
};

export const RefundItemStatusDisplay: Record<RefundItemStatus, string> = {
    PENDING: 'Pending',
    APPROVED: 'Approved',
    REJECTED: 'Rejected',
    PROCESSED: 'Processed',
    CANCELLED: 'Cancelled'
};

export interface RefundsListResponse {
    content: RefundDetailsResponse[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}