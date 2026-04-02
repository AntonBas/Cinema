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

export const getSeatCount = (seatType: SeatType): number => {
    switch (seatType) {
        case SeatType.COUPLE:
            return 2;
        default:
            return 1;
    }
};

export const getSeatDisplayName = (seatType: SeatType): string => {
    const seatsCount = getSeatCount(seatType);
    const baseName = SeatTypeDisplay[seatType];
    return seatsCount > 1 ? `${baseName} (${seatsCount} seats)` : baseName;
};

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