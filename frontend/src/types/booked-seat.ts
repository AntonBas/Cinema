export type BookedSeatStatus =
    | 'TEMPORARY'
    | 'PENDING'
    | 'CONFIRMED'
    | 'CANCELLED'
    | 'EXPIRED'
    | 'CHECKED_IN';

export interface SeatAvailabilityResponse {
    sessionId: number;
    movieTitle: string;
    basePrice: string;
    hallName: string;
    availableSeats: number;
    seats: SeatInfo[];
}

export interface SeatInfo {
    id: number;
    row: number;
    seatNumber: number;
    seatType: string;
    available: boolean;
    temporarilyReserved: boolean;
    ticketPrices: TicketPriceInfo[];
}

export interface TicketPriceInfo {
    ticketTypeId: number;
    ticketTypeName: string;
    finalPrice: string;
}

export const BookedSeatStatusDisplay: Record<BookedSeatStatus, string> = {
    TEMPORARY: 'Temporary',
    PENDING: 'Pending',
    CONFIRMED: 'Confirmed',
    CANCELLED: 'Cancelled',
    EXPIRED: 'Expired',
    CHECKED_IN: 'Checked In'
};

export const BookedSeatStatusColors: Record<BookedSeatStatus, string> = {
    TEMPORARY: 'default',
    PENDING: 'warning',
    CONFIRMED: 'success',
    CANCELLED: 'error',
    EXPIRED: 'default',
    CHECKED_IN: 'info'
};