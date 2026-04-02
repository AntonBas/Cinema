export interface PromotionCreateRequest {
    title: string;
    description?: string;
    bonusPoints: number;
    startDate?: string;
    endDate?: string;
}

export interface PromotionUpdateRequest {
    title: string;
    description?: string;
    bonusPoints: number;
    startDate?: string;
    endDate?: string;
}

export interface UserPromotionCreateRequest {
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

export interface PromotionAdminResponse {
    id: number;
    title: string;
    bonusPoints: number;
    startDate?: string;
    endDate?: string;
}