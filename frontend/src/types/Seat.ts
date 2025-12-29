export enum SeatType {
    STANDARD = 'STANDARD',
    VIP = 'VIP',
    DISABLED = 'DISABLED',
    COUPLE = 'COUPLE'
}

export interface Seat {
    id: number;
    row: number;
    number: number;
    seatType: SeatType;
    active: boolean;
    hall?: any;
}

export interface SeatResponse {
    id: number;
    row: number;
    number: number;
    seatType: SeatType;
    active: boolean;
}

export interface SeatRowResponse {
    rowNumber: number;
    seatsCount: number;
    seats: SeatResponse[];
}