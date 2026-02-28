import { api } from '@/services/api';
import type {
    PromotionResponse,
    UserPromotionResponse,
    PromotionCreateRequest,
    PromotionUpdateRequest,
    UserPromotionCreateRequest
} from '@/types/promotion';

const USER_URL = '/api/promotions';
const ADMIN_URL = '/api/admin/promotions';

export const promotionApi = {
    user: {
        getAvailable: () =>
            api.get<PromotionResponse[]>(USER_URL),

        getMyPromotions: () =>
            api.get<UserPromotionResponse[]>(`${USER_URL}/my`),

        claimPromotion: (request: UserPromotionCreateRequest) =>
            api.post<UserPromotionResponse>(`${USER_URL}/claim`, request),

        checkStatus: (promotionId: number) =>
            api.get<boolean>(`${USER_URL}/${promotionId}/status`),
    },

    admin: {
        create: (request: PromotionCreateRequest) =>
            api.post<PromotionResponse>(ADMIN_URL, request),

        getById: (promotionId: number) =>
            api.get<PromotionResponse>(`${ADMIN_URL}/${promotionId}`),

        getAll: () =>
            api.get<PromotionResponse[]>(ADMIN_URL),

        getActive: () =>
            api.get<PromotionResponse[]>(`${ADMIN_URL}/active`),

        update: (promotionId: number, request: PromotionUpdateRequest) =>
            api.put<PromotionResponse>(`${ADMIN_URL}/${promotionId}`, request),

        delete: (promotionId: number) =>
            api.delete<void>(`${ADMIN_URL}/${promotionId}`),
    }
};