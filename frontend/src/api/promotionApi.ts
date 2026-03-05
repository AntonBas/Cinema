import { api } from '@/services/api';
import type {
    PromotionResponse,
    UserPromotionResponse,
    PromotionCreateRequest,
    PromotionUpdateRequest,
    UserPromotionCreateRequest
} from '@/types/promotion';
import type { PageResponse } from '@/types/pagination';

const USER_URL = '/api/promotions';
const ADMIN_URL = '/api/admin/promotions';

export const promotionApi = {
    user: {
        getAvailable: () =>
            api.get<PromotionResponse[]>(USER_URL),

        claim: (request: UserPromotionCreateRequest) =>
            api.post<UserPromotionResponse>(`${USER_URL}/claim`, request),

        checkStatus: (promotionId: number) =>
            api.get<boolean>(`${USER_URL}/${promotionId}/status`),
    },

    admin: {
        create: (request: PromotionCreateRequest) =>
            api.post<PromotionResponse>(ADMIN_URL, request),

        getById: (promotionId: number) =>
            api.get<PromotionResponse>(`${ADMIN_URL}/${promotionId}`),

        getAll: (pageable?: { page: number; size: number; sort?: string[] }) =>
            api.get<PageResponse<PromotionResponse>>(ADMIN_URL, { params: pageable }),

        getActive: (pageable?: { page: number; size: number; sort?: string[] }) =>
            api.get<PageResponse<PromotionResponse>>(`${ADMIN_URL}/active`, { params: pageable }),

        update: (promotionId: number, request: PromotionUpdateRequest) =>
            api.put<PromotionResponse>(`${ADMIN_URL}/${promotionId}`, request),

        delete: (promotionId: number) =>
            api.delete<void>(`${ADMIN_URL}/${promotionId}`),
    }
};