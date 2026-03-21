import { SeatType } from './seat';

export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'CANCELLED' | 'EXPIRED' | 'CHECKED_IN';

export interface SeatReservationResponse {
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
    seatType: SeatType;
    available: boolean;
    temporarilyReserved: boolean;
    active: boolean;
    ticketPrices?: TicketPriceInfo[];
}

export interface TicketPriceInfo {
    ticketTypeId: number;
    ticketTypeName: string;
    finalPrice: string;
}

export const ReservationStatusDisplay: Record<ReservationStatus, string> = {
    PENDING: 'Pending',
    CONFIRMED: 'Confirmed',
    CANCELLED: 'Cancelled',
    EXPIRED: 'Expired',
    CHECKED_IN: 'Checked In'
};