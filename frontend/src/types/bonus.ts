export type BonusTransactionType =
    | 'WELCOME_BONUS'
    | 'BIRTHDAY_BONUS'
    | 'PROMOTION_BONUS'
    | 'BOOKING_SPEND'
    | 'PAYMENT_ACCRUAL'
    | 'REFUND_RETURN'
    | 'BOOKING_CANCEL';

export interface UseBonusPointsRequest {
    pointsToUse: number;
}

export interface BonusRulesRequest {
    points?: number | null;
    moneyRatio?: string | null;
    minPointsPerTransaction?: number | null;
    maxPointsPerTransaction?: number | null;
    active?: boolean | null;
}

export interface BonusBalanceResponse {
    pointsBalance: number;
    pointValue: string;
    balanceValue: string;
    minUsablePoints: number;
    maxUsablePoints: number;
    minRedemptionValue: string;
    maxRedemptionValue: string;
}

export interface BonusCardResponse {
    id: number;
    pointsBalance: number;
    lastBirthdayBonusDate: string | null;
    welcomeBonusReceived: boolean;
    userId: number;
}

export interface BonusRulesResponse {
    id: number;
    bonusType: string;
    points?: number | null;
    moneyRatio?: string | null;
    minPointsPerTransaction?: number | null;
    maxPointsPerTransaction?: number | null;
    active: boolean;
    updatedAt: string;
}

export interface BonusTransactionResponse {
    id: number;
    type: BonusTransactionType;
    typeDisplay: string;
    pointsChange: string;
    createdAt: string;
    newBalance: number;
    bookingDetails?: {
        movieTitle: string;
        bookingReference: string;
        cinemaHall: string;
        sessionDateTime: string;
    } | null;
}

export interface BonusTransactionProjection {
    id: number;
    type: string;
    typeDisplay: string;
    pointsChangeRaw: number;
    pointsChange: string;
    createdAt: string;
    newBalance: number;
    movieTitle?: string | null;
    bookingReference?: string | null;
    cinemaHall?: string | null;
    sessionDateTime?: string | null;
}

export const BonusTransactionTypeDisplay: Record<BonusTransactionType, string> = {
    WELCOME_BONUS: 'Welcome Bonus',
    BIRTHDAY_BONUS: 'Birthday Bonus',
    PROMOTION_BONUS: 'Promotion Bonus',
    BOOKING_SPEND: 'Booking Spend',
    PAYMENT_ACCRUAL: 'Payment Accrual',
    REFUND_RETURN: 'Refund Return',
    BOOKING_CANCEL: 'Booking Cancel'
};