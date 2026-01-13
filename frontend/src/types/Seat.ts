export enum SeatType {
    STANDARD = 'STANDARD',
    VIP = 'VIP',
    DISABLED = 'DISABLED',
    COUPLE = 'COUPLE'
}

export const SeatTypeDisplay: Record<SeatType, string> = {
    STANDARD: 'Standard',
    VIP: 'VIP',
    DISABLED: 'Disabled Access',
    COUPLE: 'Couple'
};

export const SeatTypePriceMultiplier: Record<SeatType, number> = {
    STANDARD: 1.0,
    VIP: 1.5,
    DISABLED: 1.0,
    COUPLE: 1.8
};

export const isPremiumSeat = (seatType: SeatType): boolean => {
    return seatType === SeatType.VIP || seatType === SeatType.COUPLE;
};

export const requiresSpecialHandling = (seatType: SeatType): boolean => {
    return seatType === SeatType.COUPLE;
};

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

export interface SeatUpdateRequest {
    seatType?: SeatType;
    active?: boolean;
}

export interface BulkSeatsUpdateRequest {
    seatIds: number[];
    seatType?: SeatType;
    active?: boolean;
}