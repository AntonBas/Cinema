export enum SeatType {
    STANDARD = 'STANDARD',
    VIP = 'VIP',
    COUPLE = 'COUPLE'
}

export const SeatTypeDisplay: Record<SeatType, string> = {
    [SeatType.STANDARD]: 'Standard',
    [SeatType.VIP]: 'VIP',
    [SeatType.COUPLE]: 'Couple'
};

export const SeatTypePriceMultiplier: Record<SeatType, number> = {
    [SeatType.STANDARD]: 1.0,
    [SeatType.VIP]: 1.5,
    [SeatType.COUPLE]: 1.8
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