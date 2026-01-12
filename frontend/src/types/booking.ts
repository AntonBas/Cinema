export type BookingStatus = 'DRAFT' | 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED' | 'FAILED' | 'REFUNDED';

export interface BookingCreateRequest {
    sessionId: number;
    seats: SeatSelectionRequest[];
    bonusPointsToUse?: number;
}

export interface SeatSelectionRequest {
    seatId: number;
    ticketTypeId: number;
}

export interface BookingResponse {
    id: number;
    bookingNumber: string;
    status: BookingStatus;
    sessionTime: string;
    movieTitle: string;
    hallName: string;
    totalPrice: string;
    bonusPointsUsed: number;
    bonusDiscountAmount: string;
    finalPrice: string;
    expiresAt: string;
    createdAt: string;
    bookedSeats: BookedSeatInfo[];
}

export interface BookedSeatInfo {
    id: number;
    seatId: number;
    row: number;
    seatNumber: number;
    ticketTypeName: string;
    seatPrice: string;
}

export const BookingStatusDisplay: Record<BookingStatus, string> = {
    DRAFT: 'Draft',
    PENDING: 'Pending',
    CONFIRMED: 'Confirmed',
    CANCELLED: 'Cancelled',
    EXPIRED: 'Expired',
    FAILED: 'Failed',
    REFUNDED: 'Refunded'
};

export const BookingStatusColors: Record<BookingStatus, string> = {
    DRAFT: 'default',
    PENDING: 'warning',
    CONFIRMED: 'success',
    CANCELLED: 'error',
    EXPIRED: 'default',
    FAILED: 'error',
    REFUNDED: 'info'
};

export interface BookingsListResponse {
    content: BookingResponse[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
}