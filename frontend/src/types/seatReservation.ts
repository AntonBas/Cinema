import { SeatType } from './seat';

export type ReservationStatus = 'PENDING' | 'CONFIRMED' | 'EXPIRED';

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
    ticketPrices: TicketPriceInfo[];
}

export interface TicketPriceInfo {
    ticketTypeId: number;
    ticketTypeName: string;
    finalPrice: string;
    minAge?: number;
    maxAge?: number;
    requiresDocument: boolean;
    documentType?: string;
}

export const ReservationStatusDisplay: Record<ReservationStatus, string> = {
    PENDING: 'Pending',
    CONFIRMED: 'Confirmed',
    EXPIRED: 'Expired'
};