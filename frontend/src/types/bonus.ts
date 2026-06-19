export type BonusTransactionType =
  | "WELCOME_BONUS"
  | "BIRTHDAY_BONUS"
  | "PROMOTION_BONUS"
  | "BOOKING_SPEND"
  | "PAYMENT_ACCRUAL"
  | "REFUND_RETURN"
  | "BOOKING_CANCEL";

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

export interface BonusRulesResponse {
  id: number;
  bonusType: BonusTransactionType;
  points?: number | null;
  moneyRatio?: string | null;
  minPointsPerTransaction?: number | null;
  maxPointsPerTransaction?: number | null;
  active: boolean;
}

export interface BonusTransactionResponse {
  id: number;
  type: BonusTransactionType;
  pointsChange: string;
  createdAt: string;
  newBalance: number;
}

export const BonusTransactionTypeDisplay: Record<BonusTransactionType, string> =
  {
    WELCOME_BONUS: "Welcome Bonus",
    BIRTHDAY_BONUS: "Birthday Bonus",
    PROMOTION_BONUS: "Promotion Bonus",
    BOOKING_SPEND: "Booking Spend",
    PAYMENT_ACCRUAL: "Payment Accrual",
    REFUND_RETURN: "Refund Return",
    BOOKING_CANCEL: "Booking Cancel",
  };
