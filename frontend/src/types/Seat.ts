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
    hall?: any;
}

export interface SeatResponse {
    id: number;
    row: number;
    number: number;
    seatType: SeatType;
}

export interface SeatLayoutRequest {
    rows: number;
    seatsPerRow: number;
    defaultSeatType: SeatType;
}

export interface SeatRowResponse {
    rowNumber: number;
    seatsCount: number;
    seats: SeatResponse[];
}