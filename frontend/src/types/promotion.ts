export interface PromotionRequest {
  title: string;
  description?: string;
  bonusPoints: number;
  startDate?: string;
  endDate?: string;
  active?: boolean;
}

export interface ClaimPromotionRequest {
  promotionId: number;
}

export type PromotionStatus = "UPCOMING" | "ACTIVE" | "EXPIRED" | "INACTIVE";

export interface PromotionResponse {
  id: number;
  title: string;
  description?: string;
  bonusPoints: number;
  startDate?: string;
  endDate?: string;
  active: boolean;
  status: PromotionStatus;
}

export interface PromotionListResponse {
  id: number;
  title: string;
  bonusPoints: number;
  startDate?: string;
  endDate?: string;
  active: boolean;
  status: PromotionStatus;
}
