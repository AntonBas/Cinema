export type BookingStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED' | 'FAILED' | 'REFUNDED';

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
    sessionId: number;
    sessionTime: string;
    movieTitle: string;
    hallName: string;
    totalPrice: string;
    bonusPointsUsed: number;
    bonusDiscountAmount: string;
    finalPrice: string;
    liqpayOrderId?: string;
    expiresAt: string;
    seatReservations: SeatReservationInfo[];
}

export interface SeatReservationInfo {
    id: number;
    seatId: number;
    row: number;
    seatNumber: number;
    ticketTypeName: string;
    seatPrice: string;
}

export const BookingStatusDisplay: Record<BookingStatus, string> = {
    PENDING: 'Pending',
    CONFIRMED: 'Confirmed',
    CANCELLED: 'Cancelled',
    EXPIRED: 'Expired',
    FAILED: 'Failed',
    REFUNDED: 'Refunded'
};