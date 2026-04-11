export interface PromotionRequest {
    title: string;
    description?: string;
    bonusPoints: number;
    startDate?: string;
    endDate?: string;
}

export interface ClaimPromotionRequest {
    promotionId: number;
}

export interface PromotionResponse {
    id: number;
    title: string;
    description?: string;
    bonusPoints: number;
    startDate?: string;
    endDate?: string;
}

export interface PromotionListResponse {
    id: number;
    title: string;
    bonusPoints: number;
    startDate?: string;
    endDate?: string;
}